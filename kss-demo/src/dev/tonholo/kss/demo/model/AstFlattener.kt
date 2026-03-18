package dev.tonholo.kss.demo.model

import dev.tonholo.kss.parser.ast.css.syntax.node.AtRule
import dev.tonholo.kss.parser.ast.css.syntax.node.AtRulePrelude
import dev.tonholo.kss.parser.ast.css.syntax.node.Block
import dev.tonholo.kss.parser.ast.css.syntax.node.Comment
import dev.tonholo.kss.parser.ast.css.syntax.node.CssNode
import dev.tonholo.kss.parser.ast.css.syntax.node.Declaration
import dev.tonholo.kss.parser.ast.css.syntax.node.QualifiedRule
import dev.tonholo.kss.parser.ast.css.syntax.node.Selector
import dev.tonholo.kss.parser.ast.css.syntax.node.SelectorListItem
import dev.tonholo.kss.parser.ast.css.syntax.node.StyleSheet
import dev.tonholo.kss.parser.ast.css.syntax.node.Value

private const val COMMENT_PREVIEW_LENGTH = 40

/**
 * Converts a CSS [StyleSheet] tree into a flat list of [AstDisplayNode]s suitable for
 * rendering in a [LazyColumn][androidx.compose.foundation.lazy.LazyColumn].
 *
 * The walker performs a depth-first traversal of the AST, emitting one [AstDisplayNode]
 * per visited node with the appropriate depth, label, summary, and source range.
 */
object AstFlattener {
    /**
     * Flattens the given [styleSheet] into an ordered list of display nodes.
     *
     * @param styleSheet The root AST node to flatten, or `null` to produce an empty list.
     * @return A flat, depth-first ordered list of [AstDisplayNode]s.
     */
    fun flatten(styleSheet: StyleSheet?): List<AstDisplayNode> {
        if (styleSheet == null) return emptyList()
        val walker = AstWalker()
        walker.walkNode(styleSheet, depth = 0)
        return walker.nodes
    }
}

private class AstWalker {
    val nodes = mutableListOf<AstDisplayNode>()
    private var nextId = 0

    private fun emit(
        depth: Int,
        label: String,
        summary: String,
        cssRange: IntRange,
        hasChildren: Boolean,
        nodeRef: CssNode,
    ) {
        nodes +=
            AstDisplayNode(
                id = nextId++,
                depth = depth,
                label = label,
                summary = summary,
                cssRange = cssRange,
                hasChildren = hasChildren,
                nodeRef = nodeRef,
            )
    }

    fun walkNode(
        node: CssNode,
        depth: Int,
    ) {
        when (node) {
            is StyleSheet -> visitStyleSheet(node, depth)

            is QualifiedRule -> visitQualifiedRule(node, depth)

            is AtRule -> visitAtRule(node, depth)

            is Declaration -> visitDeclaration(node, depth)

            is SelectorListItem -> visitSelectorListItem(node, depth)

            is Selector -> visitSelector(node, depth)

            is Value -> visitValue(node, depth)

            is Comment -> visitComment(node, depth)

            is AtRulePrelude -> visitAtRulePrelude(node, depth)

            is Block.SimpleBlock<*> -> walkBlock(node, depth)

            is Block.EmptyRuleBlock,
            is Block.EmptyDeclarationBlock,
                -> Unit
        }
    }

    fun walkBlock(
        block: Block<out CssNode>,
        depth: Int,
    ) {
        block.children.forEach { walkNode(it, depth) }
    }

    private fun visitStyleSheet(
        node: StyleSheet,
        depth: Int,
    ) {
        emit(
            depth = depth,
            label = "StyleSheet",
            summary = "${node.children.size} children",
            cssRange = node.location.start..node.location.end,
            hasChildren = node.children.isNotEmpty(),
            nodeRef = node,
        )
        node.children.forEach { walkNode(it, depth + 1) }
    }

