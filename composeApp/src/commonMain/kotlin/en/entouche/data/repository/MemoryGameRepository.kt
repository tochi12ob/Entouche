package en.entouche.data.repository

import en.entouche.data.SupabaseClient
import en.entouche.data.models.*
import en.entouche.game.QAParserService
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MemoryGameRepository {
    private val postgrest = SupabaseClient.postgrest
    private val authRepository = AuthRepository()
    private val qaParser = QAParserService()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun getCurrentUserId(): String? = authRepository.currentUserId

    /**
     * Parse text content into flashcards using LLM
     */
    suspend fun parseContentToCards(content: String): Result<List<FlashCard>> {
        return qaParser.parseContent(content)
    }

    /**
     * Create a new card deck
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun createDeck(
        name: String,
        description: String?,
        cards: List<FlashCard>
    ): Result<CardDeck> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val cardsJson = json.encodeToString(cards)

        val createDeck = CreateCardDeck(
            userId = userId,
            name = name,
            description = description,
            cards = cardsJson
        )

        val result = postgrest.from("card_decks")
            .insert(createDeck) {
                select()
            }
            .decodeSingle<CardDeckRow>()

        // Parse the cards back from JSON
        val parsedCards = json.decodeFromString<List<FlashCard>>(result.cards)

        CardDeck(
            id = result.id,
            userId = result.userId,
            name = result.name,
            description = result.description,
            cards = parsedCards,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt
        )
    }

    /**
     * Get all decks for current user
     */
    suspend fun getDecks(): Result<List<CardDeck>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val results = postgrest.from("card_decks")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CardDeckRow>()

        results.map { row ->
            val parsedCards = try {
                json.decodeFromString<List<FlashCard>>(row.cards)
            } catch (e: Exception) {
                emptyList()
            }

            CardDeck(
                id = row.id,
                userId = row.userId,
                name = row.name,
                description = row.description,
                cards = parsedCards,
                createdAt = row.createdAt,
                updatedAt = row.updatedAt,
                timesPlayed = row.timesPlayed,
                bestScore = row.bestScore
            )
        }
    }

    /**
     * Get a single deck by ID
     */
    suspend fun getDeck(deckId: String): Result<CardDeck> = runCatching {
        val result = postgrest.from("card_decks")
            .select {
                filter {
                    eq("id", deckId)
                }
            }
            .decodeSingle<CardDeckRow>()

        val parsedCards = json.decodeFromString<List<FlashCard>>(result.cards)

        CardDeck(
            id = result.id,
            userId = result.userId,
            name = result.name,
            description = result.description,
            cards = parsedCards,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt,
            timesPlayed = result.timesPlayed,
            bestScore = result.bestScore
        )
    }

    /**
     * Delete a deck
     */
    suspend fun deleteDeck(deckId: String): Result<Unit> = runCatching {
        postgrest.from("card_decks")
            .delete {
                filter {
                    eq("id", deckId)
                }
            }
    }

    /**
     * Save game result
     */
    suspend fun saveGameResult(
        deckId: String,
        mode: GameMode,
        score: Int,
        correctAnswers: Int,
        totalCards: Int,
        maxStreak: Int,
        timeTakenMs: Long
    ): Result<GameResult> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val createResult = CreateGameResult(
            userId = userId,
            deckId = deckId,
            mode = mode.name,
            score = score,
            correctAnswers = correctAnswers,
            totalCards = totalCards,
            maxStreak = maxStreak,
            timeTakenMs = timeTakenMs
        )

        val result = postgrest.from("game_results")
            .insert(createResult) {
                select()
            }
            .decodeSingle<GameResult>()

        // Update deck best score if this is higher
        val deck = getDeck(deckId).getOrNull()
        if (deck != null && score > deck.bestScore) {
            postgrest.from("card_decks")
                .update({
                    set("best_score", score)
                    set("times_played", deck.timesPlayed + 1)
                }) {
                    filter {
                        eq("id", deckId)
                    }
                }
        } else if (deck != null) {
            postgrest.from("card_decks")
                .update({
                    set("times_played", deck.timesPlayed + 1)
                }) {
                    filter {
                        eq("id", deckId)
                    }
                }
        }

        result
    }

    /**
     * Get game history for a user
     */
    suspend fun getGameHistory(limit: Int = 10): Result<List<GameResult>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        postgrest.from("game_results")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("played_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<GameResult>()
    }

    /**
     * Get user's game stats
     */
    suspend fun getGameStats(): Result<Map<String, Any>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val results = postgrest.from("game_results")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<GameResult>()

        val totalGames = results.size
        val totalScore = results.sumOf { it.score }
        val avgAccuracy = if (results.isNotEmpty()) {
            results.map { it.accuracy }.average()
        } else 0.0
        val bestStreak = results.maxOfOrNull { it.maxStreak } ?: 0

        mapOf(
            "totalGames" to totalGames,
            "totalScore" to totalScore,
            "avgAccuracy" to avgAccuracy,
            "bestStreak" to bestStreak
        )
    }

    /**
     * Generate quiz options for a card
     */
    suspend fun generateQuizOptions(
        correctAnswer: String,
        allAnswers: List<String>
    ): List<String> {
        return qaParser.generateQuizOptions(correctAnswer, allAnswers)
    }
}

/**
 * Database row representation for CardDeck (cards stored as JSON string)
 */
@kotlinx.serialization.Serializable
private data class CardDeckRow(
    val id: String = "",
    @kotlinx.serialization.SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String? = null,
    val cards: String, // JSON string
    @kotlinx.serialization.SerialName("created_at")
    val createdAt: String? = null,
    @kotlinx.serialization.SerialName("updated_at")
    val updatedAt: String? = null,
    @kotlinx.serialization.SerialName("times_played")
    val timesPlayed: Int = 0,
    @kotlinx.serialization.SerialName("best_score")
    val bestScore: Int = 0
)
