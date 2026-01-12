package en.entouche.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import en.entouche.ui.theme.*

enum class GlassButtonStyle {
    Primary,
    Secondary,
    Ghost,
    Accent
}

enum class GlassButtonSize {
    Small,
    Medium,
    Large
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.Primary,
    size: GlassButtonSize = GlassButtonSize.Medium,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val height = when (size) {
        GlassButtonSize.Small -> Dimensions.buttonHeightSmall
        GlassButtonSize.Medium -> Dimensions.buttonHeightMedium
        GlassButtonSize.Large -> Dimensions.buttonHeightLarge
    }

    val horizontalPadding = when (size) {
        GlassButtonSize.Small -> 16.dp
        GlassButtonSize.Medium -> 24.dp
        GlassButtonSize.Large -> 32.dp
    }

    val cornerRadius = when (size) {
        GlassButtonSize.Small -> 10.dp
        GlassButtonSize.Medium -> 12.dp
        GlassButtonSize.Large -> 14.dp
    }

    val textStyle = when (size) {
        GlassButtonSize.Small -> MaterialTheme.typography.labelMedium
        GlassButtonSize.Medium -> MaterialTheme.typography.labelLarge
        GlassButtonSize.Large -> MaterialTheme.typography.titleSmall
    }

    val (backgroundColor, borderColor, textColor, iconTint) = when (style) {
        GlassButtonStyle.Primary -> listOf(
            TealWave,
            TealWave,
            TextOnTeal,
            TextOnTeal
        )
        GlassButtonStyle.Secondary -> listOf(
            GlassWhite,
            GlassBorder,
            TextPrimary,
            TealWave
        )
        GlassButtonStyle.Ghost -> listOf(
            Color.Transparent,
            GlassBorder,
            TextPrimary,
            TealWave
        )
        GlassButtonStyle.Accent -> listOf(
            Seafoam.copy(alpha = 0.2f),
            Seafoam,
            Seafoam,
            Seafoam
        )
    }

    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            when (style) {
                GlassButtonStyle.Primary -> TealDark
                else -> backgroundColor.copy(alpha = 0.8f)
            }
        } else if (!enabled) {
            backgroundColor.copy(alpha = 0.5f)
        } else {
            backgroundColor
        },
        animationSpec = tween(Dimensions.animDurationFast)
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (enabled) borderColor else borderColor.copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (enabled) iconTint else iconTint.copy(alpha = 0.5f),
                    modifier = Modifier.size(
                        when (size) {
                            GlassButtonSize.Small -> 16.dp
                            GlassButtonSize.Medium -> 20.dp
                            GlassButtonSize.Large -> 24.dp
                        }
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                style = textStyle,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
            )

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = if (enabled) iconTint else iconTint.copy(alpha = 0.5f),
                    modifier = Modifier.size(
                        when (size) {
                            GlassButtonSize.Small -> 16.dp
                            GlassButtonSize.Medium -> 20.dp
                            GlassButtonSize.Large -> 24.dp
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 48.dp,
    style: GlassButtonStyle = GlassButtonStyle.Secondary,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (backgroundColor, borderColor, iconTint) = when (style) {
        GlassButtonStyle.Primary -> listOf(TealWave, TealWave, TextOnTeal)
        GlassButtonStyle.Secondary -> listOf(GlassWhite, GlassBorder, TealWave)
        GlassButtonStyle.Ghost -> listOf(Color.Transparent, GlassBorder, TextPrimary)
        GlassButtonStyle.Accent -> listOf(Seafoam.copy(alpha = 0.2f), Seafoam, Seafoam)
    }

    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            backgroundColor.copy(alpha = 0.7f)
        } else if (!enabled) {
            backgroundColor.copy(alpha = 0.5f)
        } else {
            backgroundColor
        },
        animationSpec = tween(Dimensions.animDurationFast)
    )

    val shape = RoundedCornerShape(size / 4)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (enabled) borderColor else borderColor.copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) iconTint else iconTint.copy(alpha = 0.5f),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
fun GlassFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    extended: Boolean = false,
    text: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) TealDark else TealWave,
        animationSpec = tween(Dimensions.animDurationFast)
    )

    val shape = if (extended) RoundedCornerShape(16.dp) else RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .then(
                if (extended) {
                    Modifier.height(56.dp)
                } else {
                    Modifier.size(56.dp)
                }
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(animatedBgColor, TealDark)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Seafoam.copy(alpha = 0.5f),
                        TealWave.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (extended) 20.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextOnTeal,
                modifier = Modifier.size(24.dp)
            )
            if (extended && text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextOnTeal
                )
            }
        }
    }
}
