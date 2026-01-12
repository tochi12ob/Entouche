package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import en.entouche.ui.components.*
import en.entouche.ui.theme.*

@Composable
fun NotesListScreen(
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToNewNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>("all") }
    var isGridView by remember { mutableStateOf(true) }

    val filters = remember {
        listOf(
            FilterChipData("all", "All", Icons.Filled.Dashboard),
            FilterChipData("notes", "Notes", Icons.Filled.Notes),
            FilterChipData("voice", "Voice", Icons.Filled.Mic),
            FilterChipData("photos", "Photos", Icons.Filled.PhotoCamera),
            FilterChipData("reminders", "Reminders", Icons.Filled.Alarm)
        )
    }

    // Sample data
    val notes = remember {
        listOf(
            NotePreview(
                id = "1",
                title = "Meeting Notes - Product Review",
                preview = "Discussed new feature rollout timeline. Key points: mobile app update scheduled for next month, backend improvements needed for scaling...",
                timestamp = "2h ago",
                type = NoteType.Text,
                tags = listOf("Work", "Product"),
                hasAISummary = true
            ),
            NotePreview(
                id = "2",
                title = "Voice Memo - Ideas",
                preview = "Remember to research new meditation apps and compare features...",
                timestamp = "5h ago",
                type = NoteType.Voice,
                tags = listOf("Personal", "Ideas")
            ),
            NotePreview(
                id = "3",
                title = "Recipe - Grandma's Pasta",
                preview = "Captured photo of the handwritten recipe. Ingredients include fresh tomatoes, basil, garlic...",
                timestamp = "Yesterday",
                type = NoteType.Photo,
                tags = listOf("Food", "Family")
            ),
            NotePreview(
                id = "4",
                title = "Daily Vitamins",
                preview = "Take morning vitamins with breakfast at 8 AM",
                timestamp = "2d ago",
                type = NoteType.Reminder,
                hasAISummary = true
            ),
            NotePreview(
                id = "5",
                title = "Book Recommendations",
                preview = "List of books mentioned in the podcast: Atomic Habits, Deep Work, The Psychology of Money...",
                timestamp = "3d ago",
                type = NoteType.Text,
                tags = listOf("Reading", "Learning")
            ),
            NotePreview(
                id = "6",
                title = "Workout Plan Voice Note",
                preview = "Monday: Upper body, Tuesday: Cardio, Wednesday: Lower body...",
                timestamp = "4d ago",
                type = NoteType.Voice,
                tags = listOf("Health", "Fitness")
            ),
            NotePreview(
                id = "7",
                title = "Doctor Appointment",
                preview = "Annual checkup scheduled for next Friday at 10 AM",
                timestamp = "1w ago",
                type = NoteType.Reminder,
                tags = listOf("Health")
            ),
            NotePreview(
                id = "8",
                title = "Home Renovation Ideas",
                preview = "Photos of kitchen designs from Pinterest. Love the white marble countertops...",
                timestamp = "1w ago",
                type = NoteType.Photo,
                tags = listOf("Home", "Ideas")
            )
        )
    }

    val filteredNotes = remember(notes, selectedFilter) {
        when (selectedFilter) {
            "notes" -> notes.filter { it.type == NoteType.Text }
            "voice" -> notes.filter { it.type == NoteType.Voice }
            "photos" -> notes.filter { it.type == NoteType.Photo }
            "reminders" -> notes.filter { it.type == NoteType.Reminder }
            else -> notes
        }
    }

    GradientBackground(
        modifier = modifier,
        animated = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.screenPaddingHorizontal)
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.spacingLg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Notes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )

                        Row {
                            GlassIconButton(
                                icon = if (isGridView) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                                onClick = { isGridView = !isGridView },
                                style = GlassButtonStyle.Ghost,
                                size = 40.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                    // Search bar
                    GlassSearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search notes...",
                        leadingIcon = Icons.Filled.Search,
                        trailingIcon = if (searchQuery.isNotEmpty()) Icons.Filled.Close else null,
                        onTrailingIconClick = { searchQuery = "" }
                    )

                    Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                    // Filter chips
                    FilterChipGroup(
                        chips = filters,
                        selectedChipId = selectedFilter,
                        onChipSelected = { selectedFilter = it }
                    )

                    Spacer(modifier = Modifier.height(Dimensions.spacingMd))
                }

                // Notes list/grid
                AnimatedContent(
                    targetState = isGridView,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) { showGrid ->
                    if (showGrid) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimensions.screenPaddingHorizontal),
                            verticalItemSpacing = 12.dp,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                bottom = Dimensions.bottomNavHeight + Dimensions.spacingXl + 72.dp
                            )
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNavigateToNoteDetail(note.id) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimensions.screenPaddingHorizontal),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                bottom = Dimensions.bottomNavHeight + Dimensions.spacingXl + 72.dp
                            )
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                CompactNoteCard(
                                    note = note,
                                    onClick = { onNavigateToNoteDetail(note.id) }
                                )
                            }
                        }
                    }
                }
            }

            // FAB
            GlassFAB(
                icon = Icons.Filled.Add,
                onClick = onNavigateToNewNote,
                extended = true,
                text = "New Note",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = Dimensions.screenPaddingHorizontal,
                        bottom = Dimensions.bottomNavHeight + Dimensions.spacingMd
                    )
            )
        }
    }
}
