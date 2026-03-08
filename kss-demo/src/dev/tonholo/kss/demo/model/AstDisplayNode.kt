package dev.tonholo.kss.demo.model

/**
 * A flattened representation of a CSS AST node for display in a [LazyColumn][androidx.compose.foundation.lazy.LazyColumn].
 *
 * The CSS AST is a tree structure, but [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] requires a flat list.
 * Each [AstDisplayNode] carries enough information to render an indented, collapsible tree row.
 *
 * @property id Unique identifier used for collapse/expand tracking and list keys.
 * @property depth Nesting level in the original AST (0 = root [StyleSheet][dev.tonholo.kss.parser.ast.css.syntax.node.StyleSheet]).
 * @property label The AST node type name (e.g. "QualifiedRule", "Declaration").
 * @property summary A short human-readable description (e.g. selector text, property–value pair).
 * @property cssRange The character offset range in the original CSS source that this node spans.
 * @property hasChildren Whether this node has child nodes that can be expanded/collapsed.
 */
data class AstDisplayNode(
    val id: Int,
    val depth: Int,
    val label: String,
    val summary: String,
    val cssRange: IntRange,
    val hasChildren: Boolean,
)
