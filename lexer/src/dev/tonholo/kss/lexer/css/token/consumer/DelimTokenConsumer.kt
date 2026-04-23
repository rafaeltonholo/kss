package dev.tonholo.kss.lexer.css.token.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.css.CssTokenKind

/**
 * Consumes a single code point and emits a [CssTokenKind.Delim] token.
 *
 * Per CSS Syntax Module Level 3, any input code point that does not match
 * another token production returns a `<delim-token>` whose value is the
 * current input code point.
 *
 * See: https://www.w3.org/TR/css-syntax-3/#consume-token
 */
class DelimTokenConsumer(
    iterator: TokenIterator<CssTokenKind>,
) : TokenConsumer(iterator) {
    override val supportedTokenKinds: Set<CssTokenKind> =
        setOf(
            CssTokenKind.Delim
        )

    override fun consume(kind: CssTokenKind): List<Token<out CssTokenKind>> {
        val start = iterator.offset
        val end = iterator.nextOffset()
        return listOf(Token(CssTokenKind.Delim, start, end))
    }
}
