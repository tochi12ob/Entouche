package en.entouche.backend.agents

import en.entouche.backend.services.GroqService

/**
 * Note Summarizer Agent - Summarizes notes and extracts key information
 */
class NoteSummarizerAgent(private val groqService: GroqService) {

    private val systemPrompt = """You are an intelligent note summarization assistant for the Entouche app.
        |Your role is to help users by:
        |1. Creating concise, meaningful summaries of their notes
        |2. Extracting key points and important information
        |3. Identifying action items, tasks, and commitments
        |4. Highlighting dates, names, and important references
        |
        |Always be helpful, accurate, and focus on what matters most to the user.
        |Format your responses clearly with sections for Summary, Key Points, and Action Items.
    """.trimMargin()

    suspend fun summarizeNote(content: String): SummaryAgentResult {
        val result = groqService.summarize(content)
        return SummaryAgentResult(
            summary = result.summary,
            keyPoints = result.keyPoints,
            actionItems = result.actionItems
        )
    }

    suspend fun summarizeMultipleNotes(notes: List<NoteInput>): BatchSummaryResult {
        val summaries = notes.map { note ->
            val result = summarizeNote(note.content)
            NoteSummary(
                noteId = note.id,
                title = note.title,
                summary = result.summary,
                keyPoints = result.keyPoints,
                actionItems = result.actionItems
            )
        }

        // Generate overall insights
        val allActionItems = summaries.flatMap { it.actionItems }
        val allKeyPoints = summaries.flatMap { it.keyPoints }

        return BatchSummaryResult(
            summaries = summaries,
            overallActionItems = allActionItems.distinct(),
            commonThemes = extractCommonThemes(allKeyPoints)
        )
    }

    private fun extractCommonThemes(keyPoints: List<String>): List<String> {
        // Simple theme extraction - in production, use NLP/embeddings
        val words = keyPoints.flatMap { it.lowercase().split(" ") }
            .filter { it.length > 4 }
            .groupBy { it }
            .mapValues { it.value.size }
            .filter { it.value > 1 }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
        return words
    }
}

/**
 * Semantic Search Agent - Finds relevant notes using AI understanding
 */
class SemanticSearchAgent(private val groqService: GroqService) {

    private val systemPrompt = """You are a semantic search assistant for the Entouche personal knowledge app.
        |Your role is to:
        |1. Understand the user's search intent, not just keywords
        |2. Find the most relevant notes based on meaning
        |3. Provide helpful context about why notes are relevant
        |4. Answer questions using information from the user's notes
        |
        |When searching:
        |- Consider synonyms and related concepts
        |- Understand time references ("last week", "yesterday")
        |- Recognize named entities (people, places, things)
        |- Connect related topics across different notes
    """.trimMargin()

    suspend fun search(
        query: String,
        notes: List<NoteInput>,
        limit: Int = 10
    ): SearchAgentResult {
        val noteContexts = notes.map { note ->
            en.entouche.backend.services.NoteContext(
                id = note.id,
                title = note.title,
                preview = note.content.take(500),
                type = note.type,
                createdAt = note.createdAt
            )
        }

        val result = groqService.semanticSearch(query, noteContexts)

        return SearchAgentResult(
            results = result.relevantNotes.take(limit).map { context ->
                SearchResultItem(
                    noteId = context.id,
                    title = context.title,
                    preview = context.preview,
                    relevanceScore = result.confidence
                )
            },
            aiAnswer = result.answer,
            suggestedQueries = generateSuggestedQueries(query)
        )
    }

    private fun generateSuggestedQueries(originalQuery: String): List<String> {
        // Simple query suggestions - in production, use AI
        return listOf(
            "$originalQuery from this week",
            "$originalQuery action items",
            "related to $originalQuery"
        )
    }
}

/**
 * Insights Agent - Provides proactive insights from user's notes
 */
class InsightsAgent(private val groqService: GroqService) {

    private val systemPrompt = """You are a proactive insights assistant for the Entouche personal knowledge app.
        |Your role is to:
        |1. Identify patterns and connections across the user's notes
        |2. Surface outstanding action items and tasks
        |3. Remind users of important commitments
        |4. Suggest helpful connections between notes
        |5. Provide personalized productivity tips
        |
        |Be helpful but not intrusive. Focus on actionable insights.
    """.trimMargin()

