package dev.tonholo.kss.demo.state

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tonholo.kss.demo.model.AstDisplayNode
import dev.tonholo.kss.demo.model.AstFlattener
import dev.tonholo.kss.demo.model.ParseErrorInfo
import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind
import dev.tonholo.kss.lexer.css.CssTokenizer
import dev.tonholo.kss.parser.ast.css.CssParser
import dev.tonholo.kss.parser.ast.css.consumer.CssConsumers
import dev.tonholo.kss.parser.ast.css.syntax.node.StyleSheet
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Central state holder for the KSS Demo application.
 *
 * Manages the CSS editor text, tokenization, parsing, AST flattening, and bidirectional
 * synchronization between the editor cursor and the AST tree viewer. Uses a ViewModel
 * with SharedFlow-based intent processing to drive UI state.
 */
class AppViewModel : ViewModel() {
    private val intents = MutableSharedFlow<Intent>(extraBufferCapacity = 64)

    private val initialState: UiState = reduce(UiState(), Intent.CssTextChanged(DEFAULT_CSS.trimIndent()))

    val uiState: StateFlow<UiState> =
        intents
            .scan(initialState) { state, intent -> reduce(state, intent) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)

    fun onCssTextChange(text: String) {
        viewModelScope.launch { intents.emit(Intent.CssTextChanged(text)) }
    }

    fun onCursorOffsetChange(offset: Int) {
        viewModelScope.launch { intents.emit(Intent.CursorOffsetChanged(offset)) }
    }

    fun onAstNodeClicked(node: AstDisplayNode) {
        viewModelScope.launch { intents.emit(Intent.AstNodeClicked(node.cssRange)) }
    }

    fun toggleCollapse(nodeId: Int) {
        viewModelScope.launch { intents.emit(Intent.ToggleCollapse(nodeId)) }
    }

    fun clearSelection() {
        viewModelScope.launch { intents.emit(Intent.ClearSelection) }
    }

    fun searchCss(query: String) {
        viewModelScope.launch { intents.emit(Intent.SearchCss(query)) }
    }

    fun navigateSearchResult(forward: Boolean) {
        viewModelScope.launch { intents.emit(Intent.NavigateSearchResult(forward)) }
    }

    fun clearSearch() {
        viewModelScope.launch { intents.emit(Intent.ClearSearch) }
    }

    fun searchAstTree(query: String) {
        viewModelScope.launch { intents.emit(Intent.SearchAstTree(query)) }
    }

    fun clearAstFilter() {
        viewModelScope.launch { intents.emit(Intent.ClearAstFilter) }
    }

    fun toggleNodeDetails(nodeId: Int) {
        viewModelScope.launch { intents.emit(Intent.ToggleNodeDetails(nodeId)) }
    }

    fun updateSplitRatio(ratio: Float) {
        viewModelScope.launch { intents.emit(Intent.UpdateSplitRatio(ratio)) }
    }

    fun toggleEditorSearch() {
        viewModelScope.launch { intents.emit(Intent.ToggleEditorSearch) }
    }

    fun toggleAstFilter() {
        viewModelScope.launch { intents.emit(Intent.ToggleAstFilter) }
    }
}

private sealed interface Intent {
    data class CssTextChanged(
        val text: String,
    ) : Intent

    data class CursorOffsetChanged(
        val offset: Int,
    ) : Intent

    data class AstNodeClicked(
        val cssRange: IntRange,
    ) : Intent

    data class ToggleCollapse(
        val nodeId: Int,
    ) : Intent

    data object ClearSelection : Intent

    // Search
    data class SearchCss(
        val query: String,
    ) : Intent

    data class NavigateSearchResult(
        val forward: Boolean,
    ) : Intent

    data object ClearSearch : Intent

    // AST filter
    data class SearchAstTree(
        val query: String,
    ) : Intent

    data object ClearAstFilter : Intent

    // Node details
    data class ToggleNodeDetails(
        val nodeId: Int,
    ) : Intent

    // Split pane
    data class UpdateSplitRatio(
        val ratio: Float,
    ) : Intent

    // Visibility toggles
    data object ToggleEditorSearch : Intent

    data object ToggleAstFilter : Intent
}

