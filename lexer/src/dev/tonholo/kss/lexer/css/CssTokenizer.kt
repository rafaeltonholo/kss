package dev.tonholo.kss.lexer.css

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.TokenIterator
import dev.tonholo.kss.lexer.Tokenizer
import dev.tonholo.kss.lexer.css.token.consumer.AtKeywordTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.CommentTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.DeepCombinatorTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.DelimTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.DirectTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.FunctionTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.HashTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.IdentTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.NumberTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.StringTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.TokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.UrlTokenConsumer
import dev.tonholo.kss.lexer.css.token.consumer.WhitespaceTokenConsumer

/**
 * The CSS tokenizer is responsible for converting a CSS source code string
 * into a stream of tokens.
 *
 * It uses a set of token consumers to identify and consume different types
 * of tokens based on the current character in the input stream.
 */
class CssTokenizer(
    private val iterator: TokenIterator<CssTokenKind> = CssTokenIterator(),
    private val consumers: Set<TokenConsumer> =
        setOf(
            AtKeywordTokenConsumer(iterator),
            WhitespaceTokenConsumer(iterator),
            DirectTokenConsumer(iterator),
            HashTokenConsumer(iterator),
            StringTokenConsumer(iterator),
            UrlTokenConsumer(iterator),
            NumberTokenConsumer(iterator),
            CommentTokenConsumer(iterator),
            DeepCombinatorTokenConsumer(iterator),
            FunctionTokenConsumer(iterator),
            IdentTokenConsumer(iterator),
            DelimTokenConsumer(iterator)
        ),
) : Tokenizer<CssTokenKind> {
    /**
     * Tokenizes the given CSS source code string into a list of tokens.
     *
     * @param input The CSS source code string to tokenize.
     * @return A list of tokens representing the CSS source code.
     */
    override fun tokenize(input: String): List<Token<out CssTokenKind>> =
        buildList {
            iterator.initialize(input)
            while (iterator.hasNext()) {
                val progressOffset = iterator.offset
                val kind = iterator.getTokenKind() ?: CssTokenKind.Ident
                val token =
                    consumers
                        .firstOrNull { consumer -> consumer.accept(kind) }
                        ?.consume(kind)
                        ?: error("Unsupported token kind: $kind at position ${iterator.offset}")

                check(iterator.offset > progressOffset) {
                    "CssTokenizer made no progress at offset $progressOffset on " +
                        "character '${input[progressOffset]}' (kind=$kind)"
                }

                addAll(token)
            }
            add(Token(CssTokenKind.EndOfFile, input.length, input.length))
        }
}
