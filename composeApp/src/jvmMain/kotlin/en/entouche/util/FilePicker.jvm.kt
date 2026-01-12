package en.entouche.util

import androidx.compose.runtime.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FilePickerHandler(
    private val onResult: (FilePickerResult?) -> Unit,
    private val onError: (String) -> Unit
) {
    actual fun launchFilePicker() {
        try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = "Select Q&A Document"
                fileFilter = FileNameExtensionFilter(
                    "Text Files (*.txt, *.md, *.csv, *.json)",
                    "txt", "md", "csv", "json"
                )
                isAcceptAllFileFilterUsed = true
            }

            val result = fileChooser.showOpenDialog(null)

            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                handleFile(file)
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            onError("Failed to open file picker: ${e.message}")
        }
    }

    private fun handleFile(file: File) {
        try {
            val content = file.readText()

            if (content.isNotBlank()) {
                onResult(FilePickerResult(
                    fileName = file.name,
                    content = content,
                    mimeType = when {
                        file.name.endsWith(".txt") -> "text/plain"
                        file.name.endsWith(".md") -> "text/markdown"
                        file.name.endsWith(".csv") -> "text/csv"
                        file.name.endsWith(".json") -> "application/json"
                        else -> null
                    }
                ))
            } else {
                onError("File is empty")
            }
        } catch (e: Exception) {
            onError("Error reading file: ${e.message}")
        }
    }
}

@Composable
actual fun rememberFilePickerHandler(
    onResult: (FilePickerResult?) -> Unit,
    onError: (String) -> Unit
): FilePickerHandler {
    return remember {
        FilePickerHandler(
            onResult = onResult,
            onError = onError
        )
    }
}
