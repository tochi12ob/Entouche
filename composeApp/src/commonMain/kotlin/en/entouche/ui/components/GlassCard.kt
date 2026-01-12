package en.entouche.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import en.entouche.ui.theme.*

enum class GlassCardSize {
    Small,
    Medium,
    Large
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    size: GlassCardSize = GlassCardSize.Medium,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cornerRadius = when (size) {
        GlassCardSize.Small -> 12.dp
        GlassCardSize.Medium -> 16.dp
        GlassCardSize.Large -> 24.dp
    }

    val padding = when (size) {
        GlassCardSize.Small -> 12.dp
        GlassCardSize.Medium -> 16.dp
        GlassCardSize.Large -> 20.dp
    }

    val shape = RoundedCornerShape(cornerRadius)

    val baseModifier = modifier
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    GlassWhite,
                    GlassTealTint.copy(alpha = 0.05f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    GlassBorder,
                    GlassBorder.copy(alpha = 0.1f)
                )
            ),
            shape = shape
        )

    Column(
        modifier = if (onClick != null) {
            baseModifier.clickable(onClick = onClick).padding(padding)
        } else {
            baseModifier.padding(padding)
        },
        content = content
    )
}

@Composable
fun GlassCardClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: GlassCardSize = GlassCardSize.Medium,
    content: @Composable ColumnScope.() -> Unit
) {
    val cornerRadius = when (size) {
        GlassCardSize.Small -> 12.dp
        GlassCardSize.Medium -> 16.dp
        GlassCardSize.Large -> 24.dp
    }

    val padding = when (size) {
        GlassCardSize.Small -> 12.dp
        GlassCardSize.Medium -> 16.dp
        GlassCardSize.Large -> 20.dp
    }

    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassWhite,
                        GlassTealTint.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassBorder,
                        GlassBorder.copy(alpha = 0.1f)
                    )
                ),
                shape = shape
            )
            .padding(padding),
        content = content
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            ),
        content = content
    )
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = TealWave
) {
    GlassCard(
        modifier = modifier.size(Dimensions.statsCardSize),
        size = GlassCardSize.Medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column {
                androidx.compose.material3.Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
