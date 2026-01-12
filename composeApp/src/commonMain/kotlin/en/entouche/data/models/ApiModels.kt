package en.entouche.data.models

import kotlinx.serialization.Serializable

// API Response wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

// Note models
@Serializable
data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val type: NoteType,
    val tags: List<String> = emptyList(),
    val aiSummary: String? = null,
    val actionItems: List<String> = emptyList(),
    val transcription: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val reminderTime: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class NoteType {
    TEXT, VOICE, PHOTO, REMINDER
}

@Serializable
data class CreateNoteRequest(
    val title: String,
    val content: String,
    val type: NoteType = NoteType.TEXT,
    val tags: List<String> = emptyList(),
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val reminderTime: String? = null
)

@Serializable
data class UpdateNoteRequest(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    val reminderTime: String? = null,
    val isCompleted: Boolean? = null
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)

// AI models
@Serializable
data class SummarizeRequest(
    val text: String
)

@Serializable
data class SummarizeResponse(
    val summary: String,
    val keyPoints: List<String>,
    val actionItems: List<String>
)

@Serializable
data class SemanticSearchRequest(
    val query: String,
    val limit: Int = 10
)

@Serializable
data class SearchResult(
    val noteId: String,
    val title: String,
    val preview: String,
    val score: Float,
    val highlights: List<String> = emptyList()
)

@Serializable
data class SemanticSearchResponse(
    val results: List<SearchResult>,
    val aiAnswer: String? = null
)

@Serializable
data class InsightsResponse(
    val actionItems: List<ActionItem>,
    val upcomingReminders: List<ReminderItem> = emptyList(),
    val recentTopics: List<String>,
    val suggestions: List<String>
)

@Serializable
data class ActionItem(
    val text: String,
    val sourceNoteId: String,
    val sourceNoteTitle: String,
    val priority: String
)

@Serializable
data class ReminderItem(
    val noteId: String,
    val title: String,
    val reminderTime: String
)

@Serializable
data class TranscribeRequest(
    val audioUrl: String
)

@Serializable
data class TranscribeResponse(
    val transcription: String,
    val summary: String,
    val actionItems: List<String>
)

// Health check
@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val version: String
)

// User models
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val createdAt: String
)
