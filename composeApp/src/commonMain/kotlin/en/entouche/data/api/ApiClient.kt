package en.entouche.data.api

import en.entouche.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API Client for communicating with the Entouche backend
 */
class ApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080", // Android emulator localhost
    private val userId: String = "00000000-0000-0000-0000-000000000001" // Valid UUID format
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
        }
        defaultRequest {
            header("X-User-Id", userId)
            contentType(ContentType.Application.Json)
        }
    }

    // Health check
    suspend fun healthCheck(): Result<HealthResponse> = runCatching {
        client.get("$baseUrl/health").body()
    }

    // Notes API
    suspend fun getNotes(page: Int = 1, pageSize: Int = 20, type: NoteType? = null): Result<ApiResponse<PaginatedResponse<Note>>> = runCatching {
        client.get("$baseUrl/api/v1/notes") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            type?.let { parameter("type", it.name) }
        }.body()
    }

    suspend fun getNote(id: String): Result<ApiResponse<Note>> = runCatching {
        client.get("$baseUrl/api/v1/notes/$id").body()
    }

    suspend fun createNote(request: CreateNoteRequest): Result<ApiResponse<Note>> = runCatching {
        client.post("$baseUrl/api/v1/notes") {
            setBody(request)
        }.body()
    }

    suspend fun updateNote(id: String, request: UpdateNoteRequest): Result<ApiResponse<Note>> = runCatching {
        client.put("$baseUrl/api/v1/notes/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deleteNote(id: String): Result<ApiResponse<Map<String, Boolean>>> = runCatching {
        client.delete("$baseUrl/api/v1/notes/$id").body()
    }

    suspend fun getNoteStats(): Result<ApiResponse<Map<String, Long>>> = runCatching {
        client.get("$baseUrl/api/v1/notes/stats").body()
    }

    // AI API
    suspend fun summarize(text: String): Result<ApiResponse<SummarizeResponse>> = runCatching {
        client.post("$baseUrl/api/v1/ai/summarize") {
            setBody(SummarizeRequest(text))
        }.body()
    }

    suspend fun semanticSearch(query: String, limit: Int = 10): Result<ApiResponse<SemanticSearchResponse>> = runCatching {
        client.post("$baseUrl/api/v1/ai/search") {
            setBody(SemanticSearchRequest(query, limit))
        }.body()
    }

    suspend fun getInsights(): Result<ApiResponse<InsightsResponse>> = runCatching {
        client.post("$baseUrl/api/v1/ai/insights") {
            setBody(emptyMap<String, String>())
        }.body()
    }

    suspend fun transcribe(audioUrl: String): Result<ApiResponse<TranscribeResponse>> = runCatching {
        client.post("$baseUrl/api/v1/ai/transcribe") {
            setBody(TranscribeRequest(audioUrl))
        }.body()
    }

    suspend fun extractActions(text: String): Result<ApiResponse<Map<String, List<String>>>> = runCatching {
        client.post("$baseUrl/api/v1/ai/extract-actions") {
            setBody(SummarizeRequest(text))
        }.body()
    }

    // User API
    suspend fun getCurrentUser(): Result<ApiResponse<User>> = runCatching {
        client.get("$baseUrl/api/v1/users/me").body()
    }

    fun close() {
        client.close()
    }
}

/**
 * Platform-specific base URL configuration
 */
expect fun getBaseUrl(): String
