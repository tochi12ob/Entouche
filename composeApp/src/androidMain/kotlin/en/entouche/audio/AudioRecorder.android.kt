package en.entouche.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import kotlin.math.abs

actual class AudioRecorder {
    private val _state = MutableStateFlow(AudioRecorderState())
    actual val state: StateFlow<AudioRecorderState> = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var audioData = ByteArrayOutputStream()
    private var startTime: Long = 0

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    actual suspend fun startRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Reset state
            audioData.reset()
            _state.value = AudioRecorderState(isRecording = true)
            startTime = System.currentTimeMillis()

            // Create AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext Result.failure(Exception("Failed to initialize AudioRecord"))
            }

            audioRecord?.startRecording()

            // Start recording loop
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ShortArray(bufferSize)
                val amplitudes = mutableListOf<Float>()

                while (isActive && _state.value.isRecording) {
                    val readCount = audioRecord?.read(buffer, 0, bufferSize) ?: 0

                    if (readCount > 0) {
                        // Convert to bytes and store
                        for (i in 0 until readCount) {
                            val sample = buffer[i]
                            audioData.write(sample.toInt() and 0xFF)
                            audioData.write((sample.toInt() shr 8) and 0xFF)
                        }

                        // Calculate amplitude for visualization
                        val maxAmplitude = buffer.take(readCount).maxOfOrNull { abs(it.toInt()) } ?: 0
                        val normalizedAmplitude = maxAmplitude.toFloat() / Short.MAX_VALUE
                        amplitudes.add(normalizedAmplitude)

                        // Keep last 50 amplitudes for visualization
                        if (amplitudes.size > 50) {
                            amplitudes.removeAt(0)
                        }

                        // Update state
                        _state.value = _state.value.copy(
                            durationMs = System.currentTimeMillis() - startTime,
                            amplitudes = amplitudes.toList()
                        )
                    }

                    delay(50) // Small delay to prevent busy loop
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            _state.value = AudioRecorderState()
            Result.failure(e)
        }
    }

    actual suspend fun stopRecording(): Result<RecordingResult> = withContext(Dispatchers.IO) {
        try {
            val duration = System.currentTimeMillis() - startTime

            // Stop recording
            _state.value = _state.value.copy(isRecording = false)
            recordingJob?.cancelAndJoin()

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            // Get PCM data
            val pcmData = audioData.toByteArray()

            // Convert to WAV format
            val wavData = createWavFile(pcmData, sampleRate, 1, 16)

            _state.value = AudioRecorderState()

            Result.success(
                RecordingResult(
                    audioData = wavData,
                    durationMs = duration,
                    sampleRate = sampleRate,
                    mimeType = "audio/wav"
                )
            )
        } catch (e: Exception) {
            _state.value = AudioRecorderState()
            Result.failure(e)
        }
    }

    actual fun cancelRecording() {
        _state.value = _state.value.copy(isRecording = false)
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        audioData.reset()
        _state.value = AudioRecorderState()
    }

    actual fun release() {
        cancelRecording()
    }

    /**
     * Create a WAV file from PCM data
     */
    private fun createWavFile(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = pcmData.size
        val fileSize = 36 + dataSize

        val header = ByteArray(44)

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // File size - 8
        header[4] = (fileSize and 0xFF).toByte()
        header[5] = ((fileSize shr 8) and 0xFF).toByte()
        header[6] = ((fileSize shr 16) and 0xFF).toByte()
        header[7] = ((fileSize shr 24) and 0xFF).toByte()

        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // fmt chunk size (16)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // Audio format (1 = PCM)
        header[20] = 1
        header[21] = 0

        // Channels
        header[22] = channels.toByte()
        header[23] = 0

        // Sample rate
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()

        // Byte rate
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()

        // Block align
        header[32] = blockAlign.toByte()
        header[33] = 0

        // Bits per sample
        header[34] = bitsPerSample.toByte()
        header[35] = 0

        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // Data size
        header[40] = (dataSize and 0xFF).toByte()
        header[41] = ((dataSize shr 8) and 0xFF).toByte()
        header[42] = ((dataSize shr 16) and 0xFF).toByte()
        header[43] = ((dataSize shr 24) and 0xFF).toByte()

        return header + pcmData
    }
}
