package dev.tonholo.kss.demo.model

import dev.tonholo.kss.parser.ast.css.syntax.node.CssNode

/**
 * A flattened representation of a CSS AST node for display in a [LazyColumn][androidx.compose.foundation.lazy.LazyColumn].
 *
 * @property id Unique identifier used for collapse/expand tracking and list keys.
 * @property depth Nesting level in the original AST (0 = root [StyleSheet][dev.tonholo.kss.parser.ast.css.syntax.node.StyleSheet]).
 * @property label The AST node type name (e.g. "QualifiedRule", "Declaration").
 * @property summary A short human-readable description (e.g. selector text, property-value pair).
 * @property cssRange The character offset range in the original CSS source that this node spans.
 * @property hasChildren Whether this node has child nodes that can be expanded/collapsed.
 * @property nodeRef Reference to the original [CssNode] for detail extraction. Note: participates in
 *   `equals`/`hashCode` as a data class property. This is acceptable since nodes are recreated on every parse.
 */
data class AstDisplayNode(
    val id: Int,
    val depth: Int,
    val label: String,
    val summary: String,
    val cssRange: IntRange,
    val hasChildren: Boolean,
    val nodeRef: CssNode? = null,
)
