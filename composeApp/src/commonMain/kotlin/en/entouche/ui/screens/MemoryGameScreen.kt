package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import en.entouche.data.models.*
import en.entouche.ui.components.*
import en.entouche.ui.theme.*
import en.entouche.ui.viewmodel.FriendModePhase
import en.entouche.ui.viewmodel.GameScreen
import en.entouche.ui.viewmodel.MemoryGameViewModel
import en.entouche.util.rememberFilePickerHandler

@Composable
fun MemoryGameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: MemoryGameViewModel = viewModel { MemoryGameViewModel() }

    val uiState by viewModel.uiState.collectAsState()
    val decks by viewModel.decks.collectAsState()
    val currentDeck by viewModel.currentDeck.collectAsState()
    val gameSession by viewModel.gameSession.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()
    val error by viewModel.error.collectAsState()

    GradientBackground(
        modifier = modifier,
        animated = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar - only show when not playing
            if (uiState.screen != GameScreen.PLAYING && uiState.screen != GameScreen.RESULT) {
                MemoryGameTopBar(
                    currentScreen = uiState.screen,
                    onBack = {
                        when (uiState.screen) {
                            GameScreen.DECK_LIST -> onNavigateBack()
                            GameScreen.CREATE_DECK -> viewModel.navigateToScreen(GameScreen.DECK_LIST)
                            GameScreen.DECK_DETAIL -> viewModel.navigateToScreen(GameScreen.DECK_LIST)
                            GameScreen.MODE_SELECT -> viewModel.navigateToScreen(GameScreen.DECK_DETAIL)
                            else -> viewModel.navigateToScreen(GameScreen.DECK_LIST)
                        }
                    }
                )
            }

            // Error display
            error?.let { errorMessage ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    size = GlassCardSize.Small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Error,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = TextMuted
                            )
                        }
                    }
                }
            }

            // Main content
            AnimatedContent(
                targetState = uiState.screen,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                            fadeOut() + slideOutHorizontally()
                },
                modifier = Modifier.weight(1f)
            ) { screen ->
                when (screen) {
                    GameScreen.DECK_LIST -> DeckListContent(
                        decks = decks,
                        isLoading = isLoading,
                        onDeckSelected = { viewModel.selectDeck(it) },
                        onCreateDeck = { viewModel.navigateToScreen(GameScreen.CREATE_DECK) }
                    )
                    GameScreen.CREATE_DECK -> CreateDeckContent(
                        isParsing = isParsing,
                        onCreateDeck = { content, name, desc ->
                            viewModel.parseAndCreateDeck(content, name, desc)
                        }
                    )
                    GameScreen.DECK_DETAIL -> currentDeck?.let { deck ->
                        DeckDetailContent(
                            deck = deck,
                            onStartGame = { viewModel.navigateToScreen(GameScreen.MODE_SELECT) },
                            onDeleteDeck = { viewModel.deleteDeck(deck.id) }
                        )
                    }
                    GameScreen.MODE_SELECT -> ModeSelectContent(
                        selectedMode = uiState.selectedMode,
                        onModeSelected = { viewModel.selectMode(it) },
                        onStartGame = { viewModel.startGame() }
                    )
                    GameScreen.PLAYING -> {
                        // Friend mode has its own content
                        if (uiState.selectedMode == GameMode.FRIEND) {
                            FriendModeContent(viewModel = viewModel)
                        } else {
                            GamePlayContent(viewModel = viewModel)
                        }
                    }
                    GameScreen.RESULT -> {
                        // Friend mode uses different state
                        if (uiState.selectedMode == GameMode.FRIEND) {
                            FriendModeResultContent(
                                viewModel = viewModel,
                                onPlayAgain = { viewModel.playAgain() },
                                onExit = { viewModel.navigateToScreen(GameScreen.DECK_LIST) }
                            )
                        } else {
                            gameSession?.let { session ->
                                GameResultCard(
                                    score = session.score,
                                    correctAnswers = session.correctAnswers,
                                    totalCards = session.totalCards,
                                    maxStreak = session.maxStreak,
                                    accuracy = session.accuracy,
                                    onPlayAgain = { viewModel.playAgain() },
                                    onExit = { viewModel.navigateToScreen(GameScreen.DECK_LIST) }
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
private fun MemoryGameTopBar(
    currentScreen: GameScreen,
    onBack: () -> Unit
) {
    val title = when (currentScreen) {
        GameScreen.DECK_LIST -> "Memory Game"
        GameScreen.CREATE_DECK -> "Create Deck"
        GameScreen.DECK_DETAIL -> "Deck Details"
        GameScreen.MODE_SELECT -> "Select Mode"
        else -> "Memory Game"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBack,
            style = GlassButtonStyle.Ghost
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeckListContent(
    decks: List<CardDeck>,
    isLoading: Boolean,
    onDeckSelected: (CardDeck) -> Unit,
    onCreateDeck: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Decks",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "${decks.size} deck${if (decks.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            GlassButton(
                text = "New Deck",
                onClick = onCreateDeck,
                style = GlassButtonStyle.Accent,
                size = GlassButtonSize.Small,
                leadingIcon = Icons.Filled.Add
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealWave)
            }
        } else if (decks.isEmpty()) {
            EmptyDecksContent(onCreateDeck = onCreateDeck)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(decks) { deck ->
                    DeckCard(
                        deck = deck,
                        onClick = { onDeckSelected(deck) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDecksContent(onCreateDeck: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.padding(32.dp),
            size = GlassCardSize.Large
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🧠",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No decks yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your first flashcard deck to start learning!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                GlassButton(
                    text = "Create Deck",
                    onClick = onCreateDeck,
                    style = GlassButtonStyle.Accent,
                    leadingIcon = Icons.Filled.Add
                )
            }
        }
    }
}

@Composable
private fun DeckCard(
    deck: CardDeck,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        size = GlassCardSize.Medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deck icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TealWave.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🃏",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${deck.cardCount} cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (deck.timesPlayed > 0) {
                    Text(
                        text = "Best score: ${deck.bestScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealWave
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}

@Composable
private fun CreateDeckContent(
    isParsing: Boolean,
    onCreateDeck: (content: String, name: String, description: String?) -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var deckDescription by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // File picker handler
    val filePicker = rememberFilePickerHandler(
        onResult = { result ->
            if (result != null) {
                content = result.content
                uploadedFileName = result.fileName
                uploadError = null
                // Auto-fill deck name from file name if empty
                if (deckName.isBlank()) {
                    deckName = result.fileName
                        .substringBeforeLast(".")
                        .replace("_", " ")
                        .replace("-", " ")
                }
            }
        },
        onError = { error ->
            uploadError = error
            uploadedFileName = null
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create a New Deck",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Upload a document or paste your Q&A content",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Upload document button
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { filePicker.launchFilePicker() }
                ),
            size = GlassCardSize.Medium
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.UploadFile,
                    contentDescription = null,
                    tint = TealWave,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (uploadedFileName != null) "File loaded!" else "Upload Document",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uploadedFileName != null) TealWave else TextPrimary
                    )
                    Text(
                        text = uploadedFileName ?: "Supports .txt, .md, .csv, .json files",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uploadedFileName != null) Seafoam else TextSecondary
                    )
                }
            }
        }

        // Upload error
        uploadError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider with "OR"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = GlassBorder
            )
            Text(
                text = "  OR  ",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = GlassBorder
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deck name
        GlassTextField(
            value = deckName,
            onValueChange = { deckName = it },
            placeholder = "Deck name",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description (optional)
        GlassTextField(
            value = deckDescription,
            onValueChange = { deckDescription = it },
            placeholder = "Description (optional)",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content input
        Text(
            text = "Paste your Q&A content",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        GlassTextArea(
            value = content,
            onValueChange = { content = it },
            placeholder = "Example:\nQ: What is photosynthesis?\nA: The process by which plants convert light into energy...\n\nOr paste study notes, and AI will generate questions!",
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            maxLines = 15
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Format tips
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            size = GlassCardSize.Small
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supported formats",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Q: ... A: ... format\n• Question? Answer format\n• Study notes (AI generates Q&A)\n• Any text with facts to learn",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create button
        GlassButton(
            text = if (isParsing) "Creating..." else "Create Deck",
            onClick = {
                if (deckName.isNotBlank() && content.isNotBlank()) {
                    onCreateDeck(content, deckName, deckDescription.ifBlank { null })
                }
            },
            style = GlassButtonStyle.Accent,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isParsing && deckName.isNotBlank() && content.isNotBlank(),
            leadingIcon = if (isParsing) null else Icons.Filled.AutoAwesome
        )

        if (isParsing) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = TealWave,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI is creating your flashcards...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DeckDetailContent(
    deck: CardDeck,
    onStartGame: () -> Unit,
    onDeleteDeck: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Deck header
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            size = GlassCardSize.Large
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🃏",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                deck.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${deck.cardCount}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TealWave,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${deck.timesPlayed}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Seafoam,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Plays",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${deck.bestScore}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Warning,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Best",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preview cards
        Text(
            text = "Preview Cards",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(deck.cards.take(5)) { card ->
                CardPreviewItem(card)
            }

            if (deck.cards.size > 5) {
                item {
                    Text(
                        text = "+ ${deck.cards.size - 5} more cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = "Delete",
                onClick = { showDeleteDialog = true },
                style = GlassButtonStyle.Secondary,
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.Filled.Delete
            )
            GlassButton(
                text = "Play",
                onClick = onStartGame,
                style = GlassButtonStyle.Accent,
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.Filled.PlayArrow
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Deck?") },
            text = { Text("This will permanently delete \"${deck.name}\" and all its cards.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteDeck()
                }) {
                    Text("Delete", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CardPreviewItem(card: FlashCard) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        size = GlassCardSize.Small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            DifficultyBadge(card.difficulty)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun ModeSelectContent(
    selectedMode: GameMode?,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose Game Mode",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select how you want to practice",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(GameMode.entries) { mode ->
                GameModeCard(
                    mode = mode,
                    isSelected = selectedMode == mode,
                    onClick = { onModeSelected(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassButton(
            text = "Start Game",
            onClick = onStartGame,
            style = GlassButtonStyle.Accent,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedMode != null,
            leadingIcon = Icons.Filled.PlayArrow
        )
    }
}

@Composable
private fun GamePlayContent(
    viewModel: MemoryGameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameSession by viewModel.gameSession.collectAsState()
    val currentCard by viewModel.currentCard.collectAsState()
    val quizOptions by viewModel.quizOptions.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isAnswerRevealed by viewModel.isAnswerRevealed.collectAsState()
    val timeLeftMs by viewModel.timeLeftMs.collectAsState()

    val session = gameSession ?: return
    val card = currentCard ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Game header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = session.progress,
                current = session.currentIndex + 1,
                total = session.totalCards
            )

            AnimatedScore(score = session.score)

            StreakIndicator(streak = session.streak)
        }

        // Timer for speed round
        if (uiState.selectedMode == GameMode.SPEED_ROUND) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameTimer(
                    timeLeftMs = timeLeftMs,
                    totalTimeMs = 30000
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Game content based on mode
        when (uiState.selectedMode) {
            GameMode.FLASHCARD -> {
                FlipCard(
                    card = card,
                    isFlipped = isAnswerRevealed,
                    onFlip = { viewModel.flipCard() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = isAnswerRevealed,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Didn't Know",
                            onClick = { viewModel.markCard(false) },
                            style = GlassButtonStyle.Secondary,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Filled.Close
                        )
                        GlassButton(
                            text = "Got It!",
                            onClick = { viewModel.markCard(true) },
                            style = GlassButtonStyle.Accent,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Filled.Check
                        )
                    }
                }
            }

            GameMode.QUIZ, GameMode.SPEED_ROUND -> {
                // Question card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    size = GlassCardSize.Large
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DifficultyBadge(card.difficulty)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = card.question,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    quizOptions.forEachIndexed { index, option ->
                        QuizOptionButton(
                            text = option,
                            index = index,
                            isSelected = selectedAnswer == option,
                            isCorrect = if (isAnswerRevealed) option == card.answer else null,
                            isRevealed = isAnswerRevealed,
                            onClick = {
                                viewModel.selectAnswer(option)
                                viewModel.submitAnswer(option)
                            }
                        )
                    }
                }
            }

            GameMode.MATCH -> {
                // Match mode - simplified for now
                Text(
                    text = "Match mode coming soon!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            GameMode.FRIEND -> {
                // Friend mode is handled separately
            }

            null -> {}
        }
    }
}

@Composable
private fun FriendModeResultContent(
    viewModel: MemoryGameViewModel,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val friendScore by viewModel.friendScore.collectAsState()
    val friendCorrectAnswers by viewModel.friendCorrectAnswers.collectAsState()
    val friendQuestionNumber by viewModel.friendQuestionNumber.collectAsState()
    val friendMaxStreak by viewModel.friendMaxStreak.collectAsState()

    val totalQuestions = friendQuestionNumber - 1
    val accuracy = if (totalQuestions > 0) friendCorrectAnswers.toFloat() / totalQuestions else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            size = GlassCardSize.Large
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy or celebration emoji based on performance
                Text(
                    text = when {
                        accuracy >= 0.8f -> "🏆"
                        accuracy >= 0.6f -> "🌟"
                        accuracy >= 0.4f -> "👏"
                        else -> "💪"
                    },
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Game Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Great teamwork!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$friendScore",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TealWave,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$friendCorrectAnswers/$totalQuestions",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Seafoam,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Correct",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$friendMaxStreak",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Warning,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Best Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Accuracy bar
                Text(
                    text = "${(accuracy * 100).toInt()}% Accuracy",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Exit",
                        onClick = onExit,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Play Again",
                        onClick = onPlayAgain,
                        style = GlassButtonStyle.Accent,
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Filled.Replay
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendModeContent(
    viewModel: MemoryGameViewModel
) {
    val friendModePhase by viewModel.friendModePhase.collectAsState()
    val friendQuestion by viewModel.friendQuestion.collectAsState()
    val friendAnswer by viewModel.friendAnswer.collectAsState()
    val friendQuestionNumber by viewModel.friendQuestionNumber.collectAsState()
    val friendScore by viewModel.friendScore.collectAsState()
    val friendCorrectAnswers by viewModel.friendCorrectAnswers.collectAsState()
    val friendStreak by viewModel.friendStreak.collectAsState()
    val lastAnswerCorrect by viewModel.lastAnswerCorrect.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scoreboard header
        FriendModeScoreboard(
            score = friendScore,
            correctAnswers = friendCorrectAnswers,
            totalQuestions = friendQuestionNumber - 1,
            streak = friendStreak
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Content based on phase
        when (friendModePhase) {
            FriendModePhase.QUIZ_MASTER_TURN -> {
                QuizMasterCard(
                    questionNumber = friendQuestionNumber,
                    onQuestionSubmit = { question ->
                        viewModel.submitFriendQuestion(question)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // End game button
                if (friendQuestionNumber > 1) {
                    GlassButton(
                        text = "End Game",
                        onClick = { viewModel.endFriendMode() },
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            FriendModePhase.WAITING_FOR_PLAYER -> {
                WaitingForFriend(
                    message = "Pass to your friend"
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = "I'm Ready to Answer!",
                    onClick = { viewModel.confirmHandoffToPlayer() },
                    style = GlassButtonStyle.Accent,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Filled.Person
                )
            }

            FriendModePhase.PLAYER_TURN -> {
                PlayerAnswerCard(
                    question = friendQuestion,
                    questionNumber = friendQuestionNumber,
                    onAnswerSubmit = { answer ->
                        viewModel.submitFriendAnswer(answer)
                    }
                )
            }

            FriendModePhase.WAITING_FOR_QUIZ_MASTER -> {
                WaitingForFriend(
                    message = "Pass back to Quiz Master"
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = "I'm the Quiz Master!",
                    onClick = { viewModel.confirmHandoffToQuizMaster() },
                    style = GlassButtonStyle.Accent,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Filled.Person
                )
            }

            FriendModePhase.JUDGING -> {
                AnswerRevealCard(
                    question = friendQuestion,
                    playerAnswer = friendAnswer,
                    onJudge = { isCorrect ->
                        viewModel.judgeFriendAnswer(isCorrect)
                    }
                )
            }

            FriendModePhase.FEEDBACK -> {
                lastAnswerCorrect?.let { isCorrect ->
                    AnswerFeedback(
                        isCorrect = isCorrect,
                        onContinue = { viewModel.nextFriendQuestion() }
                    )
                }
            }
        }
    }
}
