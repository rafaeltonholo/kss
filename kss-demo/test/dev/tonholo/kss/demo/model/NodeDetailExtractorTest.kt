package dev.tonholo.kss.demo.model

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.lexer.css.CssTokenizer
import dev.tonholo.kss.parser.ast.css.CssParser
import dev.tonholo.kss.parser.ast.css.consumer.CssConsumers
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeDetailExtractorTest {

    private fun parseAndFlatten(css: String): Pair<List<AstDisplayNode>, List<Token<out CssTokenKind>>> {
        val tokens = CssTokenizer().tokenize(css)
        val consumers = CssConsumers(css)
        val styleSheet = CssParser(consumers).parse(tokens)
        val nodes = AstFlattener.flatten(styleSheet)
        return nodes to tokens
    }

    @Test
    fun `extracts details for Declaration node`() {
        val css = "body { color: red; }"
        val (nodes, tokens) = parseAndFlatten(css)
        val declNode = nodes.first { it.label == "Declaration" }
        val details = NodeDetailExtractor.extract(declNode, tokens)

        assertTrue(details.any { it.first == "Property" && it.second == "color" })
        assertTrue(details.any { it.first == "Value" && it.second.contains("red") })
        assertTrue(details.any { it.first == "Important" && it.second == "false" })
        assertTrue(details.any { it.first == "Source offsets" })
    }

    @Test
    fun `extracts details for Selector node with specificity`() {
        val css = ".container { margin: 0; }"
        val (nodes, tokens) = parseAndFlatten(css)
        val selectorNode = nodes.first { it.label == "Class" }
        val details = NodeDetailExtractor.extract(selectorNode, tokens)

        assertTrue(details.any { it.first == "Type" && it.second == "Class" })
        assertTrue(details.any { it.first == "Text" })
        assertTrue(details.any { it.first == "Specificity" && it.second == "(0, 1, 0)" })
        assertTrue(details.any { it.first == "Source offsets" })
    }

    @Test
    fun `extracts details for StyleSheet node`() {
        val css = "body { color: red; } p { margin: 0; }"
        val (nodes, tokens) = parseAndFlatten(css)
        val rootNode = nodes.first { it.label == "StyleSheet" }
        val details = NodeDetailExtractor.extract(rootNode, tokens)

        assertTrue(details.any { it.first == "Child count" })
        assertTrue(details.any { it.first == "Source length" })
    }

    @Test
    fun `extracts token sequence for Declaration`() {
        val css = "body { color: red; }"
        val (nodes, tokens) = parseAndFlatten(css)
        val declNode = nodes.first { it.label == "Declaration" }
        val details = NodeDetailExtractor.extract(declNode, tokens)

        assertTrue(details.any { it.first == "Token sequence" })
        val tokenSeq = details.first { it.first == "Token sequence" }.second
        assertTrue(tokenSeq.isNotEmpty())
    }

    @Test
    fun `extracts details for Value Dimension node`() {
        val css = "body { margin: 16px; }"
        val (nodes, tokens) = parseAndFlatten(css)
        val dimNode = nodes.first { it.label == "Dimension" }
        val details = NodeDetailExtractor.extract(dimNode, tokens)

        assertTrue(details.any { it.first == "Type" && it.second == "Dimension" })
        assertTrue(details.any { it.first == "Unit" })
    }

    @Test
    fun `extracts details for AtRule`() {
        val css = "@media (max-width: 768px) { body { margin: 0; } }"
        val (nodes, tokens) = parseAndFlatten(css)
        val atRule = nodes.first { it.label == "AtRule" }
        val details = NodeDetailExtractor.extract(atRule, tokens)

        assertTrue(details.any { it.first == "Name" && it.second == "@media" })
        assertTrue(details.any { it.first == "Prelude" })
        assertTrue(details.any { it.first == "Source offsets" })
    }

    @Test
    fun `extracts details for Comment node`() {
        // The CSS parser skips comment tokens (CssIterator filters them out),
        // so we can't get a Comment AST node from parseAndFlatten.
        // Instead, test NodeDetailExtractor directly with a manually constructed node.
        val commentNode = AstDisplayNode(
            id = 99,
            depth = 0,
            label = "Comment",
            summary = "This is a comment",
            cssRange = 0..21,
            hasChildren = false,
            nodeRef = dev.tonholo.kss.parser.ast.css.syntax.node.Comment(
                value = " This is a comment ",
                location = dev.tonholo.kss.parser.ast.css.syntax.node.CssLocation(
                    source = "/* This is a comment */",
                    start = 0,
                    end = 22,
                ),
            ),
        )
        val details = NodeDetailExtractor.extract(commentNode, emptyList())

        assertTrue(details.any { it.first == "Content" })
        assertTrue(details.any { it.first == "Source offsets" })
    }
}
