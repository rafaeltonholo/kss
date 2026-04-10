package dev.tonholo.kss.parser.ast.css.consumer

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.parser.ast.css.syntax.CssIterator
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectorListItemConsumerTest {
    @Test
    fun `given type selector with EndOfFile - when consume is called - then returns SelectorListItem`() {
        // Arrange
        val content = "div"
        val tokens = listOf(
            Token(kind = CssTokenKind.Ident, startOffset = 0, endOffset = 3),
            Token(kind = CssTokenKind.EndOfFile, startOffset = 3, endOffset = 3)
        )
        val iterator = CssIterator(tokens)
        iterator.next() // advance to first token
        val selectorConsumer = SimpleSelectorConsumer(content)
        val consumer = SelectorListItemConsumer(
            content = content,
            simpleSelectorConsumer = selectorConsumer
        )

        // Act
        val result = consumer.consume(iterator)

        // Assert
        assertEquals(expected = 1, actual = result.selectors.size)
        val selector = result.selectors.first()
        check(selector is Selector.Type)
        assertEquals(expected = "div", actual = selector.name)
    }

    @Test
    fun `given id selector with EndOfFile - when consume is called - then returns SelectorListItem`() {
        // Arrange
        val content = "#my-id"
        val tokens = listOf(
            Token(kind = CssTokenKind.Hash, startOffset = 0, endOffset = 1),
            Token(kind = CssTokenKind.Ident, startOffset = 1, endOffset = 6),
            Token(kind = CssTokenKind.EndOfFile, startOffset = 6, endOffset = 6)
        )
        val iterator = CssIterator(tokens)
        iterator.next() // advance to first token
        val selectorConsumer = SimpleSelectorConsumer(content)
        val consumer = SelectorListItemConsumer(
            content = content,
            simpleSelectorConsumer = selectorConsumer
        )

        // Act
        val result = consumer.consume(iterator)

        // Assert
        assertEquals(expected = 1, actual = result.selectors.size)
        val selector = result.selectors.first()
        check(selector is Selector.Id)
        assertEquals(expected = "my-id", actual = selector.name)
    }

    @Test
    fun `given compound selector with EndOfFile - when consume is called - then returns all selectors`() {
        // Arrange
        val content = "div.active"
        val tokens = listOf(
            Token(kind = CssTokenKind.Ident, startOffset = 0, endOffset = 3),
            Token(kind = CssTokenKind.Dot, startOffset = 3, endOffset = 4),
            Token(kind = CssTokenKind.Ident, startOffset = 4, endOffset = 10),
            Token(kind = CssTokenKind.EndOfFile, startOffset = 10, endOffset = 10)
        )
        val iterator = CssIterator(tokens)
        iterator.next() // advance to first token
        val selectorConsumer = SimpleSelectorConsumer(content)
        val consumer = SelectorListItemConsumer(
            content = content,
            simpleSelectorConsumer = selectorConsumer
        )

        // Act
        val result = consumer.consume(iterator)

        // Assert
        assertEquals(expected = 2, actual = result.selectors.size)
        val type = result.selectors[0]
        check(type is Selector.Type)
        assertEquals(expected = "div", actual = type.name)
        val cls = result.selectors[1]
        check(cls is Selector.Class)
        assertEquals(expected = "active", actual = cls.name)
    }
}
