package en.entouche.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import en.entouche.ui.theme.*
import kotlin.math.sin

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientStart,
                            GradientMid,
                            GradientEnd
                        )
                    )
                )
        )

        // Animated wave overlay
        if (animated) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw subtle animated circles/orbs
                val orb1X = width * (0.2f + 0.1f * sin(animatedOffset * 2 * Math.PI).toFloat())
                val orb1Y = height * (0.3f + 0.05f * sin(animatedOffset * 3 * Math.PI).toFloat())

                val orb2X = width * (0.7f + 0.15f * sin((animatedOffset + 0.3f) * 2 * Math.PI).toFloat())
                val orb2Y = height * (0.6f + 0.08f * sin((animatedOffset + 0.5f) * 2 * Math.PI).toFloat())

                val orb3X = width * (0.5f + 0.1f * sin((animatedOffset + 0.7f) * 2 * Math.PI).toFloat())
                val orb3Y = height * (0.8f + 0.05f * sin((animatedOffset + 0.2f) * 3 * Math.PI).toFloat())

                // Orb 1 - Teal glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TealWave.copy(alpha = 0.15f),
                            TealWave.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(orb1X, orb1Y),
                        radius = width * 0.4f
                    ),
                    radius = width * 0.4f,
                    center = Offset(orb1X, orb1Y)
                )

                // Orb 2 - Seafoam glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Seafoam.copy(alpha = 0.12f),
                            Seafoam.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        center = Offset(orb2X, orb2Y),
                        radius = width * 0.35f
                    ),
                    radius = width * 0.35f,
                    center = Offset(orb2X, orb2Y)
                )

                // Orb 3 - Ocean blue glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            OceanBlue.copy(alpha = 0.2f),
                            OceanBlue.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = width * 0.5f
                    ),
                    radius = width * 0.5f,
                    center = Offset(orb3X, orb3Y)
                )
            }
        }

        // Content
        content()
    }
}

@Composable
fun SimpleGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(GradientStart, GradientMid, GradientEnd),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = colors)
            )
    ) {
        content()
    }
}