private fun reduce(
    state: UiState,
    intent: Intent,
): UiState =
    when (intent) {
        is Intent.CssTextChanged -> {
            val text = intent.text
            val (tokens, tokenizeError) = tokenize(text)
            val (styleSheet, parseError) =
                if (tokens.isNotEmpty()) {
                    parse(text, tokens)
                } else {
                    null to tokenizeError
                }
            val flattenedNodes = AstFlattener.flatten(styleSheet)
            val visibleNodes = computeVisibleNodes(flattenedNodes, state.collapsedNodeIds)
            state.copy(
                cssText = text,
                tokens = tokens,
                styleSheet = styleSheet,
                errorInfo = parseError,
                parseError = parseError?.message,
                flattenedNodes = flattenedNodes,
                visibleNodes = visibleNodes,
                highlightedNodeIndex = findDeepestNodeAtOffset(visibleNodes, state.cursorOffset)
            )
        }

        is Intent.CursorOffsetChanged -> {
            state.copy(
                cursorOffset = intent.offset,
                highlightedNodeIndex = findDeepestNodeAtOffset(state.visibleNodes, intent.offset)
            )
        }

        is Intent.AstNodeClicked -> {
            state.copy(selectedCssRange = intent.cssRange)
        }

        is Intent.ToggleCollapse -> {
            val newCollapsed =
                if (intent.nodeId in state.collapsedNodeIds) {
                    state.collapsedNodeIds - intent.nodeId
                } else {
                    state.collapsedNodeIds + intent.nodeId
                }
            val visibleNodes = computeVisibleNodes(state.flattenedNodes, newCollapsed)
            state.copy(
                collapsedNodeIds = newCollapsed,
                visibleNodes = visibleNodes,
                highlightedNodeIndex = findDeepestNodeAtOffset(visibleNodes, state.cursorOffset)
            )
        }

        Intent.ClearSelection -> {
            state.copy(selectedCssRange = null)
        }

        is Intent.SearchCss -> {
            val matches = computeSearchMatches(state.cssText, intent.query)
            val currentIndex = if (matches.isNotEmpty()) 0 else -1
            state.copy(
                editorSearchQuery = intent.query,
                editorSearchMatches = matches,
                editorSearchCurrentIndex = currentIndex
            )
        }

        is Intent.NavigateSearchResult -> {
            val newIndex =
                navigateSearchIndex(
                    currentIndex = state.editorSearchCurrentIndex,
                    matchCount = state.editorSearchMatches.size,
                    forward = intent.forward
                )
            state.copy(editorSearchCurrentIndex = newIndex)
        }

        Intent.ClearSearch -> {
            state.copy(
                editorSearchQuery = "",
                editorSearchMatches = emptyList(),
                editorSearchCurrentIndex = -1,
                editorSearchVisible = false
            )
        }

        is Intent.SearchAstTree -> {
            val matchIds = computeAstFilterMatches(state.flattenedNodes, intent.query)
            state.copy(
                astFilterQuery = intent.query,
                astFilterMatchIds = matchIds
            )
        }

        Intent.ClearAstFilter -> {
            state.copy(
                astFilterQuery = "",
                astFilterMatchIds = emptySet(),
                astFilterVisible = false
            )
        }

        is Intent.ToggleNodeDetails -> {
            val newExpanded =
                if (intent.nodeId in state.expandedDetailNodeIds) {
                    state.expandedDetailNodeIds - intent.nodeId
                } else {
                    state.expandedDetailNodeIds + intent.nodeId
                }
            state.copy(expandedDetailNodeIds = newExpanded)
        }

        is Intent.UpdateSplitRatio -> {
            state.copy(
                splitRatio = clampSplitRatio(intent.ratio)
            )
        }

        Intent.ToggleEditorSearch -> {
            state.copy(
                editorSearchVisible = !state.editorSearchVisible
            )
        }

        Intent.ToggleAstFilter -> {
            state.copy(
                astFilterVisible = !state.astFilterVisible
            )
        }
    }

private fun tokenize(css: String): Pair<List<Token<out CssTokenKind>>, ParseErrorInfo?> =
    try {
        CssTokenizer().tokenize(css) to null
    } catch (expected: Exception) {
        emptyList<Token<out CssTokenKind>>() to ParseErrorInfo.from(expected)
    }

private fun parse(
    css: String,
    tokens: List<Token<out CssTokenKind>>,
): Pair<StyleSheet?, ParseErrorInfo?> =
    try {
        val consumers = CssConsumers(css)
        val styleSheet = CssParser(consumers).parse(tokens)
        styleSheet to null
    } catch (expected: Exception) {
        null to ParseErrorInfo.from(expected)
    }

