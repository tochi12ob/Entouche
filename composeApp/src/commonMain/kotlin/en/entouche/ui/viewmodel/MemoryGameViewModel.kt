package en.entouche.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import en.entouche.data.models.*
import en.entouche.data.repository.MemoryGameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ViewModel for the Memory Game feature
 */
class MemoryGameViewModel : ViewModel() {
    private val repository = MemoryGameRepository()

    // UI State
    private val _uiState = MutableStateFlow(MemoryGameUiState())
    val uiState: StateFlow<MemoryGameUiState> = _uiState.asStateFlow()

    // Decks
    private val _decks = MutableStateFlow<List<CardDeck>>(emptyList())
    val decks: StateFlow<List<CardDeck>> = _decks.asStateFlow()

    // Current deck
    private val _currentDeck = MutableStateFlow<CardDeck?>(null)
    val currentDeck: StateFlow<CardDeck?> = _currentDeck.asStateFlow()

    // Game session
    private val _gameSession = MutableStateFlow<GameSession?>(null)
    val gameSession: StateFlow<GameSession?> = _gameSession.asStateFlow()

    // Current card for flashcard mode
    private val _currentCard = MutableStateFlow<FlashCard?>(null)
    val currentCard: StateFlow<FlashCard?> = _currentCard.asStateFlow()

    // Quiz options for quiz mode
    private val _quizOptions = MutableStateFlow<List<String>>(emptyList())
    val quizOptions: StateFlow<List<String>> = _quizOptions.asStateFlow()

    // Selected answer
    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer.asStateFlow()

    // Is answer revealed
    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    // Game history
    private val _gameHistory = MutableStateFlow<List<GameResult>>(emptyList())
    val gameHistory: StateFlow<List<GameResult>> = _gameHistory.asStateFlow()

    // Timer for speed round
    private val _timeLeftMs = MutableStateFlow(0L)
    val timeLeftMs: StateFlow<Long> = _timeLeftMs.asStateFlow()

    private var timerJob: Job? = null

    // Friend mode state
    private val _friendModePhase = MutableStateFlow(FriendModePhase.QUIZ_MASTER_TURN)
    val friendModePhase: StateFlow<FriendModePhase> = _friendModePhase.asStateFlow()

    private val _friendQuestion = MutableStateFlow("")
    val friendQuestion: StateFlow<String> = _friendQuestion.asStateFlow()

    private val _friendAnswer = MutableStateFlow("")
    val friendAnswer: StateFlow<String> = _friendAnswer.asStateFlow()

    private val _friendQuestionNumber = MutableStateFlow(1)
    val friendQuestionNumber: StateFlow<Int> = _friendQuestionNumber.asStateFlow()

    private val _friendScore = MutableStateFlow(0)
    val friendScore: StateFlow<Int> = _friendScore.asStateFlow()

    private val _friendCorrectAnswers = MutableStateFlow(0)
    val friendCorrectAnswers: StateFlow<Int> = _friendCorrectAnswers.asStateFlow()

    private val _friendStreak = MutableStateFlow(0)
    val friendStreak: StateFlow<Int> = _friendStreak.asStateFlow()

    private val _friendMaxStreak = MutableStateFlow(0)
    val friendMaxStreak: StateFlow<Int> = _friendMaxStreak.asStateFlow()

