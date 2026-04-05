package dev.tonholo.kss.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import dev.tonholo.kss.demo.state.clampSplitRatio

private val DividerWidth = 4.dp
private val GripWidth = 2.dp
private val GripHeight = 24.dp

/**
 * Calculates a new split ratio given a drag delta in pixels.
 *
 * @param currentRatio The current split ratio (0..1).
 * @param dragDeltaPx The horizontal drag delta in pixels.
 * @param totalWidthPx The total available width in pixels.
 * @return The clamped new split ratio.
 */
internal fun calculateNewRatio(
    currentRatio: Float,
    dragDeltaPx: Float,
    totalWidthPx: Float,
): Float {
    if (totalWidthPx <= 0f) return currentRatio
    return clampSplitRatio(currentRatio + dragDeltaPx / totalWidthPx)
}

/**
 * A horizontal split pane with a draggable divider.
 *
 * @param ratio The current split ratio (0..1), controlling how much space the left panel gets.
 * @param onRatioChange Callback invoked with the new ratio when the divider is dragged.
 * @param leftContent The composable content for the left panel.
 * @param modifier Optional [Modifier] applied to the root layout.
 * @param rightContent The composable content for the right panel.
 */
@Composable
fun SplitPane(
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    leftContent: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    rightContent: @Composable (Modifier) -> Unit,
) {
    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    val currentRatio by rememberUpdatedState(ratio)

    Row(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { totalWidthPx = it.width.toFloat() }
    ) {
        leftContent(Modifier.weight(ratio))

        // Divider with grip
        Box(
            modifier =
                Modifier
                    .width(DividerWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline)
                    .pointerHoverIcon(PointerIcon.Crosshair)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newRatio = calculateNewRatio(currentRatio, dragAmount.x, totalWidthPx)
                            onRatioChange(newRatio)
                        }
                    },
            contentAlignment = Alignment.Center
        ) {
            // Grip indicator
            Box(
                modifier =
                    Modifier
                        .width(GripWidth)
                        .height(GripHeight)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(1.dp)
                        )
            )
        }

        rightContent(Modifier.weight(1f - ratio))
    }
}
