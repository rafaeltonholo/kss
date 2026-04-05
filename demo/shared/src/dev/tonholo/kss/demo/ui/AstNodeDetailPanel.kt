package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DetailBackground = Color(0xFF1A3050)
private val DetailFontSize = 12.sp
private val DetailLabelWidth = 120.dp
private val AccentBorderWidth = 2.dp

/**
 * An inline detail panel that displays metadata about an AST node.
 *
 * @param details A list of (label, value) pairs to display.
 * @param indentDp The horizontal indent to align with the parent row's content.
 * @param modifier Optional [Modifier].
 */
@Composable
fun AstNodeDetailPanel(
    details: List<Pair<String, String>>,
    indentDp: Int,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = indentDp.dp)
                .drawBehind {
                    // Left border accent
                    drawLine(
                        color = accentColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = AccentBorderWidth.toPx()
                    )
                }.background(DetailBackground)
                .padding(start = AccentBorderWidth + 8.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Text(
            text = "NODE DETAILS",
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
        )
        Spacer(modifier = Modifier.height(4.dp))

        for ((label, value) in details) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.width(DetailLabelWidth),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = DetailFontSize,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                )
                Text(
                    text = value,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = DetailFontSize,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                )
            }
        }
    }
}
