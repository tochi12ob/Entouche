package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import en.entouche.ui.components.*
import en.entouche.ui.theme.*

@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Sample note data (in real app, fetch by noteId)
    val note = remember {
        NotePreview(
            id = noteId,
            title = "Meeting Notes - Product Review",
            preview = """
                Today we had our weekly product review meeting with the entire team. Here are the key discussion points:

                ## Action Items
                1. Update the mobile app UI based on user feedback
                2. Improve backend API response times
                3. Schedule user testing sessions for next week

                ## Key Decisions
                - Launch date pushed to end of month
                - Adding two more engineers to the team
                - New feature prioritization based on user surveys

                ## Notes
                The team seems optimistic about the upcoming release. We need to focus on performance improvements before the beta launch.

                Sarah mentioned that the competitor launched a similar feature last week, so we need to differentiate our approach.
            """.trimIndent(),
            timestamp = "2 hours ago",
            type = NoteType.Text,
            tags = listOf("Work", "Product", "Meeting"),
            hasAISummary = true
        )
    }

    val relatedNotes = remember {
        listOf(
            NotePreview(
                id = "r1",
                title = "Previous Meeting Notes",
                preview = "Last week's review covered...",
                timestamp = "1 week ago",
                type = NoteType.Text
            ),
            NotePreview(
                id = "r2",
                title = "Product Roadmap",
                preview = "Q2 features and timeline...",
                timestamp = "2 weeks ago",
                type = NoteType.Text
            )
        )
    }

    GradientBackground(
        modifier = modifier,
        animated = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.screenPaddingHorizontal)
                    .padding(top = Dimensions.spacingMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.Filled.ArrowBack,
                    onClick = onNavigateBack,
                    style = GlassButtonStyle.Ghost,
                    size = 44.dp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = Icons.Outlined.Share,
                        onClick = { /* TODO: Share */ },
                        style = GlassButtonStyle.Ghost,
                        size = 44.dp
                    )
                    GlassIconButton(
                        icon = Icons.Outlined.Edit,
                        onClick = onEdit,
                        style = GlassButtonStyle.Secondary,
                        size = 44.dp
                    )
                    GlassIconButton(
                        icon = Icons.Outlined.Delete,
                        onClick = { showDeleteDialog = true },
                        style = GlassButtonStyle.Ghost,
                        size = 44.dp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimensions.screenPaddingHorizontal)
            ) {
                Spacer(modifier = Modifier.height(Dimensions.spacingLg))

                // Note type and timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = TealWave.copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Notes,
                            contentDescription = null,
                            tint = TealWave,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Text Note",
                            style = MaterialTheme.typography.labelMedium,
                            color = TealWave
                        )
                        Text(
                            text = note.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingLg))

                // Title
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    note.tags.forEach { tag ->
                        TagChip(text = tag, color = TealWave)
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))

                // AI Summary Card
                if (note.hasAISummary) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        size = GlassCardSize.Medium
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = TealWave,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Summary",
                                style = MaterialTheme.typography.titleSmall,
                                color = TealWave
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "This note contains action items from a product review meeting. Key topics include mobile app updates, backend improvements, and launch timeline adjustments.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TagChip(text = "3 Action Items", color = Warning)
                            TagChip(text = "2 Decisions", color = Success)
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spacingLg))
                }

                // Note Content
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    size = GlassCardSize.Large
                ) {
                    Text(
                        text = note.preview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))

                // Related Notes
                Text(
                    text = "Related Notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                relatedNotes.forEach { relatedNote ->
                    CompactNoteCard(
                        note = relatedNote,
                        onClick = { /* Navigate to related note */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Set Reminder",
                        onClick = { /* TODO */ },
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Outlined.Alarm
                    )
                    GlassButton(
                        text = "Add to Collection",
                        onClick = { /* TODO */ },
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Outlined.Folder
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXxl))
            }
        }
    }
}
