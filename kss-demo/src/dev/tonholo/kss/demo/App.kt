package dev.tonholo.kss.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tonholo.kss.demo.state.AppViewModel
import dev.tonholo.kss.demo.ui.AstTreePanel
import dev.tonholo.kss.demo.ui.CssEditorPanel
import dev.tonholo.kss.demo.ui.SplitPane

/**
 * Root composable for the KSS Demo application.
 *
 * Renders a horizontal split-pane layout with a [CssEditorPanel] on the left
 * and an [AstTreePanel] on the right, separated by a draggable divider.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    val viewModel = viewModel { AppViewModel() }
    val state by viewModel.uiState.collectAsState()

    SplitPane(
        ratio = state.splitRatio,
        onRatioChange = viewModel::updateSplitRatio,
        modifier = modifier,
        leftContent = { panelModifier ->
            CssEditorPanel(
                state = state,
                onCssTextChange = viewModel::onCssTextChange,
                onCursorOffsetChange = viewModel::onCursorOffsetChange,
                onClearSelection = viewModel::clearSelection,
                onSearchQueryChange = viewModel::searchCss,
                onNavigateSearchUp = { viewModel.navigateSearchResult(forward = false) },
                onNavigateSearchDown = { viewModel.navigateSearchResult(forward = true) },
                onCloseSearch = viewModel::clearSearch,
                modifier = panelModifier,
            )
        },
        rightContent = { panelModifier ->
            AstTreePanel(
                state = state,
                onToggleCollapse = viewModel::toggleCollapse,
                onNodeClick = viewModel::onAstNodeClicked,
                modifier = panelModifier,
            )
        },
    )
}