    private val _lastAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerCorrect: StateFlow<Boolean?> = _lastAnswerCorrect.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDecks()
    }

    /**
     * Load all user decks
     */
    fun loadDecks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getDecks()
                .onSuccess { _decks.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    /**
     * Parse content and create a new deck
     */
    @OptIn(ExperimentalUuidApi::class)
    fun parseAndCreateDeck(content: String, name: String, description: String? = null) {
        viewModelScope.launch {
            _isParsing.value = true
            _error.value = null

            repository.parseContentToCards(content)
                .onSuccess { cards ->
                    if (cards.isEmpty()) {
                        _error.value = "No questions found in the content"
                        _isParsing.value = false
                        return@launch
                    }

                    repository.createDeck(name, description, cards)
                        .onSuccess { deck ->
                            _decks.value = listOf(deck) + _decks.value
                            _currentDeck.value = deck
                            _uiState.value = _uiState.value.copy(
                                screen = GameScreen.DECK_DETAIL
                            )
                        }
                        .onFailure { _error.value = it.message }
                }
                .onFailure { _error.value = "Failed to parse content: ${it.message}" }

            _isParsing.value = false
        }
    }

    /**
     * Select a deck
     */
    fun selectDeck(deck: CardDeck) {
        _currentDeck.value = deck
        _uiState.value = _uiState.value.copy(screen = GameScreen.DECK_DETAIL)
    }

    /**
     * Select game mode and start game
     */
    fun selectMode(mode: GameMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    /**
     * Start a new game session
     */
    @OptIn(ExperimentalUuidApi::class)
    fun startGame() {
        val mode = _uiState.value.selectedMode ?: return

        // Friend mode doesn't need a deck - handle separately
        if (mode == GameMode.FRIEND) {
            startFriendMode()
            return
        }

        val deck = _currentDeck.value ?: return

        val shuffledCards = deck.cards.shuffled()

        val session = GameSession(
            id = Uuid.random().toString(),
            deckId = deck.id,
            deckName = deck.name,
            mode = mode.name,
            cards = shuffledCards,
            startTime = System.currentTimeMillis()
        )

        _gameSession.value = session
        _currentCard.value = shuffledCards.firstOrNull()
        _selectedAnswer.value = null
        _isAnswerRevealed.value = false

        // Generate quiz options if in quiz mode
        if (mode == GameMode.QUIZ || mode == GameMode.SPEED_ROUND) {
            generateQuizOptions()
        }

        // Start timer if speed round
        if (mode == GameMode.SPEED_ROUND) {
            startTimer(30000) // 30 seconds per card
        }

        _uiState.value = _uiState.value.copy(screen = GameScreen.PLAYING)
    }

    /**
     * Generate quiz options for current card
     */
    private fun generateQuizOptions() {
        val session = _gameSession.value ?: return
        val card = _currentCard.value ?: return

        viewModelScope.launch {
            val allAnswers = session.cards.map { it.answer }
            val options = repository.generateQuizOptions(card.answer, allAnswers)
            _quizOptions.value = options
        }
    }

    /**
     * Start countdown timer
     */
    private fun startTimer(durationMs: Long) {
        timerJob?.cancel()
        _timeLeftMs.value = durationMs

        timerJob = viewModelScope.launch {
            while (_timeLeftMs.value > 0) {
                delay(100)
                _timeLeftMs.value -= 100

                if (_timeLeftMs.value <= 0) {
                    // Time's up - mark as incorrect
                    submitAnswer(null)
                }
            }
        }
    }

    /**
     * Select an answer in quiz mode
     */
    fun selectAnswer(answer: String) {
        if (_isAnswerRevealed.value) return
        _selectedAnswer.value = answer
    }

    /**
     * Submit the selected answer
     */
    fun submitAnswer(answer: String?) {
        val session = _gameSession.value ?: return
        val card = _currentCard.value ?: return

        timerJob?.cancel()
        _isAnswerRevealed.value = true

        val isCorrect = answer?.lowercase()?.trim() == card.answer.lowercase().trim()
        val mode = _uiState.value.selectedMode ?: GameMode.FLASHCARD

        val points = if (isCorrect) {
            val basePoints = card.difficulty.points
            val streakBonus = if (session.streak >= 5) basePoints / 2 else 0
            val speedBonus = if (mode == GameMode.SPEED_ROUND) {
                ((_timeLeftMs.value / 1000) * 2).toInt()
            } else 0
            basePoints + streakBonus + speedBonus
        } else 0

        val newStreak = if (isCorrect) session.streak + 1 else 0

        _gameSession.value = session.copy(
            correctAnswers = session.correctAnswers + (if (isCorrect) 1 else 0),
            incorrectAnswers = session.incorrectAnswers + (if (!isCorrect) 1 else 0),
            score = session.score + points,
            streak = newStreak,
            maxStreak = maxOf(session.maxStreak, newStreak)
        )

        // Delay before moving to next card
        viewModelScope.launch {
            delay(1500)
            moveToNextCard()
        }
    }

    /**
     * Mark flashcard as known/unknown
     */
    fun markCard(known: Boolean) {
        val session = _gameSession.value ?: return
        val card = _currentCard.value ?: return

        val points = if (known) card.difficulty.points else 0
        val newStreak = if (known) session.streak + 1 else 0

        _gameSession.value = session.copy(
            correctAnswers = session.correctAnswers + (if (known) 1 else 0),
            incorrectAnswers = session.incorrectAnswers + (if (!known) 1 else 0),
            score = session.score + points,
            streak = newStreak,
            maxStreak = maxOf(session.maxStreak, newStreak)
        )

        moveToNextCard()
    }

    /**
     * Move to the next card
     */
    private fun moveToNextCard() {
        val session = _gameSession.value ?: return
        val nextIndex = session.currentIndex + 1

        if (nextIndex >= session.cards.size) {
            // Game complete
            endGame()
            return
        }

        _gameSession.value = session.copy(currentIndex = nextIndex)
        _currentCard.value = session.cards[nextIndex]
        _selectedAnswer.value = null
        _isAnswerRevealed.value = false

        val mode = _uiState.value.selectedMode

        // Generate new quiz options
        if (mode == GameMode.QUIZ || mode == GameMode.SPEED_ROUND) {
            generateQuizOptions()
        }

        // Reset timer for speed round
        if (mode == GameMode.SPEED_ROUND) {
            startTimer(30000)
        }
    }

    /**
     * End the game and save results
     */
    private fun endGame() {
        timerJob?.cancel()

        val session = _gameSession.value ?: return

        val endTime = System.currentTimeMillis()
        val finalSession = session.copy(
            endTime = endTime,
            isComplete = true
        )
        _gameSession.value = finalSession

        // Save game result
        viewModelScope.launch {
            val mode = _uiState.value.selectedMode ?: GameMode.FLASHCARD
            repository.saveGameResult(
                deckId = session.deckId,
                mode = mode,
                score = finalSession.score,
                correctAnswers = finalSession.correctAnswers,
                totalCards = finalSession.totalCards,
                maxStreak = finalSession.maxStreak,
                timeTakenMs = endTime - session.startTime
            )
            loadGameHistory()
        }

        _uiState.value = _uiState.value.copy(screen = GameScreen.RESULT)
    }

    /**
     * Flip flashcard
     */
    fun flipCard() {
        _isAnswerRevealed.value = !_isAnswerRevealed.value
    }

    /**
     * Load game history
     */
    fun loadGameHistory() {
        viewModelScope.launch {
            repository.getGameHistory()
                .onSuccess { _gameHistory.value = it }
                .onFailure { /* Silently fail */ }
        }
    }

    /**
     * Delete a deck
     */
    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
                .onSuccess {
                    _decks.value = _decks.value.filter { it.id != deckId }
                    if (_currentDeck.value?.id == deckId) {
                        _currentDeck.value = null
                        navigateToScreen(GameScreen.DECK_LIST)
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }

    /**
     * Navigate to a specific screen
     */
    fun navigateToScreen(screen: GameScreen) {
        _uiState.value = _uiState.value.copy(screen = screen)

        when (screen) {
            GameScreen.DECK_LIST -> {
                _currentDeck.value = null
                _gameSession.value = null
            }
            GameScreen.CREATE_DECK -> { /* Keep current state */ }
            GameScreen.DECK_DETAIL -> { /* Keep current deck */ }
            GameScreen.MODE_SELECT -> { /* Keep current deck */ }
            GameScreen.PLAYING -> { /* Keep game session */ }
            GameScreen.RESULT -> { /* Keep results */ }
        }
    }

    /**
     * Reset game for replay
     */
    fun playAgain() {
        val deck = _currentDeck.value ?: return
        _uiState.value = _uiState.value.copy(screen = GameScreen.MODE_SELECT)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    // ==================== FRIEND MODE METHODS ====================

    /**
     * Start friend mode game
     */
    fun startFriendMode() {
        _uiState.value = _uiState.value.copy(
            selectedMode = GameMode.FRIEND,
            screen = GameScreen.PLAYING
        )
        resetFriendModeState()
    }

    /**
     * Reset friend mode state
     */
    private fun resetFriendModeState() {
        _friendModePhase.value = FriendModePhase.QUIZ_MASTER_TURN
        _friendQuestion.value = ""
        _friendAnswer.value = ""
        _friendQuestionNumber.value = 1
        _friendScore.value = 0
        _friendCorrectAnswers.value = 0
        _friendStreak.value = 0
        _friendMaxStreak.value = 0
        _lastAnswerCorrect.value = null
    }

    /**
     * Quiz Master submits a question
     */
    fun submitFriendQuestion(question: String) {
        _friendQuestion.value = question
        _friendModePhase.value = FriendModePhase.WAITING_FOR_PLAYER
    }

    /**
     * Confirm handoff to player
     */
    fun confirmHandoffToPlayer() {
        _friendModePhase.value = FriendModePhase.PLAYER_TURN
    }

    /**
     * Player submits their answer
     */
    fun submitFriendAnswer(answer: String) {
        _friendAnswer.value = answer
        _friendModePhase.value = FriendModePhase.WAITING_FOR_QUIZ_MASTER
    }

    /**
     * Confirm handoff back to quiz master for judging
     */
    fun confirmHandoffToQuizMaster() {
        _friendModePhase.value = FriendModePhase.JUDGING
    }

    /**
     * Quiz Master judges the answer
     */
    fun judgeFriendAnswer(isCorrect: Boolean) {
        _lastAnswerCorrect.value = isCorrect

        if (isCorrect) {
            _friendScore.value += 20
            _friendCorrectAnswers.value++
            _friendStreak.value++
            if (_friendStreak.value > _friendMaxStreak.value) {
                _friendMaxStreak.value = _friendStreak.value
            }
        } else {
            _friendStreak.value = 0
        }

        _friendModePhase.value = FriendModePhase.FEEDBACK
    }

    /**
     * Continue to next question in friend mode
     */
    fun nextFriendQuestion() {
        _friendQuestionNumber.value++
        _friendQuestion.value = ""
        _friendAnswer.value = ""
        _lastAnswerCorrect.value = null
        _friendModePhase.value = FriendModePhase.QUIZ_MASTER_TURN
    }

    /**
     * End friend mode and show results
     */
    fun endFriendMode() {
        _uiState.value = _uiState.value.copy(screen = GameScreen.RESULT)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

/**
 * UI State for memory game
 */
data class MemoryGameUiState(
    val screen: GameScreen = GameScreen.DECK_LIST,
    val selectedMode: GameMode? = null
)

/**
 * Screen states
 */
enum class GameScreen {
    DECK_LIST,
    CREATE_DECK,
    DECK_DETAIL,
    MODE_SELECT,
    PLAYING,
    RESULT
}

/**
 * Friend mode phases
 */
enum class FriendModePhase {
    QUIZ_MASTER_TURN,      // Quiz master types a question
    WAITING_FOR_PLAYER,    // Waiting for device handoff to player
    PLAYER_TURN,           // Player answers the question
    WAITING_FOR_QUIZ_MASTER, // Waiting for device handoff to quiz master
    JUDGING,               // Quiz master judges the answer
    FEEDBACK               // Show feedback animation
}
