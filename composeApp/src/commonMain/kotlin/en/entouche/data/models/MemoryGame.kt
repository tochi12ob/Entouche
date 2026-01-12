package en.entouche.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single flashcard with question and answer
 */
@Serializable
data class FlashCard(
    val id: String = "",
    val question: String,
    val answer: String,
    val hint: String? = null,
    val category: String? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    var timesReviewed: Int = 0,
    var timesCorrect: Int = 0,
    var lastReviewed: Long? = null
) {
    val successRate: Float
        get() = if (timesReviewed > 0) timesCorrect.toFloat() / timesReviewed else 0f
}

/**
 * Difficulty levels for cards
 */
@Serializable
enum class Difficulty(val points: Int, val emoji: String) {
    EASY(10, "🟢"),
    MEDIUM(20, "🟡"),
    HARD(30, "🔴")
}

/**
 * Game modes available
 */
enum class GameMode(val title: String, val description: String, val emoji: String) {
    FLASHCARD("Flashcards", "Flip cards to reveal answers", "🃏"),
    QUIZ("Quiz Mode", "Multiple choice questions", "📝"),
    MATCH("Match Game", "Match questions with answers", "🎯"),
    SPEED_ROUND("Speed Round", "Answer quickly for bonus points", "⚡"),
    FRIEND("Play with Friend", "A friend asks, you answer!", "👥")
}

/**
 * A complete game session
 */
@Serializable
data class GameSession(
    val id: String = "",
    val deckId: String,
    val deckName: String,
    val mode: String,
    val cards: List<FlashCard>,
    val startTime: Long,
    var endTime: Long? = null,
    var currentIndex: Int = 0,
    var correctAnswers: Int = 0,
    var incorrectAnswers: Int = 0,
    var score: Int = 0,
    var streak: Int = 0,
    var maxStreak: Int = 0,
    var isComplete: Boolean = false
) {
    val totalCards: Int get() = cards.size
    val progress: Float get() = if (totalCards > 0) currentIndex.toFloat() / totalCards else 0f
    val accuracy: Float get() = if (currentIndex > 0) correctAnswers.toFloat() / currentIndex else 0f
}

/**
 * A deck of flashcards (from uploaded file)
 */
@Serializable
data class CardDeck(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val name: String,
    val description: String? = null,
    val cards: List<FlashCard>,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    var timesPlayed: Int = 0,
    var bestScore: Int = 0
) {
    val cardCount: Int get() = cards.size
}

/**
 * For creating a new deck
 */
@Serializable
data class CreateCardDeck(
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String? = null,
    val cards: String // JSON string of cards
)

/**
 * Game result to save
 */
@Serializable
data class GameResult(
    val id: String = "",
    @SerialName("user_id")
    val userId: String,
    @SerialName("deck_id")
    val deckId: String,
    val mode: String,
    val score: Int,
    @SerialName("correct_answers")
    val correctAnswers: Int,
    @SerialName("total_cards")
    val totalCards: Int,
    @SerialName("max_streak")
    val maxStreak: Int,
    @SerialName("time_taken_ms")
    val timeTakenMs: Long,
    @SerialName("played_at")
    val playedAt: String? = null
) {
    val accuracy: Float get() = if (totalCards > 0) correctAnswers.toFloat() / totalCards else 0f
}

/**
 * For creating game result
 */
@Serializable
data class CreateGameResult(
    @SerialName("user_id")
    val userId: String,
    @SerialName("deck_id")
    val deckId: String,
    val mode: String,
    val score: Int,
    @SerialName("correct_answers")
    val correctAnswers: Int,
    @SerialName("total_cards")
    val totalCards: Int,
    @SerialName("max_streak")
    val maxStreak: Int,
    @SerialName("time_taken_ms")
    val timeTakenMs: Long
)

/**
 * LLM response for parsed Q&A
 */
@Serializable
data class ParsedQAResponse(
    val cards: List<ParsedCard>,
    val suggestedName: String? = null,
    val suggestedCategory: String? = null
)

@Serializable
data class ParsedCard(
    val question: String,
    val answer: String,
    val hint: String? = null,
    val difficulty: String? = null
)
