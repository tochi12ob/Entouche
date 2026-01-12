package en.entouche.audio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

actual class AudioPermissionHandler(
    private val context: Context,
    private val onPermissionResult: (Boolean) -> Unit
) {
    private var permissionLauncher: (() -> Unit)? = null

    fun setLauncher(launcher: () -> Unit) {
        permissionLauncher = launcher
    }

    @Composable
    actual fun rememberAudioPermissionState(): AudioPermissionState {
        val hasPermission = remember {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        val shouldShowRationale = remember {
            (context as? Activity)?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.RECORD_AUDIO
                )
            } ?: false
        }

        return AudioPermissionState(
            hasPermission = hasPermission,
            shouldShowRationale = shouldShowRationale
        )
    }

    actual fun requestPermission() {
        permissionLauncher?.invoke()
    }
}

@Composable
actual fun rememberAudioPermissionHandler(): AudioPermissionHandler {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val handler = remember { AudioPermissionHandler(context) { hasPermission = it } }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        handler.setLauncher { launcher.launch(Manifest.permission.RECORD_AUDIO) }
    }

    return handler
}
