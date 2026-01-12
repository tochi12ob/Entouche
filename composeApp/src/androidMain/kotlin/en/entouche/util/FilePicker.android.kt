package en.entouche.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

actual class FilePickerHandler(
    private val context: Context,
    private val launcher: () -> Unit,
    private val onResult: (FilePickerResult?) -> Unit,
    private val onError: (String) -> Unit
) {
    actual fun launchFilePicker() {
        try {
            launcher()
        } catch (e: Exception) {
            onError("Failed to open file picker: ${e.message}")
        }
    }

    fun handleUri(uri: Uri?) {
        if (uri == null) {
            onResult(null)
            return
        }

        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)

            // Get file name
            val fileName = getFileName(uri) ?: "document"

            // Read content based on type
            val content = when {
                mimeType?.startsWith("text/") == true ||
                fileName.endsWith(".txt") ||
                fileName.endsWith(".md") ||
                fileName.endsWith(".csv") -> {
                    readTextFile(uri)
                }
                mimeType == "application/pdf" || fileName.endsWith(".pdf") -> {
                    // For PDF, we'll read it as text (basic extraction)
                    readTextFile(uri) ?: "Unable to read PDF content. Please copy and paste the text instead."
                }
                mimeType == "application/json" || fileName.endsWith(".json") -> {
                    readTextFile(uri)
                }
                else -> {
                    // Try to read as text anyway
                    readTextFile(uri)
                }
            }

            if (content != null && content.isNotBlank()) {
                onResult(FilePickerResult(
                    fileName = fileName,
                    content = content,
                    mimeType = mimeType
                ))
            } else {
                onError("Could not read file content. Try a .txt file or paste the content directly.")
            }
        } catch (e: Exception) {
            onError("Error reading file: ${e.message}")
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name ?: uri.lastPathSegment
    }

    private fun readTextFile(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
actual fun rememberFilePickerHandler(
    onResult: (FilePickerResult?) -> Unit,
    onError: (String) -> Unit
): FilePickerHandler {
    val context = LocalContext.current

    var handler by remember { mutableStateOf<FilePickerHandler?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        handler?.handleUri(uri)
    }

    handler = remember(context) {
        FilePickerHandler(
            context = context,
            launcher = {
                launcher.launch(arrayOf(
                    "text/plain",
                    "text/markdown",
                    "text/csv",
                    "application/json",
                    "application/pdf",
                    "*/*"  // Allow all files as fallback
                ))
            },
            onResult = onResult,
            onError = onError
        )
    }

    return handler!!
}
