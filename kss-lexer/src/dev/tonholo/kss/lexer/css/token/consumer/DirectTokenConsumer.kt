package dev.tonholo.kss.lexer.css.token.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.css.CssTokenKind

class DirectTokenConsumer(
    iterator: TokenIterator<CssTokenKind>,
) : TokenConsumer(iterator) {
    override val supportedTokenKinds: Set<CssTokenKind> =
        setOf(
            CssTokenKind.Greater,
            CssTokenKind.Dot,
            CssTokenKind.Comma,
            CssTokenKind.Colon,
            CssTokenKind.Semicolon,
            CssTokenKind.OpenCurlyBrace,
            CssTokenKind.CloseCurlyBrace,
            CssTokenKind.OpenParenthesis,
            CssTokenKind.CloseParenthesis
        )

    override fun accept(kind: CssTokenKind): Boolean =
        when {
            kind == CssTokenKind.Dot -> {
                val next = iterator.peek(offset = 1)
                !next.isDigit()
            }

            else -> {
                super.accept(kind)
            }
        }

    override fun consume(kind: CssTokenKind): List<Token<out CssTokenKind>> {
        val start = iterator.offset
        val end = iterator.nextOffset()
        val token = Token(kind, start, end)
        return listOf(token)
    }
}
