package dev.tonholo.kss.lexer.css.token.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.css.CssTokenKind

class AtKeywordTokenConsumer(
    iterator: TokenIterator<CssTokenKind>,
) : TokenConsumer(iterator) {
    override val supportedTokenKinds: Set<CssTokenKind> =
        setOf(
            CssTokenKind.AtKeyword
        )

    override fun consume(kind: CssTokenKind): List<Token<out CssTokenKind>> {
        val start = iterator.offset
        iterator.nextOffset()
        while (iterator.hasNext()) {
            val char = iterator.next()
            if (char == ' ') {
                break
            }
        }
        return listOf(Token(CssTokenKind.AtKeyword, start, iterator.offset))
    }
}
