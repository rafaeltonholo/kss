package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FilterFontSize = 13.sp
private val IconSize = 24.dp

/**
 * An integrated filter bar for the AST tree panel.
 *
 * @param query The current filter query text.
 * @param onQueryChange Called when the user types in the filter input.
 * @param matchCount Number of matching nodes.
 * @param onClose Called when the user closes the filter bar.
 * @param modifier Optional [Modifier].
 */
@Composable
fun AstFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search icon
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Filter",
            modifier = Modifier.size(IconSize).padding(2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle =
                TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = FilterFontSize,
                    color = MaterialTheme.colorScheme.onSurface
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )

        if (query.isNotEmpty()) {
            Text(
                text = "$matchCount matches",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = FilterFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        }

        // Close button
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close filter",
            modifier =
                Modifier
                    .size(IconSize)
                    .clickable(onClick = onClose)
                    .padding(4.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
