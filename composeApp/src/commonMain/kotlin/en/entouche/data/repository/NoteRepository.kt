package en.entouche.data.repository

import en.entouche.data.api.ApiClient
import en.entouche.data.api.getBaseUrl
import en.entouche.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing notes data
 */
class NoteRepository(
    private val apiClient: ApiClient = ApiClient(baseUrl = getBaseUrl())
) {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun fetchNotes(page: Int = 1, pageSize: Int = 20, type: NoteType? = null) {
        _isLoading.value = true
        _error.value = null

        apiClient.getNotes(page, pageSize, type)
            .onSuccess { response ->
                if (response.success && response.data != null) {
                    _notes.value = response.data.items
                } else {
                    _error.value = response.error ?: "Failed to fetch notes"
                }
            }
            .onFailure { e ->
                _error.value = e.message ?: "Network error"
            }

        _isLoading.value = false
    }

    suspend fun getNote(id: String): Note? {
        return apiClient.getNote(id)
            .getOrNull()
            ?.takeIf { it.success }
            ?.data
    }

    suspend fun createNote(
        title: String,
        content: String,
        type: NoteType = NoteType.TEXT,
        tags: List<String> = emptyList()
    ): Note? {
        _isLoading.value = true

        val result = apiClient.createNote(
            CreateNoteRequest(
                title = title,
                content = content,
                type = type,
                tags = tags
            )
        )

        _isLoading.value = false

        return result.getOrNull()?.takeIf { it.success }?.data?.also {
            _notes.value = listOf(it) + _notes.value
        }
    }

    suspend fun updateNote(id: String, title: String? = null, content: String? = null): Note? {
        val result = apiClient.updateNote(
            id,
            UpdateNoteRequest(title = title, content = content)
        )

        return result.getOrNull()?.takeIf { it.success }?.data?.also { updated ->
            _notes.value = _notes.value.map { if (it.id == id) updated else it }
        }
    }

    suspend fun deleteNote(id: String): Boolean {
        val result = apiClient.deleteNote(id)

        return result.getOrNull()?.success == true.also {
            if (it) {
                _notes.value = _notes.value.filter { note -> note.id != id }
            }
        }
    }

    suspend fun getStats(): Map<String, Long>? {
        return apiClient.getNoteStats()
            .getOrNull()
            ?.takeIf { it.success }
            ?.data
    }

    fun clearError() {
        _error.value = null
    }
}
