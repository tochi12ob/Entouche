package en.entouche.data.repository

import en.entouche.data.SupabaseClient
import en.entouche.data.models.Note
import en.entouche.data.models.NoteType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseNote(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val content: String,
    val type: String = "TEXT",
    val tags: List<String> = emptyList(),
    @SerialName("ai_summary")
    val aiSummary: String? = null,
    @SerialName("action_items")
    val actionItems: List<String> = emptyList(),
    val transcription: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("reminder_time")
    val reminderTime: String? = null,
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun toNote(): Note = Note(
        id = id ?: "",
        userId = userId,
        title = title,
        content = content,
        type = try { NoteType.valueOf(type) } catch (e: Exception) { NoteType.TEXT },
        tags = tags,
        aiSummary = aiSummary,
        actionItems = actionItems,
        transcription = transcription,
        audioUrl = audioUrl,
        imageUrl = imageUrl,
        reminderTime = reminderTime,
        isCompleted = isCompleted,
        createdAt = createdAt ?: Clock.System.now().toString(),
        updatedAt = updatedAt ?: Clock.System.now().toString()
    )
}

@Serializable
data class CreateSupabaseNote(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val content: String,
    val type: String = "TEXT",
    val tags: List<String> = emptyList(),
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("reminder_time")
    val reminderTime: String? = null
)

@Serializable
data class UpdateSupabaseNote(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    @SerialName("ai_summary")
    val aiSummary: String? = null,
    @SerialName("action_items")
    val actionItems: List<String>? = null,
    @SerialName("is_completed")
    val isCompleted: Boolean? = null,
    @SerialName("reminder_time")
    val reminderTime: String? = null
)

class SupabaseNoteRepository {
    private val postgrest = SupabaseClient.postgrest
    private val authRepository = AuthRepository()

    private fun getCurrentUserId(): String? = authRepository.currentUserId

    suspend fun getNotes(page: Int = 1, pageSize: Int = 20, type: NoteType? = null): Result<List<Note>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val offset = (page - 1) * pageSize

        val result = postgrest.from("notes")
            .select {
                filter {
                    eq("user_id", userId)
                    type?.let { eq("type", it.name) }
                }
                order("updated_at", Order.DESCENDING)
                range(offset.toLong(), (offset + pageSize - 1).toLong())
            }
            .decodeList<SupabaseNote>()

        result.map { it.toNote() }
    }

    suspend fun getNote(id: String): Result<Note> = runCatching {
        val result = postgrest.from("notes")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingle<SupabaseNote>()

        result.toNote()
    }

    suspend fun createNote(
        title: String,
        content: String,
        type: NoteType = NoteType.TEXT,
        tags: List<String> = emptyList(),
        audioUrl: String? = null,
        imageUrl: String? = null,
        reminderTime: String? = null
    ): Result<Note> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val newNote = CreateSupabaseNote(
            userId = userId,
            title = title,
            content = content,
            type = type.name,
            tags = tags,
            audioUrl = audioUrl,
            imageUrl = imageUrl,
            reminderTime = reminderTime
        )

        val result = postgrest.from("notes")
            .insert(newNote) {
                select()
            }
            .decodeSingle<SupabaseNote>()

        result.toNote()
    }

    suspend fun updateNote(
        id: String,
        title: String? = null,
        content: String? = null,
        tags: List<String>? = null,
        aiSummary: String? = null,
        actionItems: List<String>? = null,
        isCompleted: Boolean? = null,
        reminderTime: String? = null
    ): Result<Note> = runCatching {
        val updates = UpdateSupabaseNote(
            title = title,
            content = content,
            tags = tags,
            aiSummary = aiSummary,
            actionItems = actionItems,
            isCompleted = isCompleted,
            reminderTime = reminderTime
        )

        val result = postgrest.from("notes")
            .update(updates) {
                filter {
                    eq("id", id)
                }
                select()
            }
            .decodeSingle<SupabaseNote>()

        result.toNote()
    }

    suspend fun deleteNote(id: String): Result<Boolean> = runCatching {
        postgrest.from("notes")
            .delete {
                filter {
                    eq("id", id)
                }
            }
        true
    }

    suspend fun getStats(): Result<Map<String, Long>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val allNotes = postgrest.from("notes")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<SupabaseNote>()

        mapOf(
            "total" to allNotes.size.toLong(),
            "text" to allNotes.count { it.type == "TEXT" }.toLong(),
            "voice" to allNotes.count { it.type == "VOICE" }.toLong(),
            "image" to allNotes.count { it.type == "IMAGE" }.toLong(),
            "reminders" to allNotes.count { it.type == "REMINDER" }.toLong()
        )
    }

    suspend fun searchNotes(query: String): Result<List<Note>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        val result = postgrest.from("notes")
            .select {
                filter {
                    eq("user_id", userId)
                    or {
                        ilike("title", "%$query%")
                        ilike("content", "%$query%")
                    }
                }
                order("updated_at", Order.DESCENDING)
            }
            .decodeList<SupabaseNote>()

        result.map { it.toNote() }
    }
}
