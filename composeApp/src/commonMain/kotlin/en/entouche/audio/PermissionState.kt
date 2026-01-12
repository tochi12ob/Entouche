package en.entouche.audio

import androidx.compose.runtime.Composable

/**
 * Permission state for audio recording
 */
data class AudioPermissionState(
    val hasPermission: Boolean,
    val shouldShowRationale: Boolean = false
)

/**
 * Platform-specific permission handling
 */
expect class AudioPermissionHandler {
    @Composable
    fun rememberAudioPermissionState(): AudioPermissionState

    fun requestPermission()
}

/**
 * Creates a platform-specific permission handler
 */
@Composable
expect fun rememberAudioPermissionHandler(): AudioPermissionHandler
