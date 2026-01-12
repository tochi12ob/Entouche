package en.entouche.backend

import en.entouche.backend.agents.*
import en.entouche.backend.api.*
import en.entouche.backend.config.AppConfig
import en.entouche.backend.database.DatabaseFactory
import en.entouche.backend.database.NoteRepository
import en.entouche.backend.database.UserRepository
import en.entouche.backend.models.ApiResponse
import en.entouche.backend.services.GroqService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = AppConfig.port,
        host = AppConfig.host,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseFactory.init()

    // Initialize services
    val groqService = GroqService()

    // Initialize repositories
    val userRepository = UserRepository()
    val noteRepository = NoteRepository()

    // Initialize AI agents
    val summarizerAgent = NoteSummarizerAgent(groqService)
    val searchAgent = SemanticSearchAgent(groqService)
    val insightsAgent = InsightsAgent(groqService)
    val voiceMemoAgent = VoiceMemoAgent(groqService)

    // Configure plugins
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-User-Id")

        anyHost() // In production, restrict this to your domains
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(
                    success = false,
                    error = cause.message ?: "Unknown error"
                )
            )
        }
    }

    // Configure routes
    routing {
        healthRoutes()

        route("/api/v1") {
            noteRoutes(
                noteRepository,
                groqService,
                summarizerAgent,
                searchAgent,
                insightsAgent,
                voiceMemoAgent
            )

            aiRoutes(
                noteRepository,
                groqService,
                summarizerAgent,
                searchAgent,
                insightsAgent,
                voiceMemoAgent
            )

            userRoutes(userRepository)
        }
    }

    environment.log.info("Entouche Backend started on ${AppConfig.host}:${AppConfig.port}")
    environment.log.info("API available at http://${AppConfig.host}:${AppConfig.port}/api/v1")
}
