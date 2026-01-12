package en.entouche.game

import en.entouche.audio.TranscriptionConfig
import en.entouche.data.models.Difficulty
import en.entouche.data.models.FlashCard
import en.entouche.data.models.ParsedCard
import en.entouche.data.models.ParsedQAResponse
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Service for parsing Q&A content using Groq LLM
 */
class QAParserService {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Parse text content into flashcards using LLM
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun parseContent(content: String): Result<List<FlashCard>> {
        return try {
            val apiKey = TranscriptionConfig.apiKey
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured"))
            }

            println("QAParser: Parsing content of ${content.length} characters")

            val prompt = """
You are a helpful assistant that extracts question-answer pairs from text content.
Parse the following content and extract all question-answer pairs.

Return a JSON object with this structure:
{
    "cards": [
        {
            "question": "The question text",
            "answer": "The answer text",
            "hint": "Optional hint for the question",
            "difficulty": "easy" or "medium" or "hard"
        }
    ],
    "suggestedName": "A suggested name for this flashcard deck",
    "suggestedCategory": "A category like 'Science', 'History', etc."
}

Content to parse:
$content

Important:
- Extract ALL question-answer pairs you can find
- If the content is in Q&A format, parse directly
- If it's study material, create questions from key facts
- Keep questions clear and concise
- Provide helpful hints when possible
- Estimate difficulty based on complexity
- Return valid JSON only
"""

            val requestBody = GroqChatRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    GroqMessage(role = "user", content = prompt)
                ),
                temperature = 0.3,
                maxTokens = 4096
            )

            val response = httpClient.post("https://api.groq.com/openai/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(GroqChatRequest.serializer(), requestBody))
            }

            println("QAParser: Response status ${response.status}")

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                println("QAParser: Response received")

                val chatResponse = json.decodeFromString<GroqChatResponse>(responseBody)
                val assistantMessage = chatResponse.choices.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("No response from LLM"))

                // Extract JSON from response (might be wrapped in markdown)
                val jsonContent = extractJson(assistantMessage)

                val parsed = json.decodeFromString<ParsedQAResponse>(jsonContent)

                val flashCards = parsed.cards.mapIndexed { index, card ->
                    FlashCard(
                        id = Uuid.random().toString(),
                        question = card.question,
                        answer = card.answer,
                        hint = card.hint,
                        category = parsed.suggestedCategory,
                        difficulty = when (card.difficulty?.lowercase()) {
                            "easy" -> Difficulty.EASY
                            "hard" -> Difficulty.HARD
                            else -> Difficulty.MEDIUM
                        }
                    )
                }

                println("QAParser: Extracted ${flashCards.size} flashcards")
                Result.success(flashCards)
            } else {
                val errorBody = response.bodyAsText()
                println("QAParser: Error $errorBody")
                Result.failure(Exception("Failed to parse content: ${response.status}"))
            }
        } catch (e: Exception) {
            println("QAParser: Exception ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Generate quiz options for a flashcard
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun generateQuizOptions(
        correctAnswer: String,
        allAnswers: List<String>,
        count: Int = 4
    ): List<String> {
        // First try to use other answers from the deck as distractors
        val otherAnswers = allAnswers.filter { it != correctAnswer }.shuffled()

        if (otherAnswers.size >= count - 1) {
            val options = (otherAnswers.take(count - 1) + correctAnswer).shuffled()
            return options
        }

        // If not enough answers in deck, generate with LLM
        return try {
            val apiKey = TranscriptionConfig.apiKey
            val prompt = """
Generate ${count - 1} plausible but incorrect answers for this question.
The correct answer is: "$correctAnswer"

Return only a JSON array of strings with the wrong answers.
Example: ["wrong1", "wrong2", "wrong3"]

Make the wrong answers believable but clearly incorrect.
"""

            val requestBody = GroqChatRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(GroqMessage(role = "user", content = prompt)),
                temperature = 0.7,
                maxTokens = 256
            )

            val response = httpClient.post("https://api.groq.com/openai/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(GroqChatRequest.serializer(), requestBody))
            }

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val chatResponse = json.decodeFromString<GroqChatResponse>(responseBody)
                val content = chatResponse.choices.firstOrNull()?.message?.content ?: "[]"
                val jsonContent = extractJson(content)
                val wrongAnswers = json.decodeFromString<List<String>>(jsonContent)
                (wrongAnswers.take(count - 1) + correctAnswer).shuffled()
            } else {
                // Fallback: just return the correct answer with placeholders
                listOf(correctAnswer, "Option A", "Option B", "Option C").shuffled()
            }
        } catch (e: Exception) {
            listOf(correctAnswer, "Option A", "Option B", "Option C").shuffled()
        }
    }

    private fun extractJson(text: String): String {
        // Try to extract JSON from markdown code blocks
        val codeBlockPattern = "```(?:json)?\\s*([\\s\\S]*?)```".toRegex()
        val match = codeBlockPattern.find(text)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // Try to find JSON object or array directly
        val jsonStart = text.indexOfFirst { it == '{' || it == '[' }
        val jsonEnd = text.indexOfLast { it == '}' || it == ']' }

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1)
        }

        return text.trim()
    }

    fun close() {
        httpClient.close()
    }
}

@Serializable
private data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    @kotlinx.serialization.SerialName("max_tokens")
    val maxTokens: Int = 1024
)

@Serializable
private data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
private data class GroqChatResponse(
    val choices: List<GroqChoice>
)

@Serializable
private data class GroqChoice(
    val message: GroqMessage
)
