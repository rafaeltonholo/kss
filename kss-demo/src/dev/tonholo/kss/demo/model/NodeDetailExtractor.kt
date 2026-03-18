package dev.tonholo.kss.demo.model

import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.parser.ast.css.CssSpecificity
import dev.tonholo.kss.parser.ast.css.syntax.node.AtRule
import dev.tonholo.kss.parser.ast.css.syntax.node.Comment
import dev.tonholo.kss.parser.ast.css.syntax.node.Declaration
import dev.tonholo.kss.parser.ast.css.syntax.node.QualifiedRule
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import dev.tonholo.kss.parser.ast.css.syntax.node.SelectorListItem
import dev.tonholo.kss.parser.ast.css.syntax.node.StyleSheet
import dev.tonholo.kss.parser.ast.css.syntax.node.Value

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
        val details = mutableListOf<Pair<String, String>>()

        when (cssNode) {
            is StyleSheet -> {
                details += "Child count" to "${cssNode.children.size}"
                details += "Source length" to "${cssNode.location.end - cssNode.location.start} chars"
            }

            is QualifiedRule -> {
                val selectorText = cssNode.prelude.components
                    .joinToString(", ") { it.toString(indent = 0) }
                details += "Selector" to selectorText
                val declCount = cssNode.block.children.size
                details += "Declaration count" to "$declCount"
            }

            is AtRule -> {
                details += "Name" to cssNode.name
                val preludeText = cssNode.prelude.components
                    .joinToString(" ") { it.toString(indent = 0) }
                details += "Prelude" to preludeText
                details += "Child count" to "${cssNode.block.children.size}"
            }

            is Declaration -> {
                details += "Property" to cssNode.property
                val valuesText = cssNode.values.joinToString(" ") { it.toString(indent = 0) }
                details += "Value" to valuesText
                details += "Important" to "${cssNode.important}"
            }

            is SelectorListItem -> {
                val text = cssNode.selectors.joinToString("") { it.toString(indent = 0) }
                details += "Text" to text
                details += "Selector count" to "${cssNode.selectors.size}"
            }

            is Selector -> {
                val typeName = when (cssNode) {
                    is Selector.Type -> "Type"
                    is Selector.Class -> "Class"
                    is Selector.Id -> "Id"
                    is Selector.PseudoClass -> "PseudoClass"
                    is Selector.PseudoElement -> "PseudoElement"
                    is Selector.Attribute -> "Attribute"
                }
                details += "Type" to typeName
                details += "Text" to cssNode.toString(indent = 0)
                // Compute specificity based on selector type
                val specificity = when (cssNode) {
                    is Selector.Id -> CssSpecificity(a = 1, b = 0, c = 0)
                    is Selector.Class,
                    is Selector.Attribute,
                    is Selector.PseudoClass -> CssSpecificity(a = 0, b = 1, c = 0)
                    is Selector.Type,
                    is Selector.PseudoElement -> CssSpecificity(a = 0, b = 0, c = 1)
                }
                details += "Specificity" to "(${specificity.a}, ${specificity.b}, ${specificity.c})"
                if (cssNode is Selector.Attribute) {
                    cssNode.matcher?.let { details += "Matcher" to it }
                    cssNode.value?.let { details += "Attr value" to it }
                }
            }

            is Value -> {
                val typeName = when (cssNode) {
                    is Value.Color -> "Color"
                    is Value.String -> "String"
                    is Value.Identifier -> "Identifier"
                    is Value.Number -> "Number"
                    is Value.Dimension -> "Dimension"
                    is Value.Percentage -> "Percentage"
                    is Value.Function -> "Function"
                    is Value.Url -> "Url"
                }
                details += "Type" to typeName
                details += "Raw value" to cssNode.toString(indent = 0)
                if (cssNode is Value.Dimension) {
                    details += "Unit" to cssNode.unit
                }
                if (cssNode is Value.Function) {
                    details += "Name" to cssNode.name
                    details += "Arg count" to "${cssNode.arguments.size}"
                }
            }

            is Comment -> {
                details += "Content" to cssNode.value.take(COMMENT_DETAIL_LENGTH)
            }

            else -> {
                details += "Type" to node.label
            }
        }

        // Common fields
        details += "Source offsets" to "${cssNode.location.start}..${cssNode.location.end}"

        // Token sequence (for Declaration and other leaf-ish nodes)
        val tokenSeq = tokensInRange(tokens, node.cssRange)
        if (tokenSeq.isNotEmpty()) {
            details += "Token sequence" to tokenSeq
        }

        return details
    }

    private fun tokensInRange(
        tokens: List<Token<out CssTokenKind>>,
        range: IntRange,
    ): String {
        val filtered = tokens.filter { token ->
            token.startOffset >= range.first && token.endOffset <= range.last + 1
        }
        if (filtered.isEmpty()) return ""
        return filtered
            .filter { it.kind != CssTokenKind.WhiteSpace && it.kind != CssTokenKind.EndOfFile }
            .joinToString(", ") { it.kind.name }
    }
}
