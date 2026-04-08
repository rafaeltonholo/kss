package dev.tonholo.kss.demo.model

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.parser.ast.css.CssSpecificity
import dev.tonholo.kss.parser.ast.css.syntax.node.AtRule
import dev.tonholo.kss.parser.ast.css.syntax.node.Comment
import dev.tonholo.kss.parser.ast.css.syntax.node.CssNode
import dev.tonholo.kss.parser.ast.css.syntax.node.Declaration
import dev.tonholo.kss.parser.ast.css.syntax.node.QualifiedRule
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import dev.tonholo.kss.parser.ast.css.syntax.node.SelectorListItem
import dev.tonholo.kss.parser.ast.css.syntax.node.StyleSheet
import dev.tonholo.kss.parser.ast.css.syntax.node.Value
import dev.tonholo.kss.parser.ast.css.syntax.node.typeName

private const val COMMENT_DETAIL_LENGTH = 80

/**
 * Extracts a list of key-value detail pairs from an [AstDisplayNode] for display
 * in the inline node detail panel.
 */
object NodeDetailExtractor {
    /**
     * Extracts metadata from the given [node] as a list of label-value pairs.
     *
     * @param node The display node (must have a non-null [AstDisplayNode.nodeRef]).
     * @param tokens The full token stream, used to compute token sequences within the node's range.
     * @return A list of (label, value) pairs to display.
     */
    fun extract(
        node: AstDisplayNode,
        tokens: List<Token<out CssTokenKind>>,
    ): List<Pair<String, String>> {
        val cssNode = node.nodeRef ?: return listOf("Error" to "No AST node reference")
        val details = extractNodeDetails(cssNode, node.label)

        details += "Source offsets" to "${cssNode.location.start}..${cssNode.location.end}"

        val tokenSeq = tokensInRange(tokens, node.cssRange)
        if (tokenSeq.isNotEmpty()) {
            details += "Token sequence" to tokenSeq
        }

        return details
    }

    private fun extractNodeDetails(
        cssNode: CssNode,
        label: String,
    ): MutableList<Pair<String, String>> =
        when (cssNode) {
            is StyleSheet -> extractStyleSheet(cssNode)
            is QualifiedRule -> extractQualifiedRule(cssNode)
            is AtRule -> extractAtRule(cssNode)
            is Declaration -> extractDeclaration(cssNode)
            is SelectorListItem -> extractSelectorListItem(cssNode)
            is Selector -> extractSelector(cssNode)
            is Value -> extractValue(cssNode)
            is Comment -> mutableListOf("Content" to cssNode.value.take(COMMENT_DETAIL_LENGTH))
            else -> mutableListOf("Type" to label)
        }

    private fun extractStyleSheet(node: StyleSheet) =
        mutableListOf(
            "Child count" to "${node.children.size}",
            "Source length" to "${node.location.end - node.location.start} chars"
        )

    private fun extractQualifiedRule(node: QualifiedRule): MutableList<Pair<String, String>> {
        val selectorText =
            node.prelude.components
                .joinToString(", ") { it.toString(indent = 0) }
        return mutableListOf(
            "Selector" to selectorText,
            "Declaration count" to "${node.block.children.size}"
        )
    }

    private fun extractAtRule(node: AtRule): MutableList<Pair<String, String>> {
        val preludeText =
            node.prelude.components
                .joinToString(" ") { it.toString(indent = 0) }
        return mutableListOf(
            "Name" to node.name,
            "Prelude" to preludeText,
            "Child count" to "${node.block.children.size}"
        )
    }

    private fun extractDeclaration(node: Declaration) =
        mutableListOf(
            "Property" to node.property,
            "Value" to node.values.joinToString(" ") { it.toString(indent = 0) },
            "Important" to "${node.important}"
        )

    private fun extractSelectorListItem(node: SelectorListItem) =
        mutableListOf(
            "Text" to node.selectors.joinToString("") { it.toString(indent = 0) },
            "Selector count" to "${node.selectors.size}"
        )

    private fun extractSelector(node: Selector): MutableList<Pair<String, String>> {
        val typeName = when (node) {
            is Selector.Type -> "Type"
            is Selector.Class -> "Class"
            is Selector.Id -> "Id"
            is Selector.PseudoClass -> "PseudoClass"
            is Selector.PseudoElement -> "PseudoElement"
            is Selector.Attribute -> "Attribute"
        }
        // Simplified specificity based on selector type. Does not handle edge cases
        // like universal selector (*) = (0,0,0), or functional pseudo-classes
        // (:where() = (0,0,0), :not()/:is() = max of arguments).
        // See CssSpecificity.calculateSpecificity() for the full algorithm.
        val specificity =
            when (node) {
                is Selector.Id -> CssSpecificity(a = 1, b = 0, c = 0)

                is Selector.Class,
                is Selector.Attribute,
                is Selector.PseudoClass,
                    -> CssSpecificity(a = 0, b = 1, c = 0)

                is Selector.Type,
                is Selector.PseudoElement,
                    -> CssSpecificity(a = 0, b = 0, c = 1)
            }
        val details = mutableListOf(
            "Type" to typeName,
            "Text" to node.toString(indent = 0),
            "Specificity" to "(${specificity.a}, ${specificity.b}, ${specificity.c})"
        )
        if (node is Selector.Attribute) {
            node.matcher?.let { details += "Matcher" to it }
            node.value?.let { details += "Attr value" to it }
        }
        return details
    }

    private fun extractValue(node: Value): MutableList<Pair<String, String>> {
        val details =
            mutableListOf(
                "Type" to node.typeName,
                "Raw value" to node.toString(indent = 0)
            )
        if (node is Value.Dimension) {
            details += "Unit" to node.unit
        }
        if (node is Value.Function) {
            details += "Name" to node.name
            details += "Arg count" to "${node.arguments.size}"
        }
        return details
    }

    private fun tokensInRange(
        tokens: List<Token<out CssTokenKind>>,
        range: IntRange,
    ): String {
        val filtered =
            tokens.filter { token ->
                // range is inclusive (IntRange), token.endOffset is exclusive, hence +1
                token.startOffset >= range.first && token.endOffset <= range.last + 1
            }
        if (filtered.isEmpty()) return ""
        return filtered
            .filter { it.kind != CssTokenKind.WhiteSpace && it.kind != CssTokenKind.EndOfFile }
            .joinToString(", ") { it.kind.name }
    }
}
