package en.entouche.backend.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object GroqConfig {
    val apiKey: String = System.getenv("GROQ_API_KEY") ?: throw IllegalStateException("GROQ_API_KEY environment variable is required")
    const val baseUrl = "https://api.groq.com/openai/v1"

    // Available Groq models
    object Models {
        const val LLAMA_3_3_70B = "llama-3.3-70b-versatile"
        const val LLAMA_3_1_70B = "llama-3.1-70b-versatile"
        const val LLAMA_3_1_8B = "llama-3.1-8b-instant"
        const val MIXTRAL_8X7B = "mixtral-8x7b-32768"
        const val GEMMA_2_9B = "gemma2-9b-it"
        const val WHISPER_LARGE = "whisper-large-v3"
        const val WHISPER_LARGE_TURBO = "whisper-large-v3-turbo"

        // Default model for chat completions
        val DEFAULT = LLAMA_3_3_70B
        // Fast model for quick responses
        val FAST = LLAMA_3_1_8B
        // Model for transcription
        val TRANSCRIPTION = WHISPER_LARGE_TURBO
    }

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}

object AppConfig {
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host: String = System.getenv("HOST") ?: "0.0.0.0"
    val databaseUrl: String = System.getenv("DATABASE_URL") ?: "jdbc:h2:mem:entouche;DB_CLOSE_DELAY=-1"
    val databaseDriver: String = System.getenv("DATABASE_DRIVER") ?: "org.h2.Driver"
    val jwtSecret: String = System.getenv("JWT_SECRET") ?: "development-secret-change-in-production"
    val jwtIssuer: String = System.getenv("JWT_ISSUER") ?: "entouche-backend"
}
