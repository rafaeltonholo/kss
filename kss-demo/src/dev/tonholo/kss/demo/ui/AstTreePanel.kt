package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tonholo.kss.demo.model.AstDisplayNode
import dev.tonholo.kss.demo.state.AppState

private val ErrorFontSize = 12.sp
private const val ERROR_BANNER_ALPHA = 0.15f
private const val ERROR_MAX_LINES = 3

/**
 * Right panel of the KSS Demo: an interactive, collapsible AST tree viewer.
 *
 * Displays the flattened CSS AST nodes in a [LazyColumn][androidx.compose.foundation.lazy.LazyColumn],
 * auto-scrolls to the node matching the current editor cursor, and shows parse errors in a banner.
 *
 * @param state The shared [AppState] providing the visible AST nodes and error information.
 * @param modifier Optional [Modifier] applied to the root layout.
 */
@Composable
fun AstTreePanel(
    state: AppState,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val highlightedIndex = state.highlightedNodeIndex

    LaunchedEffect(highlightedIndex) {
        val index = highlightedIndex ?: return@LaunchedEffect
        lazyListState.animateScrollToItem(index)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
    ) {
        val error = state.parseError
        if (error != null) {
            ParseErrorBanner(error = error)
        }

        val nodes = state.visibleNodes
        if (nodes.isEmpty() && error == null) {
            EmptyAstPlaceholder()
        } else {
            AstNodeList(
                nodes = nodes,
                highlightedIndex = highlightedIndex,
                collapsedNodeIds = state.collapsedNodeIds,
                onToggleCollapse = state::toggleCollapse,
                onNodeClick = state::onAstNodeClicked,
                lazyListState = lazyListState
            )
        }
    }
}

@Composable
private fun ParseErrorBanner(
    error: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error.copy(alpha = ERROR_BANNER_ALPHA))
                .padding(8.dp)
    ) {
        Text(
            text = "Parse error: $error",
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = ErrorFontSize,
                    color = MaterialTheme.colorScheme.error
                ),
            maxLines = ERROR_MAX_LINES
        )
    }
}

@Composable
private fun EmptyAstPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Type CSS in the editor to see the AST",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AstNodeList(
    nodes: List<AstDisplayNode>,
    highlightedIndex: Int?,
    collapsedNodeIds: Set<Int>,
    onToggleCollapse: (Int) -> Unit,
    onNodeClick: (AstDisplayNode) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = nodes,
            key = { _, node -> node.id }
        ) { index, node ->
            AstTreeRow(
                node = node,
                isHighlighted = index == highlightedIndex,
                isCollapsed = node.id in collapsedNodeIds,
                onToggleCollapse = { onToggleCollapse(node.id) },
                onClick = { onNodeClick(node) }
            )
        }
    }
}
