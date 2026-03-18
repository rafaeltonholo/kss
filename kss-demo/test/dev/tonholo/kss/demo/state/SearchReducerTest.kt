package dev.tonholo.kss.demo.state

import dev.tonholo.kss.demo.model.AstDisplayNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun testDisplayNode(
    id: Int,
    label: String = "",
    summary: String = "",
    depth: Int = 0,
    cssRange: IntRange = 0..0,
    hasChildren: Boolean = false,
) = AstDisplayNode(
    id = id,
    depth = depth,
    label = label,
    summary = summary,
    cssRange = cssRange,
    hasChildren = hasChildren,
)

class SearchReducerTest {

    @Test
    fun `computeSearchMatches finds all case-insensitive occurrences`() {
        val text = "color: red; background-color: blue;"
        val matches = computeSearchMatches(text, "color")
        assertEquals(2, matches.size)
        assertEquals(0..4, matches[0])
        assertEquals(23..27, matches[1])
    }

    @Test
    fun `computeSearchMatches returns empty for blank query`() {
        val matches = computeSearchMatches("color: red;", "")
        assertTrue(matches.isEmpty())
    }

    @Test
    fun `computeSearchMatches is case-insensitive`() {
        val matches = computeSearchMatches("Color: RED; color: red;", "color")
        assertEquals(2, matches.size)
    }

    @Test
    fun `navigateSearchIndex wraps forward`() {
        assertEquals(0, navigateSearchIndex(currentIndex = 2, matchCount = 3, forward = true))
    }

    @Test
    fun `navigateSearchIndex wraps backward`() {
        assertEquals(2, navigateSearchIndex(currentIndex = 0, matchCount = 3, forward = false))
    }

    @Test
    fun `navigateSearchIndex stays at -1 with no matches`() {
        assertEquals(-1, navigateSearchIndex(currentIndex = -1, matchCount = 0, forward = true))
    }

    @Test
    fun `computeAstFilterMatches matches node label case-insensitively`() {
        val nodes = listOf(
            testDisplayNode(id = 0, label = "Declaration", summary = "color: red"),
            testDisplayNode(id = 1, label = "QualifiedRule", summary = "body"),
            testDisplayNode(id = 2, label = "Declaration", summary = "margin: 0"),
        )
        val matchIds = computeAstFilterMatches(nodes, "declaration")
        assertEquals(setOf(0, 2), matchIds)
    }

    @Test
    fun `computeAstFilterMatches matches node summary`() {
        val nodes = listOf(
            testDisplayNode(id = 0, label = "Declaration", summary = "color: red"),
            testDisplayNode(id = 1, label = "Declaration", summary = "margin: 0"),
        )
        val matchIds = computeAstFilterMatches(nodes, "color")
        assertEquals(setOf(0), matchIds)
    }

    @Test
    fun `computeAstFilterMatches returns empty for blank query`() {
        val nodes = listOf(
            testDisplayNode(id = 0, label = "Declaration", summary = "color: red"),
        )
        val matchIds = computeAstFilterMatches(nodes, "")
        assertTrue(matchIds.isEmpty())
    }

    @Test
    fun `clampSplitRatio enforces minimum panel width`() {
        assertEquals(0.2f, clampSplitRatio(0.05f), 0.001f)
        assertEquals(0.8f, clampSplitRatio(0.95f), 0.001f)
        assertEquals(0.5f, clampSplitRatio(0.5f), 0.001f)
    }
}
