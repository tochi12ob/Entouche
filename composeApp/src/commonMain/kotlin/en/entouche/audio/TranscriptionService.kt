package en.entouche.audio

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Transcription result from AI service
 */
@Serializable
data class TranscriptionResult(
    val text: String,
    val language: String? = null,
    val duration: Double? = null
)

/**
 * Service for transcribing audio using Groq's Whisper API
 */
class TranscriptionService(
    private val apiKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Transcribe audio data using Groq's Whisper API
     */
    suspend fun transcribe(
        audioData: ByteArray,
        fileName: String = "recording.wav",
        language: String? = null
    ): Result<TranscriptionResult> {
        return try {
            println("Transcription: Sending ${audioData.size} bytes to Groq API")

            val response = httpClient.submitFormWithBinaryData(
                url = "https://api.groq.com/openai/v1/audio/transcriptions",
                formData = formData {
                    append("file", audioData, Headers.build {
                        append(HttpHeaders.ContentType, "audio/wav")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                    append("model", "whisper-large-v3")
                    append("response_format", "json")
                    language?.let { append("language", it) }
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }

            println("Transcription: Response status ${response.status}")

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                println("Transcription: Response body $responseBody")
                val result = json.decodeFromString<GroqTranscriptionResponse>(responseBody)
                Result.success(
                    TranscriptionResult(
                        text = result.text,
                        language = language,
                        duration = null
                    )
                )
            } else {
                val errorBody = response.bodyAsText()
                println("Transcription: Error $errorBody")
                Result.failure(Exception("Transcription failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            println("Transcription: Exception ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}

@Serializable
private data class GroqTranscriptionResponse(
    val text: String
)

/**
 * Configuration for AI transcription
 */
object TranscriptionConfig {
    // Groq API key for Whisper transcription
    // TODO: Replace with your own Groq API key
    // Get one from: https://console.groq.com -> API Keys
    var apiKey: String = "YOUR_GROQ_API_KEY"

    fun isConfigured(): Boolean = apiKey.isNotBlank() && apiKey != "YOUR_GROQ_API_KEY"
}
