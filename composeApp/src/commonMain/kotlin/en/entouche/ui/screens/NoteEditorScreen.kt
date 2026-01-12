package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import en.entouche.data.models.NoteType
import en.entouche.ui.components.*
import en.entouche.ui.theme.*
import en.entouche.ui.viewmodel.EntoucheViewModel

@Composable
fun NoteEditorScreen(
    viewModel: EntoucheViewModel,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NoteType.TEXT) }
    var isSaving by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    fun saveNote() {
        if (title.isNotBlank() && content.isNotBlank()) {
            isSaving = true
            viewModel.createNote(
                title = title,
                content = content,
                type = selectedType,
                tags = emptyList()
            )
            onSave()
        }
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
                    icon = Icons.Filled.Close,
                    onClick = onNavigateBack,
                    style = GlassButtonStyle.Ghost,
                    size = 44.dp
                )

                Text(
                    text = "New Note",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                if (isSaving || isLoading) {
                    CircularProgressIndicator(
                        color = TealWave,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    GlassButton(
                        text = "Save",
                        onClick = { saveNote() },
                        style = GlassButtonStyle.Primary,
                        size = GlassButtonSize.Small,
                        enabled = title.isNotBlank() && content.isNotBlank()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimensions.screenPaddingHorizontal)
                    .padding(top = Dimensions.spacingLg)
            ) {
                // Note type selector
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NoteTypeChip(
                        text = "Text",
                        icon = Icons.Filled.EditNote,
                        selected = selectedType == NoteType.TEXT,
                        onClick = { selectedType = NoteType.TEXT }
                    )
                    NoteTypeChip(
                        text = "Reminder",
                        icon = Icons.Filled.Alarm,
                        selected = selectedType == NoteType.REMINDER,
                        onClick = { selectedType = NoteType.REMINDER }
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingLg))

                // Title field
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                GlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Enter a title for your note",
                    leadingIcon = Icons.Filled.Title,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingLg))

                // Content field
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                GlassTextArea(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = "Write your thoughts here...",
                    minLines = 8,
                    maxLines = 20,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                AnimatedVisibility(
                    visible = error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    error?.let {
                        Spacer(modifier = Modifier.height(Dimensions.spacingMd))
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            size = GlassCardSize.Small
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))

                // Tips
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    size = GlassCardSize.Medium
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tips",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your notes are automatically saved to the cloud and synced across devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))
            }
        }
    }
}

@Composable
private fun NoteTypeChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier,
        size = GlassCardSize.Small,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) TealWave else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) TealWave else TextSecondary
            )
        }
    }
}
