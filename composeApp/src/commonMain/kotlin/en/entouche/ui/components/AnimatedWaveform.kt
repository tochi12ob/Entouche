package en.entouche.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import en.entouche.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedWaveform(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    amplitudes: List<Float> = emptyList(),
    barCount: Int = 40,
    barWidth: Dp = 4.dp,
    barGap: Dp = 2.dp,
    minHeight: Dp = 8.dp,
    maxHeight: Dp = 80.dp,
    activeColor: Color = TealWave,
    inactiveColor: Color = FrostedGlass
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Create animated values for each bar (used when no real amplitudes provided)
    val animatedHeights = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + (index % 5) * 100,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset((index * 50) % 500)
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val barGapPx = barGap.toPx()
        val minHeightPx = minHeight.toPx()
        val maxHeightPx = maxHeight.toPx()

        val totalBarWidth = barWidthPx + barGapPx
        val startX = (canvasWidth - (barCount * totalBarWidth - barGapPx)) / 2

        for (i in 0 until barCount) {
            val heightMultiplier = if (isAnimating) {
                // Use real amplitudes if available, otherwise use animation
                if (amplitudes.isNotEmpty()) {
                    val ampIndex = (i * amplitudes.size / barCount).coerceIn(0, amplitudes.size - 1)
                    amplitudes.getOrElse(ampIndex) { 0.2f }.coerceIn(0.1f, 1f)
                } else {
                    animatedHeights[i].value
                }
            } else {
                0.15f + 0.1f * sin(i * 0.5f).toFloat()
            }

            val barHeight = minHeightPx + (maxHeightPx - minHeightPx) * heightMultiplier
            val x = startX + i * totalBarWidth
            val y = (canvasHeight - barHeight) / 2

            val color = if (isAnimating) {
                activeColor.copy(alpha = 0.5f + 0.5f * heightMultiplier)
            } else {
                inactiveColor
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2)
            )
        }
    }
}

@Composable
fun SimpleWaveform(
    levels: List<Float>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 3.dp,
    barGap: Dp = 2.dp,
    maxHeight: Dp = 60.dp,
    color: Color = TealWave
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val barGapPx = barGap.toPx()
        val maxHeightPx = maxHeight.toPx()

        val totalBarWidth = barWidthPx + barGapPx
        val barCount = levels.size.coerceAtMost((canvasWidth / totalBarWidth).toInt())
        val startX = (canvasWidth - (barCount * totalBarWidth - barGapPx)) / 2

        for (i in 0 until barCount) {
            val level = levels.getOrElse(i) { 0f }.coerceIn(0f, 1f)
            val barHeight = (maxHeightPx * level).coerceAtLeast(4f)
            val x = startX + i * totalBarWidth
            val y = (canvasHeight - barHeight) / 2

            drawRoundRect(
                color = color.copy(alpha = 0.4f + 0.6f * level),
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2)
            )
        }
    }
}

@Composable
fun CircularWaveform(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    ringCount: Int = 3,
    baseRadius: Dp = 60.dp,
    ringSpacing: Dp = 20.dp,
    color: Color = TealWave
) {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedScales = List(ringCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 + index * 200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 200)
            )
        )
    }

    val animatedAlphas = List(ringCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 + index * 200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 200)
            )
        )
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadiusPx = baseRadius.toPx()
        val ringSpacingPx = ringSpacing.toPx()

        for (i in 0 until ringCount) {
            val radius = if (isAnimating) {
                (baseRadiusPx + i * ringSpacingPx) * animatedScales[i].value
            } else {
                baseRadiusPx + i * ringSpacingPx
            }

            val alpha = if (isAnimating) {
                animatedAlphas[i].value
            } else {
                0.3f - i * 0.08f
            }

            drawCircle(
                color = color.copy(alpha = alpha.coerceAtLeast(0.1f)),
                radius = radius,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun RecordingTimer(
    durationSeconds: Int,
    modifier: Modifier = Modifier
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    androidx.compose.material3.Text(
        text = timeText,
        style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
        color = TextPrimary,
        modifier = modifier
    )
}
