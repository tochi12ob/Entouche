package en.entouche.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import en.entouche.ui.theme.*

data class FilterChipData(
    val id: String,
    val label: String,
    val icon: ImageVector? = null
)

@Composable
fun GlassFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = TealWave
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.2f) else GlassWhite,
        animationSpec = tween(Dimensions.animDurationFast)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else GlassBorder,
        animationSpec = tween(Dimensions.animDurationFast)
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else TextSecondary,
        animationSpec = tween(Dimensions.animDurationFast)
    )

    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )

        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FilterChipGroup(
    chips: List<FilterChipData>,
    selectedChipId: String?,
    onChipSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = TealWave
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            GlassFilterChip(
                label = chip.label,
                isSelected = chip.id == selectedChipId,
                onClick = { onChipSelected(chip.id) },
                icon = chip.icon,
                accentColor = accentColor
            )
        }
    }
}

@Composable
fun MultiSelectFilterChipGroup(
    chips: List<FilterChipData>,
    selectedChipIds: Set<String>,
    onChipToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = TealWave
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            GlassFilterChip(
                label = chip.label,
                isSelected = chip.id in selectedChipIds,
                onClick = { onChipToggled(chip.id) },
                icon = chip.icon,
                accentColor = accentColor
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRemovable: Boolean = false,
    onRemove: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )

        if (isRemovable && onRemove != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Remove",
                tint = color,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

@Composable
fun ToggleChipGroup(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) TealWave else Color.Transparent,
                animationSpec = tween(Dimensions.animDurationFast)
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextOnTeal else TextSecondary,
                animationSpec = tween(Dimensions.animDurationFast)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onSelectionChange(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
            }
        }
    }
}
