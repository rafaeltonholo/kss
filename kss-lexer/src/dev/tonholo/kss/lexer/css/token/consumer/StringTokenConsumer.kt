package dev.tonholo.kss.lexer.css.token.consumer

import dev.tonholo.kss.extensions.EMPTY
import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.css.CssTokenKind

private const val ESCAPE_CHAR = '\\'
private val quotesTokens: Set<Char> = CssTokenKind.Quote.representation +
    CssTokenKind.DoubleQuote.representation

class StringTokenConsumer(
    iterator: TokenIterator<CssTokenKind>,
) : TokenConsumer(iterator) {
    override val supportedTokenKinds: Set<CssTokenKind> = setOf(
        CssTokenKind.Quote,
        CssTokenKind.DoubleQuote,
    )

    override fun consume(kind: CssTokenKind): List<Token<out CssTokenKind>> {
        val start = iterator.offset
        var currentOffset = 1
        while (true) {
            val current = iterator.peek(currentOffset++)
            when (current) {
                ESCAPE_CHAR if iterator.hasNext() && iterator.peek(currentOffset) in quotesTokens -> {
                    currentOffset++
                }

                in quotesTokens, Char.EMPTY -> {
                    break
                }
            }
        }

        iterator.nextOffset(currentOffset)
        return listOf(
            if (iterator.hasNext().not() && iterator.peek(-1) !in quotesTokens) {
                Token(CssTokenKind.BadString, start, iterator.offset)
            } else {
                Token(CssTokenKind.String, start, iterator.offset)
            },
        )
    }
}
