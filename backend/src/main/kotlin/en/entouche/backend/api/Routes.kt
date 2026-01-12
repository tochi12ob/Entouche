package en.entouche.backend.api

import en.entouche.backend.agents.*
import en.entouche.backend.database.NoteRepository
import en.entouche.backend.database.NoteUpdates
import en.entouche.backend.database.UserRepository
import en.entouche.backend.models.*
import en.entouche.backend.services.GroqService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock

fun Route.noteRoutes(
    noteRepository: NoteRepository,
    groqService: GroqService,
    summarizerAgent: NoteSummarizerAgent,
    searchAgent: SemanticSearchAgent,
    insightsAgent: InsightsAgent,
    voiceMemoAgent: VoiceMemoAgent
) {
    route("/notes") {
        // Get all notes for user
        get {
            val userId = call.request.headers["X-User-Id"]
                ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse<Nothing>(false, error = "User ID required"))

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val type = call.request.queryParameters["type"]?.let { NoteType.valueOf(it.uppercase()) }

            val notes = if (type != null) {
                noteRepository.findByUserIdAndType(userId, type)
            } else {
                noteRepository.findByUserId(userId, pageSize, (page - 1) * pageSize)
            }

            val total = noteRepository.countByUserId(userId)

            call.respond(
                ApiResponse(
                    success = true,
                    data = PaginatedResponse(
                        items = notes,
                        total = total.toInt(),
                        page = page,
                        pageSize = pageSize,
                        hasMore = (page * pageSize) < total
                    )
                )
            )
        }

        // Get single note
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(false, error = "Note ID required"))

            val note = noteRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Nothing>(false, error = "Note not found"))

            call.respond(ApiResponse(success = true, data = note))
        }

        // Create note
        post {
            val userId = call.request.headers["X-User-Id"]
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse<Nothing>(false, error = "User ID required"))

            val request = call.receive<CreateNoteRequest>()

            val note = noteRepository.create(
                userId = userId,
                title = request.title,
                content = request.content,
                type = request.type,
                tags = request.tags,
                audioUrl = request.audioUrl,
                imageUrl = request.imageUrl,
                reminderTime = request.reminderTime
            )

            // Generate AI summary asynchronously
            if (request.content.length > 100) {
                try {
                    val summary = summarizerAgent.summarizeNote(request.content)
                    noteRepository.update(
                        note.id,
                        NoteUpdates(
                            aiSummary = summary.summary,
                            actionItems = summary.actionItems
                        )
                    )
                } catch (e: Exception) {
                    // Log error but don't fail the request
                    println("Failed to generate AI summary: ${e.message}")
                }
            }

            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = note))
        }

        // Update note
        put("/{id}") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(false, error = "Note ID required"))

            val request = call.receive<UpdateNoteRequest>()

            val updated = noteRepository.update(
                id,
                NoteUpdates(
                    title = request.title,
                    content = request.content,
                    tags = request.tags,
                    reminderTime = request.reminderTime,
                    isCompleted = request.isCompleted
                )
            )

            if (updated != null) {
                call.respond(ApiResponse(success = true, data = updated))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Nothing>(false, error = "Note not found"))
            }
        }

        // Delete note
        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(false, error = "Note ID required"))

            val deleted = noteRepository.delete(id)
            if (deleted) {
                call.respond(ApiResponse(success = true, data = mapOf("deleted" to true)))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Nothing>(false, error = "Note not found"))
            }
        }

        // Get note stats
        get("/stats") {
            val userId = call.request.headers["X-User-Id"]
                ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse<Nothing>(false, error = "User ID required"))

            val stats = mapOf(
                "total" to noteRepository.countByUserId(userId),
                "text" to noteRepository.countByUserIdAndType(userId, NoteType.TEXT),
                "voice" to noteRepository.countByUserIdAndType(userId, NoteType.VOICE),
                "photo" to noteRepository.countByUserIdAndType(userId, NoteType.PHOTO),
                "reminders" to noteRepository.countByUserIdAndType(userId, NoteType.REMINDER)
            )

            call.respond(ApiResponse(success = true, data = stats))
        }
    }
}

