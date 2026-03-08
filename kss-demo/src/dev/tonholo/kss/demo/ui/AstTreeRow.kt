package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tonholo.kss.demo.model.AstDisplayNode

private val IndentStep = 16.dp
private val NodeFontSize = 13.sp
private val CollapseToggleSize = 20.dp
private val CollapseToggleFontSize = 10.sp
private val HighlightColor = Color(0xFF264F78)

/**
 * A single row in the AST tree, showing the node label, summary, and an expand/collapse toggle.
 *
 * @param node The [AstDisplayNode] to render.
 * @param isHighlighted Whether this row is the cursor-matched node (accent background).
 * @param isCollapsed Whether this node's children are currently collapsed.
 * @param onToggleCollapse Called when the expand/collapse toggle is clicked.
 * @param onClick Called when the row itself is clicked (selects the CSS range).
 * @param modifier Optional [Modifier] applied to the row.
 */
@Composable
fun AstTreeRow(
    node: AstDisplayNode,
    isHighlighted: Boolean,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .let { if (isHighlighted) it.background(HighlightColor) else it }
                .clickable(onClick = onClick)
                .padding(start = IndentStep * node.depth, end = 8.dp)
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (node.hasChildren) {
            Text(
                text = if (isCollapsed) "\u25B6" else "\u25BC",
                modifier =
                    Modifier
                        .size(CollapseToggleSize)
                        .clickable(onClick = onToggleCollapse)
                        .padding(2.dp),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = CollapseToggleFontSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
            )
        } else {
            Spacer(modifier = Modifier.width(CollapseToggleSize))
        }

        Text(
            text = node.label,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = NodeFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = node.summary,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = NodeFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