@Immutable
data class UiState(
    val cssText: String = "",
    val cursorOffset: Int = 0,
    val selectedCssRange: IntRange? = null,
    val collapsedNodeIds: Set<Int> = emptySet(),
    val tokens: List<Token<out CssTokenKind>> = emptyList(),
    val styleSheet: StyleSheet? = null,
    val errorInfo: ParseErrorInfo? = null,
    val parseError: String? = null,
    val flattenedNodes: List<AstDisplayNode> = emptyList(),
    val visibleNodes: List<AstDisplayNode> = emptyList(),
    val highlightedNodeIndex: Int? = null,
    // Search state
    val editorSearchQuery: String = "",
    val editorSearchMatches: List<IntRange> = emptyList(),
    val editorSearchCurrentIndex: Int = -1,
    val editorSearchVisible: Boolean = false,
    // AST filter state
    val astFilterQuery: String = "",
    val astFilterMatchIds: Set<Int> = emptySet(),
    val astFilterVisible: Boolean = false,
    // Node detail expansion
    val expandedDetailNodeIds: Set<Int> = emptySet(),
    // Split pane
    val splitRatio: Float = 0.5f,
)

internal fun computeSearchMatches(
    text: String,
    query: String,
): List<IntRange> {
    if (query.isBlank()) return emptyList()
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val matches = mutableListOf<IntRange>()
    var start = 0
    while (true) {
        val index = lowerText.indexOf(lowerQuery, start)
        if (index < 0) break
        matches += index..(index + lowerQuery.length - 1)
        start = index + 1
    }
    return matches
}

internal fun navigateSearchIndex(
    currentIndex: Int,
    matchCount: Int,
    forward: Boolean,
): Int {
    if (matchCount == 0) return -1
    return if (forward) {
        (currentIndex + 1) % matchCount
    } else {
        if (currentIndex <= 0) matchCount - 1 else currentIndex - 1
    }
}

internal fun computeAstFilterMatches(
    nodes: List<AstDisplayNode>,
    query: String,
): Set<Int> {
    if (query.isBlank()) return emptySet()
    val lowerQuery = query.lowercase()
    return nodes
        .filter { node ->
            node.label.lowercase().contains(lowerQuery) ||
                node.summary.lowercase().contains(lowerQuery)
        }.map { it.id }
        .toSet()
}

internal fun clampSplitRatio(ratio: Float): Float = ratio.coerceIn(0.2f, 0.8f)

private fun computeVisibleNodes(
    flattenedNodes: List<AstDisplayNode>,
    collapsed: Set<Int>,
): List<AstDisplayNode> {
    val result = mutableListOf<AstDisplayNode>()
    var skipUntilDepth = Int.MAX_VALUE
    for (node in flattenedNodes) {
        if (node.depth > skipUntilDepth) continue
        skipUntilDepth = Int.MAX_VALUE
        result += node
        if (node.id in collapsed && node.hasChildren) {
            skipUntilDepth = node.depth
        }
    }
    return result
}

private fun findDeepestNodeAtOffset(
    nodes: List<AstDisplayNode>,
    offset: Int,
): Int? {
    var bestIndex: Int? = null
    var bestSize = Int.MAX_VALUE
    nodes.forEachIndexed { index, node ->
        if (offset in node.cssRange) {
            val size = node.cssRange.last - node.cssRange.first
            if (size < bestSize) {
                bestSize = size
                bestIndex = index
            }
        }
    }
    return bestIndex
}

private const val DEFAULT_CSS = """
/* KSS Demo - CSS AST Explorer */

:root {
    --primary: #569cd6;
    --bg: #1e1e1e;
}

body {
    margin: 0;
    padding: 16px;
    font-family: sans-serif;
    background-color: var(--bg);
    color: #d4d4d4;
}

.container {
    max-width: 960px;
    margin: 0 auto;
}

h1, h2, h3 {
    color: var(--primary);
    font-weight: 600;
}

a:hover {
    text-decoration: underline;
    color: #9cdcfe;
}

.input-text {
    border: 1px solid #3e3e42;
    padding: 8px 12px;
    border-radius: 4px;
}

@media (max-width: 768px) {
    .container {
        padding: 0 16px;
    }
}
"""