    suspend fun generateInsights(notes: List<NoteInput>): InsightsAgentResult {
        val noteContexts = notes.map { note ->
            en.entouche.backend.services.NoteContext(
                id = note.id,
                title = note.title,
                preview = note.content.take(500),
                type = note.type,
                createdAt = note.createdAt
            )
        }

        val result = groqService.generateInsights(noteContexts)

        return InsightsAgentResult(
            actionItems = result.actionItems.map { item ->
                ActionItemInsight(
                    text = item.text,
                    priority = item.priority,
                    sourceNote = item.source
                )
            },
            topics = result.topics,
            suggestions = result.suggestions,
            connections = findConnections(notes)
        )
    }

    private fun findConnections(notes: List<NoteInput>): List<NoteConnection> {
        // Simple connection finding - in production, use embeddings
        val connections = mutableListOf<NoteConnection>()

        for (i in notes.indices) {
            for (j in i + 1 until notes.size) {
                val note1 = notes[i]
                val note2 = notes[j]

                // Check for shared tags or keywords
                val words1 = note1.content.lowercase().split(" ").toSet()
                val words2 = note2.content.lowercase().split(" ").toSet()
                val common = words1.intersect(words2).filter { it.length > 5 }

                if (common.size >= 3) {
                    connections.add(
                        NoteConnection(
                            noteId1 = note1.id,
                            noteId2 = note2.id,
                            reason = "Shared topics: ${common.take(3).joinToString(", ")}"
                        )
                    )
                }
            }
        }

        return connections.take(5)
    }
}

/**
 * Voice Memo Agent - Processes voice memos with transcription and analysis
 */
class VoiceMemoAgent(private val groqService: GroqService) {

    private val systemPrompt = """You are a voice memo processing assistant for the Entouche app.
        |Your role is to:
        |1. Analyze transcribed voice memos
        |2. Create clear, readable summaries
        |3. Extract action items and tasks mentioned
        |4. Identify important dates, names, and references
        |5. Suggest appropriate tags for organization
        |
        |Voice memos are often informal and conversational - extract the key information clearly.
    """.trimMargin()

    suspend fun processVoiceMemo(transcription: String): VoiceMemoResult {
        val summary = groqService.summarize(transcription, maxLength = 150)
        val actionItems = groqService.extractActionItems(transcription)

        return VoiceMemoResult(
            cleanedTranscription = cleanTranscription(transcription),
            summary = summary.summary,
            actionItems = actionItems,
            suggestedTags = suggestTags(transcription),
            keyPoints = summary.keyPoints
        )
    }

    private fun cleanTranscription(text: String): String {
        // Clean up common transcription artifacts
        return text
            .replace(Regex("\\bum\\b|\\buh\\b|\\blike\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun suggestTags(text: String): List<String> {
        val tags = mutableListOf<String>()
        val lowerText = text.lowercase()

        // Simple tag suggestion based on keywords
        if (lowerText.contains("meeting") || lowerText.contains("call")) tags.add("meeting")
        if (lowerText.contains("todo") || lowerText.contains("remember")) tags.add("task")
        if (lowerText.contains("idea") || lowerText.contains("think")) tags.add("idea")
        if (lowerText.contains("work") || lowerText.contains("project")) tags.add("work")
        if (lowerText.contains("personal") || lowerText.contains("family")) tags.add("personal")

        return tags.distinct()
    }
}

// Data classes for agent results
data class NoteInput(
    val id: String,
    val title: String,
    val content: String,
    val type: String,
    val createdAt: String
)

data class SummaryAgentResult(
    val summary: String,
    val keyPoints: List<String>,
    val actionItems: List<String>
)

data class NoteSummary(
    val noteId: String,
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val actionItems: List<String>
)

data class BatchSummaryResult(
    val summaries: List<NoteSummary>,
    val overallActionItems: List<String>,
    val commonThemes: List<String>
)

data class SearchResultItem(
    val noteId: String,
    val title: String,
    val preview: String,
    val relevanceScore: Float
)

data class SearchAgentResult(
    val results: List<SearchResultItem>,
    val aiAnswer: String?,
    val suggestedQueries: List<String>
)

data class ActionItemInsight(
    val text: String,
    val priority: String,
    val sourceNote: String
)

data class NoteConnection(
    val noteId1: String,
    val noteId2: String,
    val reason: String
)

data class InsightsAgentResult(
    val actionItems: List<ActionItemInsight>,
    val topics: List<String>,
    val suggestions: List<String>,
    val connections: List<NoteConnection>
)

data class VoiceMemoResult(
    val cleanedTranscription: String,
    val summary: String,
    val actionItems: List<String>,
    val suggestedTags: List<String>,
    val keyPoints: List<String>
)
