package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tonholo.kss.demo.state.UiState

private val EditorFontSize = 14.sp
private val LineNumberWidth = 48.dp
private const val LINE_HEIGHT_MULTIPLIER = 1.5f

/**
 * Left panel of the KSS Demo: a syntax-highlighted CSS text editor with line numbers.
 *
 * Supports bidirectional synchronization with the AST tree panel: cursor movements update
 * the highlighted AST node, and clicking an AST node selects the corresponding CSS range.
 * Parse/tokenize errors are visually underlined at the offending offset.
 *
 * @param state The [UiState] driving the editor content and error information.
 * @param onCssTextChange Callback when the CSS text changes.
 * @param onCursorOffsetChange Callback when the cursor position changes.
 * @param onClearSelection Callback to clear the AST-driven selection.
 * @param onSearchQueryChange Callback when the search query text changes.
 * @param onNavigateSearchUp Callback to navigate to the previous search match.
 * @param onNavigateSearchDown Callback to navigate to the next search match.
 * @param onCloseSearch Callback to close the search bar and clear the search.
 * @param modifier Optional [Modifier] applied to the root layout.
 */
@Composable
fun CssEditorPanel(
    state: UiState,
    onCssTextChange: (String) -> Unit,
    onCursorOffsetChange: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateSearchUp: () -> Unit,
    onNavigateSearchDown: () -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var textFieldValue by remember(state.cssText) {
        mutableStateOf(
            TextFieldValue(
                text = state.cssText,
                selection = TextRange(state.cursorOffset.coerceIn(0, state.cssText.length))
            )
        )
    }

    LaunchedEffect(state.selectedCssRange) {
        val range = state.selectedCssRange ?: return@LaunchedEffect
        val start = range.first.coerceIn(0, state.cssText.length)
        val end = (range.last + 1).coerceIn(start, state.cssText.length)
        textFieldValue =
            textFieldValue.copy(
                selection = TextRange(start, end)
            )
    }

    val errorRange = state.errorInfo?.errorRange
    val highlightedText =
        remember(state.cssText, state.tokens, errorRange, state.editorSearchMatches, state.editorSearchCurrentIndex) {
            SyntaxHighlighter.highlight(
                text = state.cssText,
                tokens = state.tokens,
                errorRange = errorRange,
                searchMatches = state.editorSearchMatches,
                currentMatchIndex = state.editorSearchCurrentIndex
            )
        }

    val visualTransformation =
        remember(highlightedText) {
            VisualTransformation { _ ->
                androidx.compose.ui.text.input.TransformedText(
                    highlightedText,
                    androidx.compose.ui.text.input.OffsetMapping.Identity
                )
            }
        }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val lineCount = remember(state.cssText) { state.cssText.count { it == '\n' } + 1 }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
    ) {
        PanelHeader(title = "CSS Editor", shortcutHint = "Ctrl+F search")

        Box(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxSize()) {
                LineNumberGutter(lineCount = lineCount, verticalScroll = verticalScroll)
                EditorTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onCssTextChange(newValue.text)
                        onCursorOffsetChange(newValue.selection.start)
                        onClearSelection()
                    },
                    visualTransformation = visualTransformation,
                    verticalScroll = verticalScroll,
                    horizontalScroll = horizontalScroll
                )
            }

            if (state.editorSearchVisible) {
                EditorSearchBar(
                    query = state.editorSearchQuery,
                    onQueryChange = onSearchQueryChange,
                    matchCount = state.editorSearchMatches.size,
                    currentMatchIndex = state.editorSearchCurrentIndex,
                    onNavigateUp = onNavigateSearchUp,
                    onNavigateDown = onNavigateSearchDown,
                    onClose = onCloseSearch,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LineNumberGutter(
    lineCount: Int,
    verticalScroll: ScrollState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .widthIn(min = LineNumberWidth)
                .fillMaxHeight()
                .verticalScroll(verticalScroll)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = (1..lineCount).joinToString("\n"),
            style =
                TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = EditorFontSize,
                    lineHeight = EditorFontSize * LINE_HEIGHT_MULTIPLIER,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        )
    }
}

@Composable
private fun EditorTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    visualTransformation: VisualTransformation,
    verticalScroll: ScrollState,
    horizontalScroll: ScrollState,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxHeight()
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll)
                .padding(4.dp),
        textStyle =
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = EditorFontSize,
                lineHeight = EditorFontSize * LINE_HEIGHT_MULTIPLIER,
                color = MaterialTheme.colorScheme.onBackground
            ),
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}
