package en.entouche.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import en.entouche.ui.theme.*

enum class NoteType {
    Text,
    Voice,
    Photo,
    Reminder
}

data class NotePreview(
    val id: String,
    val title: String,
    val preview: String,
    val timestamp: String,
    val type: NoteType,
    val tags: List<String> = emptyList(),
    val hasAISummary: Boolean = false
)

@Composable
fun NoteCard(
    note: NotePreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, accentColor) = when (note.type) {
        NoteType.Text -> Icons.Filled.Notes to TealWave
        NoteType.Voice -> Icons.Filled.Mic to Seafoam
        NoteType.Photo -> Icons.Filled.PhotoCamera to AquaMist
        NoteType.Reminder -> Icons.Filled.Alarm to Warning
    }

    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassWhite,
                        accentColor.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassBorder,
                        accentColor.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Header row with icon and timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = note.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preview text
        Text(
            text = note.preview,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // Tags
        if (note.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                note.tags.take(3).forEach { tag ->
                    TagChip(text = tag, color = accentColor)
                }
            }
        }

        // AI Summary indicator
        if (note.hasAISummary) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(TealWave)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Summary available",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealWave
                )
            }
        }
    }
}

@Composable
fun TagChip(
    text: String,
    color: Color = TealWave,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun CompactNoteCard(
    note: NotePreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, accentColor) = when (note.type) {
        NoteType.Text -> Icons.Filled.Notes to TealWave
        NoteType.Voice -> Icons.Filled.Mic to Seafoam
        NoteType.Photo -> Icons.Filled.PhotoCamera to AquaMist
        NoteType.Reminder -> Icons.Filled.Alarm to Warning
    }

    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = note.preview,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = note.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun HorizontalNoteCard(
    note: NotePreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, accentColor) = when (note.type) {
        NoteType.Text -> Icons.Filled.Notes to TealWave
        NoteType.Voice -> Icons.Filled.Mic to Seafoam
        NoteType.Photo -> Icons.Filled.PhotoCamera to AquaMist
        NoteType.Reminder -> Icons.Filled.Alarm to Warning
    }

    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassWhite,
                        accentColor.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = note.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = note.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = note.preview,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
