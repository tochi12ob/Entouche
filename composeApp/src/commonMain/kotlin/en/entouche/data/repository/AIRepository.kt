package en.entouche.data.repository

import en.entouche.data.api.ApiClient
import en.entouche.data.api.getBaseUrl
import en.entouche.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for AI-powered features
 */
class AIRepository(
    private val apiClient: ApiClient = ApiClient(baseUrl = getBaseUrl())
) {
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _insights = MutableStateFlow<InsightsResponse?>(null)
    val insights: StateFlow<InsightsResponse?> = _insights.asStateFlow()

    suspend fun summarize(text: String): SummarizeResponse? {
        _isProcessing.value = true

        val result = apiClient.summarize(text)
            .getOrNull()
            ?.takeIf { it.success }
            ?.data

        _isProcessing.value = false
        return result
    }

    suspend fun search(query: String, limit: Int = 10): SemanticSearchResponse? {
        _isProcessing.value = true
        _searchResults.value = emptyList()

        val result = apiClient.semanticSearch(query, limit)
            .getOrNull()
            ?.takeIf { it.success }
            ?.data

        result?.results?.let { _searchResults.value = it }

        _isProcessing.value = false
        return result
    }

    suspend fun getInsights(): InsightsResponse? {
        _isProcessing.value = true

        val result = apiClient.getInsights()
            .getOrNull()
            ?.takeIf { it.success }
            ?.data

        result?.let { _insights.value = it }

        _isProcessing.value = false
        return result
    }

    suspend fun transcribe(audioUrl: String): TranscribeResponse? {
        _isProcessing.value = true

        val result = apiClient.transcribe(audioUrl)
            .getOrNull()
            ?.takeIf { it.success }
            ?.data

        _isProcessing.value = false
        return result
    }

    suspend fun extractActionItems(text: String): List<String> {
        _isProcessing.value = true

        val result = apiClient.extractActions(text)
            .getOrNull()
            ?.takeIf { it.success }
            ?.data
            ?.get("actionItems")
            ?: emptyList()

        _isProcessing.value = false
        return result
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}
