package dev.tonholo.kss.parser.ast.css

import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.lexer.css.CssTokenizer
import dev.tonholo.kss.parser.ast.css.consumer.CssConsumers
import dev.tonholo.kss.parser.ast.css.syntax.AstParserException
import dev.tonholo.kss.parser.ast.css.syntax.CssIterator
import dev.tonholo.kss.parser.ast.css.syntax.node.SelectorListItem

/**
 * Parses CSS selector strings directly into [SelectorListItem]s
 * without requiring them to be wrapped in a qualified rule.
 *
 * Usage:
 * ```kotlin
 * val parser = CssSelectorParser()
 * val items = parser.parse("div.active, #header > p")
 * ```
 */
class CssSelectorParser {
    /**
     * Parses [selector] into a list of [SelectorListItem].
     *
     * Comma-separated selector groups produce multiple items.
     * Returns an empty list for blank or unparseable input.
     *
     * @param selector A CSS selector string (e.g., `"div.active, #main"`).
     * @return The parsed selector list items, or an empty list if
     *         the input is blank or cannot be parsed.
     */
    fun parse(selector: String): List<SelectorListItem> {
        if (selector.isBlank()) return emptyList()
        val tokenizer = CssTokenizer()
        val tokens = tokenizer.tokenize(input = selector)
        val consumers = CssConsumers(content = selector)
        val iterator = CssIterator(tokens)
        val items = mutableListOf<SelectorListItem>()
        return try {
            consumeSelectors(iterator, consumers, items)
            items
        } catch (_: AstParserException) {
            emptyList()
        }
    }

    private fun consumeSelectors(
        iterator: CssIterator,
        consumers: CssConsumers,
        items: MutableList<SelectorListItem>,
    ) {
        while (iterator.hasNext()) {
            val token = iterator.next() ?: return
            when (token.kind) {
                CssTokenKind.EndOfFile -> {
                    return
                }

                CssTokenKind.WhiteSpace -> {
                    Unit
                }

                else -> {
                    items += consumers.selectorListItemConsumer.consume(iterator)
                }
            }
        }
    }
}
