package en.entouche.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import en.entouche.data.models.*
import en.entouche.data.repository.MoodRepository
import en.entouche.data.repository.SupabaseNoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the Entouche app
 * Uses Supabase for data storage
 */
class EntoucheViewModel : ViewModel() {
    private val noteRepository = SupabaseNoteRepository()
    private val moodRepository = MoodRepository()

    // Mood state
    private val _todayMood = MutableStateFlow<MoodLog?>(null)
    val todayMood: StateFlow<MoodLog?> = _todayMood.asStateFlow()

    private val _moodHistory = MutableStateFlow<List<MoodLog>>(emptyList())
    val moodHistory: StateFlow<List<MoodLog>> = _moodHistory.asStateFlow()

    private val _moodStreak = MutableStateFlow(0)
    val moodStreak: StateFlow<Int> = _moodStreak.asStateFlow()

    // Notes state
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Search state
    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults.asStateFlow()

    // Insights (placeholder for future AI integration)
    private val _insights = MutableStateFlow<InsightsResponse?>(null)
    val insights: StateFlow<InsightsResponse?> = _insights.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Selected note
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    // Stats
    private val _stats = MutableStateFlow<Map<String, Long>>(emptyMap())
    val stats: StateFlow<Map<String, Long>> = _stats.asStateFlow()

    // Connection status (always connected with Supabase)
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI Answer from search (placeholder)
    private val _aiAnswer = MutableStateFlow<String?>(null)
    val aiAnswer: StateFlow<String?> = _aiAnswer.asStateFlow()

    fun loadNotes(type: NoteType? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            noteRepository.getNotes(type = type)
                .onSuccess { notes ->
                    _notes.value = notes
                }
                .onFailure { e ->
                    _error.value = e.message
                }

            _isLoading.value = false
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            noteRepository.getStats()
                .onSuccess { stats ->
                    _stats.value = stats
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun selectNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.getNote(noteId)
                .onSuccess { note ->
                    _selectedNote.value = note
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun clearSelectedNote() {
        _selectedNote.value = null
    }

    fun createNote(
        title: String,
        content: String,
        type: NoteType = NoteType.TEXT,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            noteRepository.createNote(
                title = title,
                content = content,
                type = type,
                tags = tags
            )
                .onSuccess { note ->
                    // Add to local list
                    _notes.value = listOf(note) + _notes.value
                    loadStats()
                }
                .onFailure { e ->
                    _error.value = e.message
                }

            _isLoading.value = false
        }
    }

    fun updateNote(id: String, title: String? = null, content: String? = null) {
        viewModelScope.launch {
            noteRepository.updateNote(id, title = title, content = content)
                .onSuccess { note ->
                    _selectedNote.value = note
                    // Update in local list
                    _notes.value = _notes.value.map { if (it.id == id) note else it }
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(id)
                .onSuccess {
                    _selectedNote.value = null
                    _notes.value = _notes.value.filter { it.id != id }
                    loadStats()
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _aiAnswer.value = null
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true

            noteRepository.searchNotes(query)
                .onSuccess { results ->
                    _searchResults.value = results
                    // Generate a simple AI-like answer based on results
                    if (results.isNotEmpty()) {
                        _aiAnswer.value = "Found ${results.size} notes matching \"$query\". " +
                                "The most relevant is \"${results.first().title}\"."
                    } else {
                        _aiAnswer.value = "No notes found matching \"$query\"."
                    }
                }
                .onFailure { e ->
                    _error.value = e.message
                }

            _isProcessing.value = false
        }
    }

    fun loadInsights() {
        viewModelScope.launch {
            _isProcessing.value = true

            // Generate insights based on stats
            val currentStats = _stats.value
            val total = currentStats["total"] ?: 0

            if (total > 0) {
                _insights.value = InsightsResponse(
                    actionItems = emptyList(),
                    recentTopics = listOf("Personal", "Work", "Ideas"),
                    suggestions = listOf(
                        "Try recording more voice memos for quick capture",
                        "Add tags to organize your notes better",
                        "Review and summarize older notes"
                    )
                )
            }

            _isProcessing.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    // Mood functions
    fun loadTodayMood() {
        viewModelScope.launch {
            moodRepository.getTodayMood()
                .onSuccess { mood ->
                    _todayMood.value = mood
                }
                .onFailure { e ->
                    // Silently fail - mood is optional
                    println("Failed to load today's mood: ${e.message}")
                }
        }
    }

    fun logMood(mood: Int, note: String? = null) {
        viewModelScope.launch {
            moodRepository.logMood(mood, note)
                .onSuccess { moodLog ->
                    _todayMood.value = moodLog
                    loadMoodHistory()
                    loadMoodStreak()
                }
                .onFailure { e ->
                    _error.value = "Failed to log mood: ${e.message}"
                }
        }
    }

    fun loadMoodHistory(days: Int = 7) {
        viewModelScope.launch {
            moodRepository.getMoodHistory(days)
                .onSuccess { history ->
                    _moodHistory.value = history
                }
                .onFailure { e ->
                    println("Failed to load mood history: ${e.message}")
                }
        }
    }

    fun loadMoodStreak() {
        viewModelScope.launch {
            moodRepository.getMoodStreak()
                .onSuccess { streak ->
                    _moodStreak.value = streak
                }
                .onFailure { e ->
                    println("Failed to load mood streak: ${e.message}")
                }
        }
    }
}
