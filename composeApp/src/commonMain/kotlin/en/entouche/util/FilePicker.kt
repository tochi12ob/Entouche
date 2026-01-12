package en.entouche.util

import androidx.compose.runtime.Composable

/**
 * Result from file picker
 */
data class FilePickerResult(
    val fileName: String,
    val content: String,
    val mimeType: String? = null
)

/**
 * File picker state
 */
data class FilePickerState(
    val isPickerOpen: Boolean = false,
    val result: FilePickerResult? = null,
    val error: String? = null
)

/**
 * Expected file picker handler - implemented per platform
 */
expect class FilePickerHandler {
    /**
     * Launch file picker to select a document
     */
    fun launchFilePicker()
}

/**
 * Remember file picker handler composable
 */
@Composable
expect fun rememberFilePickerHandler(
    onResult: (FilePickerResult?) -> Unit,
    onError: (String) -> Unit
): FilePickerHandler
