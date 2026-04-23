package dev.tonholo.kss.lexer.css.token.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.lexer.css.isIdentCodePoint

class IdentTokenConsumer(
    iterator: TokenIterator<CssTokenKind>,
) : TokenConsumer(iterator) {
    override val supportedTokenKinds: Set<CssTokenKind> =
        setOf(
            CssTokenKind.Ident
        )

    override fun consume(kind: CssTokenKind): List<Token<out CssTokenKind>> {
        val start = iterator.offset
        while (iterator.hasNext() && iterator.get().isIdentCodePoint()) {
            iterator.nextOffset()
        }

        return listOf(
            Token(CssTokenKind.Ident, start, iterator.offset)
        )
    }
}
