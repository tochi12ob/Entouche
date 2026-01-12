package en.entouche.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Audio recorder state
 */
data class AudioRecorderState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val durationMs: Long = 0,
    val amplitudes: List<Float> = emptyList()
)

/**
 * Result of a completed recording
 */
data class RecordingResult(
    val audioData: ByteArray,
    val durationMs: Long,
    val sampleRate: Int = 16000,
    val mimeType: String = "audio/wav"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as RecordingResult
        return audioData.contentEquals(other.audioData) &&
                durationMs == other.durationMs &&
                sampleRate == other.sampleRate &&
                mimeType == other.mimeType
    }

    override fun hashCode(): Int {
        var result = audioData.contentHashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Platform-specific audio recorder interface
 */
expect class AudioRecorder() {
    /**
     * Current state of the recorder
     */
    val state: StateFlow<AudioRecorderState>

    /**
     * Start recording audio
     */
    suspend fun startRecording(): Result<Unit>

    /**
     * Stop recording and return the result
     */
    suspend fun stopRecording(): Result<RecordingResult>

    /**
     * Cancel recording without saving
     */
    fun cancelRecording()

    /**
     * Release resources
     */
    fun release()
}
