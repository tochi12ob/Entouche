package en.entouche.backend.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class NoteType {
    TEXT,
    VOICE,
    PHOTO,
    REMINDER
}

@Serializable
data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val type: NoteType,
    val tags: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val aiSummary: String? = null,
    val actionItems: List<String> = emptyList(),
    val transcription: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val reminderTime: Instant? = null,
    val isCompleted: Boolean = false
)

@Serializable
data class CreateNoteRequest(
    val title: String,
    val content: String,
    val type: NoteType,
    val tags: List<String> = emptyList(),
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val reminderTime: Instant? = null
)

@Serializable
data class UpdateNoteRequest(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    val reminderTime: Instant? = null,
    val isCompleted: Boolean? = null
)

@Serializable
data class VoiceMemo(
    val id: String,
    val userId: String,
    val audioUrl: String,
    val duration: Int, // seconds
    val transcription: String? = null,
    val summary: String? = null,
    val actionItems: List<String> = emptyList(),
    val createdAt: Instant
)

@Serializable
data class TranscribeRequest(
    val audioUrl: String,
    val language: String = "en"
)

@Serializable
data class TranscribeResponse(
    val transcription: String,
    val summary: String?,
    val actionItems: List<String>
)

@Serializable
data class SummarizeRequest(
    val text: String,
    val maxLength: Int = 200
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
data class SemanticSearchResponse(
    val results: List<SearchResult>,
    val aiAnswer: String?
)

@Serializable
data class SearchResult(
    val noteId: String,
    val title: String,
    val preview: String,
    val score: Float,
    val highlights: List<String>
)

@Serializable
data class ExtractInsightsRequest(
    val noteIds: List<String>
)

@Serializable
data class InsightsResponse(
    val actionItems: List<ActionItem>,
    val upcomingReminders: List<ReminderInfo>,
    val recentTopics: List<String>,
    val suggestions: List<String>
)

@Serializable
data class ActionItem(
    val text: String,
    val sourceNoteId: String,
    val sourceNoteTitle: String,
    val priority: String = "medium",
    val dueDate: Instant? = null
)

@Serializable
data class ReminderInfo(
    val noteId: String,
    val title: String,
    val reminderTime: Instant,
    val preview: String
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val createdAt: Instant
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
