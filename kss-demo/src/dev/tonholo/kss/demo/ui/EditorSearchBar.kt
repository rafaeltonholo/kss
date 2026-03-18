package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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

private val SearchBarFontSize = 13.sp
private val ButtonSize = 24.dp

@Composable
fun EditorSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatchIndex: Int,
    onNavigateUp: () -> Unit,
    onNavigateDown: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(max = 320.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = SearchBarFontSize,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        )

        val counterText = if (matchCount > 0) {
            "${currentMatchIndex + 1} of $matchCount"
        } else if (query.isNotEmpty()) {
            "No results"
        } else {
            ""
        }
        if (counterText.isNotEmpty()) {
            Text(
                text = counterText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = SearchBarFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        // Up button
        Text(
            text = "\u25B2",
            modifier = Modifier
                .size(ButtonSize)
                .clickable(onClick = onNavigateUp)
                .padding(4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )

        // Down button
        Text(
            text = "\u25BC",
            modifier = Modifier
                .size(ButtonSize)
                .clickable(onClick = onNavigateDown)
                .padding(4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )

        // Close button
        Text(
            text = "\u2715",
            modifier = Modifier
                .size(ButtonSize)
                .clickable(onClick = onClose)
                .padding(4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}
