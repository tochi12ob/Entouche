package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import en.entouche.ui.components.*
import en.entouche.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onNavigateToNoteDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val recentSearches = remember {
        listOf(
            "meeting notes",
            "recipe ideas",
            "doctor appointment",
            "workout plan"
        )
    }

    val suggestedQueries = remember {
        listOf(
            "What did I discuss in my last meeting?",
            "Find all my voice memos from this week",
            "Show me notes about health",
            "What are my upcoming reminders?"
        )
    }

    // Sample search results
    val searchResults = remember {
        listOf(
            NotePreview(
                id = "1",
                title = "Meeting Notes - Product Review",
                preview = "Discussed project timeline and deliverables...",
                timestamp = "2h ago",
                type = NoteType.Text,
                tags = listOf("Work", "Meeting"),
                hasAISummary = true
            ),
            NotePreview(
                id = "2",
                title = "Voice Memo - Call Summary",
                preview = "Follow up with client about proposal...",
                timestamp = "Yesterday",
                type = NoteType.Voice,
                tags = listOf("Work")
            ),
            NotePreview(
                id = "3",
                title = "Weekly Planning",
                preview = "Tasks for the week including meetings and deadlines...",
                timestamp = "3d ago",
                type = NoteType.Text,
                hasAISummary = true
            )
        )
    }

    // Simulate search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            isSearching = true
            delay(800)
            isSearching = false
            hasSearched = true
        } else {
            hasSearched = false
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
                .padding(horizontal = Dimensions.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Header
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMd))

            // Search bar with AI indicator
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                size = GlassCardSize.Small
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = TealWave,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI-Powered Semantic Search",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealWave
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMd))

            GlassSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Ask anything about your notes...",
                leadingIcon = Icons.Filled.Search,
                trailingIcon = when {
                    isSearching -> Icons.Filled.Refresh
                    searchQuery.isNotEmpty() -> Icons.Filled.Close
                    else -> Icons.Outlined.Mic
                },
                onTrailingIconClick = {
                    if (searchQuery.isNotEmpty()) searchQuery = ""
                },
                onSearch = { /* Perform search */ }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Content based on state
            AnimatedContent(
                targetState = Triple(searchQuery.isEmpty(), isSearching, hasSearched),
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { (isEmpty, searching, searched) ->
                when {
                    isEmpty -> {
                        // Show recent searches and suggestions
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMd)
                        ) {
                            // Recent searches
                            item {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(Dimensions.spacingSm))
                            }

                            items(recentSearches) { query ->
                                RecentSearchItem(
                                    query = query,
                                    onClick = { searchQuery = query }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(Dimensions.spacingMd))
                                Text(
                                    text = "Try asking",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(Dimensions.spacingSm))
                            }

                            items(suggestedQueries) { query ->
                                SuggestedQueryItem(
                                    query = query,
                                    onClick = { searchQuery = query }
                                )
                            }
                        }
                    }
                    searching -> {
                        // Loading state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularWaveform(
                                    isAnimating = true,
                                    modifier = Modifier.size(120.dp),
                                    color = TealWave
                                )
                                Spacer(modifier = Modifier.height(Dimensions.spacingMd))
                                Text(
                                    text = "Searching with AI...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    searched -> {
                        // Show results
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = Dimensions.bottomNavHeight + Dimensions.spacingXl)
                        ) {
                            item {
                                // AI Answer card
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    size = GlassCardSize.Medium
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                                imageVector = Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                tint = TealWave,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "AI Answer",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TealWave
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Based on your notes, I found 3 relevant entries. Your most recent meeting discussed project timelines and Q2 deliverables. The action items included updating the mobile app UI and scheduling user testing.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(Dimensions.spacingSm))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Related Notes",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${searchResults.size} results",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            items(searchResults) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNavigateToNoteDetail(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = GlassWhite,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = query,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.NorthWest,
            contentDescription = "Search",
            tint = TealWave,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun SuggestedQueryItem(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        GlassWhite,
                        TealWave.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = TealWave,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = query,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "Try this",
            tint = TealWave,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onClick)
        )
    }
}
