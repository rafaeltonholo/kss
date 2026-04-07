package dev.tonholo.kss.demo.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SplitPaneRatioTest {

    @Test
    fun `calculateNewRatio adds delta proportional to total width`() {
        val newRatio = calculateNewRatio(
            currentRatio = 0.5f,
            dragDeltaPx = 100f,
            totalWidthPx = 1000f,
        )
        assertEquals(0.6f, newRatio, 0.001f)
    }

    @Test
    fun `calculateNewRatio clamps to minimum`() {
        val newRatio = calculateNewRatio(
            currentRatio = 0.25f,
            dragDeltaPx = -200f,
            totalWidthPx = 1000f,
        )
        assertEquals(0.2f, newRatio, 0.001f)
    }

    @Test
    fun `calculateNewRatio clamps to maximum`() {
        val newRatio = calculateNewRatio(
            currentRatio = 0.75f,
            dragDeltaPx = 200f,
            totalWidthPx = 1000f,
        )
        assertEquals(0.8f, newRatio, 0.001f)
    }

    @Test
    fun `calculateNewRatio handles zero width gracefully`() {
        val newRatio = calculateNewRatio(
            currentRatio = 0.5f,
            dragDeltaPx = 100f,
            totalWidthPx = 0f,
        )
        assertEquals(0.5f, newRatio, 0.001f)
    }
}
