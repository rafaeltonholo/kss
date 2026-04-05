package dev.tonholo.kss.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tonholo.kss.demo.state.AppViewModel
import dev.tonholo.kss.demo.ui.AstTreePanel
import dev.tonholo.kss.demo.ui.CssEditorPanel
import dev.tonholo.kss.demo.ui.KeyAction
import dev.tonholo.kss.demo.ui.SplitPane
import dev.tonholo.kss.demo.ui.handleKeyEvent
import dev.tonholo.kss.demo.ui.isMacOs

/**
 * Root composable for the KSS Demo application.
 *
 * Renders a horizontal split-pane layout with a [CssEditorPanel] on the left
 * and an [AstTreePanel] on the right, separated by a draggable divider.
 */
@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel { AppViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val macOs = remember { isMacOs() }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    val action = handleKeyEvent(event, macOs) ?: return@onPreviewKeyEvent false
                    when (action) {
                        KeyAction.ToggleEditorSearch -> {
                            viewModel.toggleEditorSearch()
                            true
                        }

                        KeyAction.ToggleAstFilter -> {
                            viewModel.toggleAstFilter()
                            true
                        }

                        KeyAction.NextSearchMatch -> {
                            if (state.editorSearchVisible) {
                                viewModel.navigateSearchResult(forward = true)
                                true
                            } else {
                                false
                            }
                        }

                        KeyAction.PreviousSearchMatch -> {
                            if (state.editorSearchVisible) {
                                viewModel.navigateSearchResult(forward = false)
                                true
                            } else {
                                false
                            }
                        }

                        KeyAction.Escape -> {
                            when {
                                state.editorSearchVisible -> {
                                    viewModel.clearSearch()
                                    true
                                }

                                state.astFilterVisible -> {
                                    viewModel.clearAstFilter()
                                    true
                                }

                                else -> {
                                    false
                                }
                            }
                        }
                    }
                }
    ) {
        SplitPane(
            ratio = state.splitRatio,
            onRatioChange = viewModel::updateSplitRatio,
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
                    modifier = panelModifier
                )
            },
            rightContent = { panelModifier ->
                AstTreePanel(
                    state = state,
                    onToggleCollapse = viewModel::toggleCollapse,
                    onNodeClick = viewModel::onAstNodeClicked,
                    onFilterQueryChange = viewModel::searchAstTree,
                    onCloseFilter = viewModel::clearAstFilter,
                    onToggleNodeDetails = viewModel::toggleNodeDetails,
                    modifier = panelModifier
                )
            }
        )
    }
}
