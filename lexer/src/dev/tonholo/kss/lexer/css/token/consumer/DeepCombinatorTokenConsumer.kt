package dev.tonholo.kss.lexer.css.token.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.lexer.css.DEEP_COMBINATOR

/**
 * Consumes the legacy `/deep/` Shadow DOM piercing descendant combinator
 * and emits a [CssTokenKind.DeepCombinator] token spanning all six
 * characters.
 */
class DeepCombinatorTokenConsumer(
    iterator: TokenIterator<CssTokenKind>,
) : TokenConsumer(iterator) {
    override val supportedTokenKinds: Set<CssTokenKind> =
        setOf(
            CssTokenKind.DeepCombinator
        )

    override fun consume(kind: CssTokenKind): List<Token<out CssTokenKind>> {
        val start = iterator.offset
        val end = iterator.nextOffset(steps = DEEP_COMBINATOR.length)
        return listOf(Token(CssTokenKind.DeepCombinator, start, end))
    }
}
