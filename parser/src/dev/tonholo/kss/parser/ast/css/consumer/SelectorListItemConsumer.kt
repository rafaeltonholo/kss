package dev.tonholo.kss.parser.ast.css.consumer

import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.parser.ast.css.CssCombinator
import dev.tonholo.kss.parser.ast.css.syntax.node.CssLocation
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import dev.tonholo.kss.parser.ast.css.syntax.node.SelectorListItem
import dev.tonholo.kss.parser.ast.iterator.AstParserIterator
import dev.tonholo.kss.parser.ast.iterator.parserError

val selectorTokens =
    setOf(
        CssTokenKind.Ident,
        CssTokenKind.Hash,
        CssTokenKind.Asterisk,
        CssTokenKind.Colon,
        CssTokenKind.Dot,
        CssTokenKind.OpenSquareBracket,
        CssTokenKind.Colon,
        CssTokenKind.DoubleColon
    )

/**
 * Consumes a selector list item, which is a comma-separated list of selectors
 * from the given iterator and builds a [SelectorListItem] object.
 *
 * For example:
 * ```css
 * h1, h2 { color: red; }
 * ```
 *
 * In this case, `h1` and `h2` are two selector list items.
 *
 * @param content The entire CSS content string.
 * @param simpleSelectorConsumer A consumer responsible for consuming individual selectors.
 */
class SelectorListItemConsumer(
    content: String,
    private val simpleSelectorConsumer: Consumer<Selector>,
) : Consumer<SelectorListItem>(content) {
    override fun consume(iterator: AstParserIterator<CssTokenKind>): SelectorListItem {
        val current = iterator.expectToken(selectorTokens)
        val location = CssLocation.Undefined
        val selectors = mutableListOf<Selector>()
        val selectorListItem =
            SelectorListItem(
                location = location,
                selectors = selectors
            )

        // re-consume current token.
        iterator.rewind()
        var pendingCombinator = false
        while (iterator.hasNext()) {
            val next = iterator.expectNextTokenNotNull()
            when (next.kind) {
                CssTokenKind.Comma -> {
                    if (pendingCombinator) {
                        iterator.parserError(content, "Trailing combinator before ','")
                    }
                    break
                }

                CssTokenKind.OpenCurlyBrace -> {
                    if (pendingCombinator) {
                        iterator.parserError(content, "Trailing combinator before '{'")
                    }
                    iterator.rewind()
                    break
                }

                CssTokenKind.EndOfFile -> {
                    if (pendingCombinator) {
                        iterator.parserError(content, "Trailing combinator at end of selector")
                    }
                    iterator.rewind()
                    break
                }

                in CssCombinator.tokens -> {
                    pendingCombinator = true
                }

                else -> {
                    pendingCombinator = false
                    selectors += simpleSelectorConsumer.consume(iterator)
                }
            }
        }
        if (pendingCombinator) {
            iterator.parserError(content, "Trailing combinator at end of selector")
        }

        return selectorListItem.copy(
            location =
                selectors.last().let { last ->
                    CssLocation(
                        source = content.substring(current.startOffset, last.location.end),
                        start = current.startOffset,
                        end = last.location.end
                    )
                }
        )
    }
}