    private fun visitQualifiedRule(
        node: QualifiedRule,
        depth: Int,
    ) {
        val selectorText =
            node.prelude.components
                .joinToString(", ") { it.toString(indent = 0) }
        emit(
            depth = depth,
            label = "QualifiedRule",
            summary = selectorText,
            cssRange = node.location.start..node.location.end,
            hasChildren = true,
            nodeRef = node,
        )
        node.prelude.components.forEach { walkNode(it, depth + 1) }
        walkBlock(node.block, depth + 1)
    }

    private fun visitAtRule(
        node: AtRule,
        depth: Int,
    ) {
        val preludeText =
            node.prelude.components
                .joinToString(" ") { it.toString(indent = 0) }
        emit(
            depth = depth,
            label = "AtRule",
            summary = "@${node.name} $preludeText",
            cssRange = node.location.start..node.location.end,
            hasChildren = true,
            nodeRef = node,
        )
        walkBlock(node.block, depth + 1)
    }

    private fun visitDeclaration(
        node: Declaration,
        depth: Int,
    ) {
        val valuesText =
            node.values
                .joinToString(" ") { it.toString(indent = 0) }
        val important = if (node.important) " !important" else ""
        emit(
            depth = depth,
            label = "Declaration",
            summary = "${node.property}: $valuesText$important",
            cssRange = node.location.start..node.location.end,
            hasChildren = node.values.isNotEmpty(),
            nodeRef = node,
        )
        node.values.forEach { walkNode(it, depth + 1) }
    }

    private fun visitSelectorListItem(
        node: SelectorListItem,
        depth: Int,
    ) {
        val text =
            node.selectors
                .joinToString("") { it.toString(indent = 0) }
        emit(
            depth = depth,
            label = "SelectorList",
            summary = text,
            cssRange = node.location.start..node.location.end,
            hasChildren = node.selectors.isNotEmpty(),
            nodeRef = node,
        )
        node.selectors.forEach { walkNode(it, depth + 1) }
    }

    private fun visitSelector(
        node: Selector,
        depth: Int,
    ) {
        val prefix =
            when (node) {
                is Selector.Type -> "Type"
                is Selector.Class -> "Class"
                is Selector.Id -> "Id"
                is Selector.PseudoClass -> "PseudoClass"
                is Selector.PseudoElement -> "PseudoElement"
                is Selector.Attribute -> "Attribute"
            }
        emit(
            depth = depth,
            label = prefix,
            summary = node.toString(indent = 0),
            cssRange = node.location.start..node.location.end,
            hasChildren = false,
            nodeRef = node,
        )
    }

    private fun visitValue(
        node: Value,
        depth: Int,
    ) {
        val prefix =
            when (node) {
                is Value.Color -> "Color"
                is Value.String -> "String"
                is Value.Identifier -> "Identifier"
                is Value.Number -> "Number"
                is Value.Dimension -> "Dimension"
                is Value.Percentage -> "Percentage"
                is Value.Function -> "Function"
                is Value.Url -> "Url"
            }
        val hasKids = node is Value.Function && node.arguments.isNotEmpty()
        emit(
            depth = depth,
            label = prefix,
            summary = node.toString(indent = 0),
            cssRange = node.location.start..node.location.end,
            hasChildren = hasKids,
            nodeRef = node,
        )
        if (node is Value.Function) {
            node.arguments.forEach { walkNode(it, depth + 1) }
        }
    }

    private fun visitComment(
        node: Comment,
        depth: Int,
    ) {
        emit(
            depth = depth,
            label = "Comment",
            summary = node.value.take(COMMENT_PREVIEW_LENGTH),
            cssRange = node.location.start..node.location.end,
            hasChildren = false,
            nodeRef = node,
        )
    }

    private fun visitAtRulePrelude(
        node: AtRulePrelude,
        depth: Int,
    ) {
        emit(
            depth = depth,
            label = "AtRulePrelude",
            summary = node.value,
            cssRange = node.location.start..node.location.end,
            hasChildren = false,
            nodeRef = node,
        )
    }
}
