package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import en.entouche.data.models.Note
import en.entouche.data.models.NoteType as ApiNoteType
import en.entouche.ui.components.*
import en.entouche.ui.theme.*
import en.entouche.ui.viewmodel.EntoucheViewModel
import kotlinx.datetime.*

@Composable
fun HomeScreen(
    viewModel: EntoucheViewModel,
    onNavigateToNotes: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToNewNote: () -> Unit = {},
    onNavigateToMemoryGame: () -> Unit = {},
    userName: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Collect state from ViewModel
    val notes by viewModel.notes.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val insights by viewModel.insights.collectAsState()

    // Mood state
    val todayMood by viewModel.todayMood.collectAsState()
    val moodStreak by viewModel.moodStreak.collectAsState()

    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadInsights()
        viewModel.loadTodayMood()
        viewModel.loadMoodStreak()
    }

    // Convert API notes to UI notes
    val recentNotes = notes.take(5).map { note ->
        NotePreview(
            id = note.id,
            title = note.title,
            preview = note.content.take(100) + if (note.content.length > 100) "..." else "",
            timestamp = formatTimestamp(note.createdAt),
            type = when (note.type) {
                ApiNoteType.TEXT -> NoteType.Text
                ApiNoteType.VOICE -> NoteType.Voice
                ApiNoteType.PHOTO -> NoteType.Photo
                ApiNoteType.REMINDER -> NoteType.Reminder
            },
            tags = note.tags,
            hasAISummary = note.aiSummary != null
        )
    }

    GradientBackground(
        modifier = modifier,
        animated = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimensions.screenPaddingHorizontal)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Header
            HomeHeader()

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Greeting
            GreetingSection(userName = userName)

            Spacer(modifier = Modifier.height(Dimensions.spacingLg))

            // Mood Tracker
            MoodTracker(
                todayMood = todayMood,
                moodStreak = moodStreak,
                onMoodSelected = { mood ->
                    viewModel.logMood(mood)
                }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Connection status
            if (!isConnected) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    size = GlassCardSize.Small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline - Connect to backend",
                            style = MaterialTheme.typography.bodySmall,
                            color = Warning
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.spacingMd))
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealWave)
                }
                Spacer(modifier = Modifier.height(Dimensions.spacingMd))
            }

            // Quick Stats
            QuickStatsSection(stats = stats)

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Quick Actions
            Text(
                text = "Quick Capture",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMd))

            QuickActionRow(
                onNewNote = onNavigateToNewNote,
                onVoiceMemo = onNavigateToVoice,
                onPhotoCapture = { /* TODO: Navigate to photo capture */ }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Recent Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                GlassButton(
                    text = "See All",
                    onClick = onNavigateToNotes,
                    style = GlassButtonStyle.Ghost,
                    size = GlassButtonSize.Small
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMd))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 20.dp)
            ) {
                items(recentNotes) { note ->
                    HorizontalNoteCard(
                        note = note,
                        onClick = { onNavigateToNoteDetail(note.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // AI Insights Card
            AIInsightsCard(onNavigateToSearch = onNavigateToSearch)

            Spacer(modifier = Modifier.height(Dimensions.spacingXl))

            // Memory Game Card
            MemoryGameCard(onNavigateToMemoryGame = onNavigateToMemoryGame)

            Spacer(modifier = Modifier.height(Dimensions.bottomNavHeight + Dimensions.spacingXl))
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Entouche",
                style = MaterialTheme.typography.headlineMedium,
                color = TealWave
            )
        }

        GlassIconButton(
            icon = Icons.Outlined.Notifications,
            onClick = { /* TODO: Show notifications */ },
            style = GlassButtonStyle.Secondary
        )
    }
}

@Composable
private fun GreetingSection(userName: String? = null) {
    val greeting = remember {
        val hour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour

        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val displayName = userName?.split(" ")?.firstOrNull() ?: ""
    val greetingText = if (displayName.isNotEmpty()) "$greeting, $displayName" else greeting

    Column {
        Text(
            text = greetingText,
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "What would you like to remember today?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}

@Composable
private fun QuickStatsSection(stats: Map<String, Long>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "Notes",
            value = stats["total"]?.toString() ?: "0",
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Notes,
                    contentDescription = null,
                    tint = TealWave,
                    modifier = Modifier.size(22.dp)
                )
            },
            modifier = Modifier.weight(1f),
            accentColor = TealWave
        )

        StatsCard(
            title = "Voice",
            value = stats["voice"]?.toString() ?: "0",
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Seafoam,
                    modifier = Modifier.size(22.dp)
                )
            },
            modifier = Modifier.weight(1f),
            accentColor = Seafoam
        )

        StatsCard(
            title = "Reminders",
            value = stats["reminders"]?.toString() ?: "0",
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(22.dp)
                )
            },
            modifier = Modifier.weight(1f),
            accentColor = Warning
        )
    }
}

@Composable
private fun MemoryGameCard(
    onNavigateToMemoryGame: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        size = GlassCardSize.Large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Seafoam.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧠",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Memory Game",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Train your memory with custom flashcards",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureChip(text = "Upload Q&A", emoji = "📝")
            FeatureChip(text = "AI Parsing", emoji = "✨")
            FeatureChip(text = "Fun Games", emoji = "🎮")
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassButton(
            text = "Play Now",
            onClick = onNavigateToMemoryGame,
            style = GlassButtonStyle.Accent,
            size = GlassButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Filled.SportsEsports
        )
    }
}

@Composable
private fun FeatureChip(
    text: String,
    emoji: String
) {
    Row(
        modifier = Modifier
            .background(
                color = GlassSurface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun AIInsightsCard(
    onNavigateToSearch: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        size = GlassCardSize.Large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = TealWave.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = TealWave,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You have 3 action items from your recent notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample insight items
        InsightItem(
            text = "Call dentist - from voice memo yesterday",
            color = Seafoam
        )
        Spacer(modifier = Modifier.height(8.dp))
        InsightItem(
            text = "Review Q2 deliverables - from meeting notes",
            color = TealWave
        )
        Spacer(modifier = Modifier.height(8.dp))
        InsightItem(
            text = "Buy groceries for pasta recipe",
            color = AquaMist
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlassButton(
            text = "Explore with AI",
            onClick = onNavigateToSearch,
            style = GlassButtonStyle.Accent,
            size = GlassButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Filled.Search
        )
    }
}

@Composable
private fun InsightItem(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = color,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

/**
 * Format timestamp string to a human-readable relative time
 */
private fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val now = Clock.System.now()
        val diff = now - instant

        when {
            diff.inWholeMinutes < 1 -> "Just now"
            diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes}m ago"
            diff.inWholeHours < 24 -> "${diff.inWholeHours}h ago"
            diff.inWholeDays < 7 -> "${diff.inWholeDays}d ago"
            else -> {
                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${localDateTime.month.name.take(3)} ${localDateTime.dayOfMonth}"
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}
