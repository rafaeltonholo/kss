package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ShortcutFontSize = 11.sp

/**
 * A small header bar for a panel, showing the panel name and an optional keyboard shortcut hint.
 *
 * @param title The panel name (e.g., "CSS Editor", "AST Tree").
 * @param modifier Optional [Modifier] applied to the root layout.
 * @param shortcutHint An optional keyboard shortcut hint (e.g., "Ctrl+F to search").
 */
@Composable
fun PanelHeader(
    title: String,
    modifier: Modifier = Modifier,
    shortcutHint: String = "",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
        )
        Spacer(modifier = Modifier.weight(1f))
        if (shortcutHint.isNotEmpty()) {
            Text(
                text = shortcutHint,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = ShortcutFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        }
    }
}
