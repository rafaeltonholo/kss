package dev.tonholo.kss.parser.ast.css.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.parser.ast.css.CssCombinator
import dev.tonholo.kss.parser.ast.css.syntax.node.CssLocation
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import dev.tonholo.kss.parser.ast.iterator.AstParserIterator
import dev.tonholo.kss.parser.ast.iterator.parserError

/**
 * Consumes a simple CSS selectors from the given iterator and builds a [Selector] object.
 *
 * This consumer handles the parsing of individual selector components,
 * such as type selectors, ID selectors, class selectors, attribute selectors,
 * pseudo-class selectors, and pseudo-element selectors. It also handles
 * combinators between selectors.
 *
 * @param content The CSS content being parsed.
 */
class SimpleSelectorConsumer(
    content: String,
) : Consumer<Selector>(content) {
    override fun consume(iterator: AstParserIterator<CssTokenKind>): Selector {
        val current = iterator.expectToken(selectorTokens)
        return when (current.kind) {
            CssTokenKind.Ident, CssTokenKind.Asterisk -> {
                Selector.Type(
                    location = calculateLocation(current, current),
                    name = content.substring(current.startOffset, endIndex = current.endOffset),
                    combinator = lookupForCombinator(iterator)
                )
            }

            CssTokenKind.Hash -> {
                val next = iterator.expectNextToken(kind = CssTokenKind.Ident)
                Selector.Id(
                    location = calculateLocation(current, next),
                    name = content.substring(next.startOffset, endIndex = next.endOffset),
                    combinator = lookupForCombinator(iterator)
                )
            }

            CssTokenKind.Dot -> {
                val next = iterator.expectNextToken(kind = CssTokenKind.Ident)
                Selector.Class(
                    location = calculateLocation(current, next),
                    name = content.substring(next.startOffset, endIndex = next.endOffset),
                    combinator = lookupForCombinator(iterator)
                )
            }

            CssTokenKind.OpenSquareBracket -> {
                val nameToken = iterator.expectNextToken(kind = CssTokenKind.Ident)
                val name = content.substring(nameToken.startOffset, endIndex = nameToken.endOffset)
                var afterName = iterator.expectNextTokenNotNull()
                if (afterName.kind == CssTokenKind.WhiteSpace) {
                    afterName = iterator.expectNextTokenNotNull()
                }
                val matcher: String?
                val value: String?
                val endToken: Token<out CssTokenKind>
                if (afterName.kind == CssTokenKind.CloseSquareBracket) {
                    matcher = null
                    value = null
                    endToken = afterName
                } else {
                    matcher = buildAttributeMatcher(afterName, iterator)
                    var valueToken = iterator.expectNextTokenNotNull()
                    if (valueToken.kind == CssTokenKind.WhiteSpace) {
                        valueToken = iterator.expectNextTokenNotNull()
                    }
                    value = extractAttributeValue(valueToken)
                    val nextToken = iterator.expectNextTokenNotNull()
                    if (nextToken.kind == CssTokenKind.WhiteSpace) {
                        endToken = iterator.expectNextToken(kind = CssTokenKind.CloseSquareBracket)
                    } else {
                        iterator.expectToken(kind = CssTokenKind.CloseSquareBracket)
                        endToken = nextToken
                    }
                }
                Selector.Attribute(
                    location = calculateLocation(current.startOffset, endToken.endOffset),
                    name = name,
                    matcher = matcher,
                    value = value,
                    combinator = lookupForCombinator(iterator)
                )
            }

            CssTokenKind.DoubleColon -> {
                val next = iterator.expectNextToken(kind = CssTokenKind.Ident)
                val parameters = buildParameters(iterator)
                val endOffset =
                    if (parameters.isEmpty()) {
                        next.endOffset
                    } else {
                        parameters.last().location.end + 1
                    }
                Selector.PseudoElement(
                    location = calculateLocation(current.startOffset, endOffset),
                    name = content.substring(next.startOffset, endIndex = next.endOffset),
                    parameters = parameters,
                    combinator = lookupForCombinator(iterator)
                )
            }

            CssTokenKind.Colon -> {
                val next = iterator.expectNextToken(kind = CssTokenKind.Ident)
                val parameters = buildParameters(iterator)
                val endOffset =
                    if (parameters.isEmpty()) {
                        next.endOffset
                    } else {
                        parameters.last().location.end + 1
                    }
                Selector.PseudoClass(
                    location = calculateLocation(current.startOffset, endOffset),
                    name = content.substring(next.startOffset, endIndex = next.endOffset),
                    parameters = parameters,
                    combinator = lookupForCombinator(iterator)
                )
            }

            else -> {
                iterator.parserError(content, "Unexpected token ${current.kind}")
            }
        }
    }

    private val compoundMatcherPrefixes =
        setOf(
            CssTokenKind.Tilde,
            CssTokenKind.Pipe,
            CssTokenKind.Caret,
            CssTokenKind.Dollar,
            CssTokenKind.Asterisk
        )

    private fun buildAttributeMatcher(
        firstToken: Token<out CssTokenKind>,
        iterator: AstParserIterator<CssTokenKind>,
    ): String {
        val prefix = content.substring(firstToken.startOffset, endIndex = firstToken.endOffset)
        if (firstToken.kind in compoundMatcherPrefixes) {
            val equalsToken = iterator.expectNextToken(kind = CssTokenKind.Equals)
            return prefix + content.substring(equalsToken.startOffset, endIndex = equalsToken.endOffset)
        }
        return prefix
    }

    private fun extractAttributeValue(token: Token<out CssTokenKind>): String {
        val raw = content.substring(token.startOffset, endIndex = token.endOffset)
        return when (token.kind) {
            CssTokenKind.String -> raw.removeSurrounding("\"").removeSurrounding("'")
            CssTokenKind.Ident -> raw
            else -> raw
        }
    }

    private fun lookupForCombinator(iterator: AstParserIterator<CssTokenKind>): CssCombinator? =
        CssCombinator.from(iterator.peek(steps = 0)?.kind)

    private fun calculateLocation(
        startToken: Token<out CssTokenKind>,
        endToken: Token<out CssTokenKind>,
    ): CssLocation = calculateLocation(startToken.startOffset, endToken.endOffset)

    private fun calculateLocation(
        startOffset: Int,
        endOffset: Int,
    ): CssLocation =
        CssLocation(
            source = content.substring(startOffset, endOffset),
            start = startOffset,
            end = endOffset
        )

    private fun buildParameters(iterator: AstParserIterator<CssTokenKind>): List<Selector> {
        if (iterator.expectNextTokenNotNull().kind != CssTokenKind.OpenParenthesis) {
            iterator.rewind()
            return emptyList()
        }
        val parameters = mutableListOf<Selector>()
        while (iterator.hasNext()) {
            val next = iterator.expectNextTokenNotNull()
            if (next.kind == CssTokenKind.CloseParenthesis) {
                break
            }
            parameters += consume(iterator)
        }

        return parameters
    }
}