fun Route.aiRoutes(
    noteRepository: NoteRepository,
    groqService: GroqService,
    summarizerAgent: NoteSummarizerAgent,
    searchAgent: SemanticSearchAgent,
    insightsAgent: InsightsAgent,
    voiceMemoAgent: VoiceMemoAgent
) {
    route("/ai") {
        // Summarize text
        post("/summarize") {
            val request = call.receive<SummarizeRequest>()

            try {
                val result = summarizerAgent.summarizeNote(request.text)

                call.respond(
                    ApiResponse(
                        success = true,
                        data = SummarizeResponse(
                            summary = result.summary,
                            keyPoints = result.keyPoints,
                            actionItems = result.actionItems
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, error = "Summarization failed: ${e.message}")
                )
            }
        }

        // Semantic search
        post("/search") {
            val userId = call.request.headers["X-User-Id"]
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse<Nothing>(false, error = "User ID required"))

            val request = call.receive<SemanticSearchRequest>()

            try {
                // Get user's notes for context
                val notes = noteRepository.findByUserId(userId, limit = 100)
                val noteInputs = notes.map { note ->
                    NoteInput(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        type = note.type.name,
                        createdAt = note.createdAt.toString()
                    )
                }

                val result = searchAgent.search(request.query, noteInputs, request.limit)

                call.respond(
                    ApiResponse(
                        success = true,
                        data = SemanticSearchResponse(
                            results = result.results.map { item ->
                                SearchResult(
                                    noteId = item.noteId,
                                    title = item.title,
                                    preview = item.preview,
                                    score = item.relevanceScore,
                                    highlights = emptyList()
                                )
                            },
                            aiAnswer = result.aiAnswer
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, error = "Search failed: ${e.message}")
                )
            }
        }

        // Get AI insights
        post("/insights") {
            val userId = call.request.headers["X-User-Id"]
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse<Nothing>(false, error = "User ID required"))

            try {
                val notes = noteRepository.findByUserId(userId, limit = 50)
                val noteInputs = notes.map { note ->
                    NoteInput(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        type = note.type.name,
                        createdAt = note.createdAt.toString()
                    )
                }

                val result = insightsAgent.generateInsights(noteInputs)

                call.respond(
                    ApiResponse(
                        success = true,
                        data = InsightsResponse(
                            actionItems = result.actionItems.map { item ->
                                ActionItem(
                                    text = item.text,
                                    sourceNoteId = "",
                                    sourceNoteTitle = item.sourceNote,
                                    priority = item.priority
                                )
                            },
                            upcomingReminders = emptyList(),
                            recentTopics = result.topics,
                            suggestions = result.suggestions
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, error = "Insights generation failed: ${e.message}")
                )
            }
        }

        // Process voice memo
        post("/transcribe") {
            val request = call.receive<TranscribeRequest>()

            try {
                // In a real implementation, we'd first transcribe the audio
                // For now, we'll simulate with a placeholder
                val transcription = groqService.transcribeAudio(request.audioUrl)
                val processed = voiceMemoAgent.processVoiceMemo(transcription)

                call.respond(
                    ApiResponse(
                        success = true,
                        data = TranscribeResponse(
                            transcription = processed.cleanedTranscription,
                            summary = processed.summary,
                            actionItems = processed.actionItems
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, error = "Transcription failed: ${e.message}")
                )
            }
        }

        // Extract action items from text
        post("/extract-actions") {
            val request = call.receive<SummarizeRequest>()

            try {
                val actionItems = groqService.extractActionItems(request.text)

                call.respond(
                    ApiResponse(
                        success = true,
                        data = mapOf("actionItems" to actionItems)
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, error = "Action extraction failed: ${e.message}")
                )
            }
        }
    }
}

fun Route.userRoutes(userRepository: UserRepository) {
    route("/users") {
        // Get current user
        get("/me") {
            val userId = call.request.headers["X-User-Id"]
                ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse<Nothing>(false, error = "User ID required"))

            val user = userRepository.findById(userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Nothing>(false, error = "User not found"))

            call.respond(ApiResponse(success = true, data = user))
        }
    }
}

fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            mapOf(
                "status" to "healthy",
                "timestamp" to Clock.System.now().toString(),
                "version" to "1.0.0"
            )
        )
    }
}
