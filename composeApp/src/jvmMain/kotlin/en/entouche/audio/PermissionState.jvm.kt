package en.entouche.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class AudioPermissionHandler {
    @Composable
    actual fun rememberAudioPermissionState(): AudioPermissionState {
        // JVM/Desktop doesn't need runtime permissions for audio
        return AudioPermissionState(hasPermission = true)
    }

    actual fun requestPermission() {
        // No-op on JVM/Desktop
    }
}

@Composable
actual fun rememberAudioPermissionHandler(): AudioPermissionHandler {
    return remember { AudioPermissionHandler() }
}
