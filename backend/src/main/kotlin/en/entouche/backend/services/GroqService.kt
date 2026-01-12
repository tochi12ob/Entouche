package en.entouche.backend.services

import en.entouche.backend.config.GroqConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 2048,
    val stream: Boolean = false
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String? = null
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

class GroqService {
    private val client = GroqConfig.httpClient
    private val apiKey = GroqConfig.apiKey
    private val baseUrl = GroqConfig.baseUrl

    suspend fun chatCompletion(
        messages: List<ChatMessage>,
        model: String = GroqConfig.Models.DEFAULT,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): String {
        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            max_tokens = maxTokens
        )

        val response = client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(request)
        }

        val result = response.body<ChatCompletionResponse>()
        return result.choices.firstOrNull()?.message?.content ?: ""
    }

    suspend fun summarize(text: String, maxLength: Int = 200): SummaryResult {
        val systemPrompt = """You are an AI assistant specialized in summarizing notes and extracting key information.
            |When summarizing:
            |1. Create a concise summary (max $maxLength characters)
            |2. Extract key points as a list
            |3. Identify any action items or tasks mentioned
            |
            |Respond in JSON format:
            |{
            |  "summary": "concise summary here",
            |  "keyPoints": ["point 1", "point 2"],
            |  "actionItems": ["task 1", "task 2"]
            |}""".trimMargin()

        val messages = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", "Please summarize this text and extract key information:\n\n$text")
        )

        val response = chatCompletion(messages, temperature = 0.3f)
        return parseSummaryResult(response)
    }

    suspend fun extractActionItems(text: String): List<String> {
        val systemPrompt = """You are an AI assistant that extracts action items and tasks from text.
            |Identify any commitments, to-dos, or action items mentioned.
            |Return them as a JSON array of strings.
            |Example: ["Call dentist tomorrow", "Send report by Friday"]
            |If no action items found, return an empty array: []""".trimMargin()

        val messages = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", text)
        )

        val response = chatCompletion(messages, model = GroqConfig.Models.FAST, temperature = 0.2f)
        return parseActionItems(response)
    }

    suspend fun semanticSearch(query: String, notes: List<NoteContext>): SemanticSearchResult {
        val notesContext = notes.mapIndexed { index, note ->
            "[$index] Title: ${note.title}\nContent: ${note.preview}"
        }.joinToString("\n\n---\n\n")

        val systemPrompt = """You are an AI assistant helping users find relevant notes.
            |Given a search query and a list of notes, identify the most relevant notes and provide a helpful answer.
            |
            |Respond in JSON format:
            |{
            |  "relevantIndices": [0, 2, 5],
            |  "answer": "Based on your notes, here's what I found...",
            |  "confidence": 0.85
            |}""".trimMargin()

        val messages = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", "Query: $query\n\nNotes:\n$notesContext")
        )

        val response = chatCompletion(messages, temperature = 0.3f)
        return parseSemanticSearchResult(response, notes)
    }

    suspend fun generateInsights(notes: List<NoteContext>): InsightsResult {
        val notesContext = notes.map { note ->
            "Title: ${note.title}\nType: ${note.type}\nContent: ${note.preview}\nCreated: ${note.createdAt}"
        }.joinToString("\n\n---\n\n")

        val systemPrompt = """You are an AI assistant analyzing user's notes to provide helpful insights.
            |Analyze the notes and provide:
            |1. Outstanding action items that need attention
            |2. Common topics/themes
            |3. Helpful suggestions based on the content
            |
            |Respond in JSON format:
            |{
            |  "actionItems": [{"text": "action", "priority": "high/medium/low", "source": "note title"}],
            |  "topics": ["topic1", "topic2"],
            |  "suggestions": ["suggestion 1", "suggestion 2"]
            |}""".trimMargin()

        val messages = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", "Please analyze these recent notes:\n\n$notesContext")
        )

        val response = chatCompletion(messages, temperature = 0.4f)
        return parseInsightsResult(response)
    }

    suspend fun transcribeAudio(audioUrl: String): String {
        // Note: Groq's Whisper API requires multipart form data with the actual audio file
        // For this implementation, we'll simulate transcription
        // In production, you'd download the audio and send it to Groq's transcription endpoint

        // This would be the actual implementation:
        // val audioBytes = downloadAudio(audioUrl)
        // val response = client.submitFormWithBinaryData(
        //     url = "$baseUrl/audio/transcriptions",
        //     formData = formData {
        //         append("file", audioBytes, Headers.build {
        //             append(HttpHeaders.ContentDisposition, "filename=audio.wav")
        //         })
        //         append("model", GroqConfig.Models.TRANSCRIPTION)
        //     }
        // ) {
        //     header("Authorization", "Bearer $apiKey")
        // }

        return "Transcription would be performed here with actual audio file"
    }

    private fun parseSummaryResult(response: String): SummaryResult {
        return try {
            val json = Json.parseToJsonElement(response).jsonObject
            SummaryResult(
                summary = json["summary"]?.jsonPrimitive?.content ?: "",
                keyPoints = json["keyPoints"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                actionItems = json["actionItems"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            )
        } catch (e: Exception) {
            SummaryResult(summary = response, keyPoints = emptyList(), actionItems = emptyList())
        }
    }

    private fun parseActionItems(response: String): List<String> {
        return try {
            Json.parseToJsonElement(response).jsonArray.map { it.jsonPrimitive.content }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseSemanticSearchResult(response: String, notes: List<NoteContext>): SemanticSearchResult {
        return try {
            val json = Json.parseToJsonElement(response).jsonObject
            val indices = json["relevantIndices"]?.jsonArray?.map { it.jsonPrimitive.int } ?: emptyList()
            SemanticSearchResult(
                relevantNotes = indices.mapNotNull { notes.getOrNull(it) },
                answer = json["answer"]?.jsonPrimitive?.content,
                confidence = json["confidence"]?.jsonPrimitive?.float ?: 0.5f
            )
        } catch (e: Exception) {
            SemanticSearchResult(relevantNotes = emptyList(), answer = null, confidence = 0f)
        }
    }

    private fun parseInsightsResult(response: String): InsightsResult {
        return try {
            val json = Json.parseToJsonElement(response).jsonObject
            InsightsResult(
                actionItems = json["actionItems"]?.jsonArray?.map { item ->
                    val obj = item.jsonObject
                    ActionItemResult(
                        text = obj["text"]?.jsonPrimitive?.content ?: "",
                        priority = obj["priority"]?.jsonPrimitive?.content ?: "medium",
                        source = obj["source"]?.jsonPrimitive?.content ?: ""
                    )
                } ?: emptyList(),
                topics = json["topics"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                suggestions = json["suggestions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            )
        } catch (e: Exception) {
            InsightsResult(actionItems = emptyList(), topics = emptyList(), suggestions = emptyList())
        }
    }
}

data class SummaryResult(
    val summary: String,
    val keyPoints: List<String>,
    val actionItems: List<String>
)

data class NoteContext(
    val id: String,
    val title: String,
    val preview: String,
    val type: String,
    val createdAt: String
)

data class SemanticSearchResult(
    val relevantNotes: List<NoteContext>,
    val answer: String?,
    val confidence: Float
)

data class InsightsResult(
    val actionItems: List<ActionItemResult>,
    val topics: List<String>,
    val suggestions: List<String>
)

data class ActionItemResult(
    val text: String,
    val priority: String,
    val source: String
)
