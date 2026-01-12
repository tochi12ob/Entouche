package en.entouche.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import en.entouche.audio.AudioRecorder
import en.entouche.audio.RecordingResult
import en.entouche.audio.TranscriptionConfig
import en.entouche.audio.TranscriptionService
import en.entouche.audio.rememberAudioPermissionHandler
import en.entouche.data.models.NoteType
import en.entouche.ui.components.*
import en.entouche.ui.theme.*
import en.entouche.ui.viewmodel.EntoucheViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun VoiceCaptureScreen(
    viewModel: EntoucheViewModel,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Permission handler
    val permissionHandler = rememberAudioPermissionHandler()
    val permissionState = permissionHandler.rememberAudioPermissionState()
    var permissionRequested by remember { mutableStateOf(false) }

    // Audio recorder
    val audioRecorder = remember { AudioRecorder() }
    val recorderState by audioRecorder.state.collectAsState()

    // UI State
    var hasRecording by remember { mutableStateOf(false) }
    var recordingResult by remember { mutableStateOf<RecordingResult?>(null) }
    var transcription by remember { mutableStateOf("") }
    var memoTitle by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()

    // Calculate duration in seconds for display
    val durationSeconds = (recorderState.durationMs / 1000).toInt()

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
        }
    }

    // Generate title when recording completes
    LaunchedEffect(hasRecording) {
        if (hasRecording && memoTitle.isEmpty()) {
            val now = Clock.System.now()
            memoTitle = "Voice Memo - ${now.toString().take(16).replace("T", " ")}"
        }
    }

    // Function to start recording
    fun startRecording() {
        // Check permission first
        if (!permissionState.hasPermission) {
            permissionRequested = true
            permissionHandler.requestPermission()
            return
        }

        coroutineScope.launch {
            errorMessage = null
            hasRecording = false
            transcription = ""
            memoTitle = ""
            recordingResult = null

            audioRecorder.startRecording()
                .onFailure { e ->
                    errorMessage = "Failed to start recording: ${e.message}"
                }
        }
    }

    // Auto-start recording after permission granted
    LaunchedEffect(permissionState.hasPermission, permissionRequested) {
        if (permissionState.hasPermission && permissionRequested && !recorderState.isRecording) {
            permissionRequested = false
            errorMessage = null
            hasRecording = false
            transcription = ""
            memoTitle = ""
            recordingResult = null

            audioRecorder.startRecording()
                .onFailure { e ->
                    errorMessage = "Failed to start recording: ${e.message}"
                }
        }
    }

    // Function to stop recording and transcribe
    fun stopRecording() {
        coroutineScope.launch {
            audioRecorder.stopRecording()
                .onSuccess { result ->
                    recordingResult = result
                    hasRecording = true

                    // Auto-transcribe if API key is configured
                    if (TranscriptionConfig.isConfigured()) {
                        isTranscribing = true
                        val service = TranscriptionService(TranscriptionConfig.apiKey)

                        service.transcribe(result.audioData)
                            .onSuccess { transcriptionResult ->
                                transcription = transcriptionResult.text
                            }
                            .onFailure { e ->
                                errorMessage = "Transcription failed: ${e.message}"
                            }

                        service.close()
                        isTranscribing = false
                    }
                }
                .onFailure { e ->
                    errorMessage = "Failed to stop recording: ${e.message}"
                }
        }
    }

    // Function to save the voice memo as a note
    fun saveVoiceMemo() {
        if (memoTitle.isNotBlank() && transcription.isNotBlank()) {
            isSaving = true
            viewModel.createNote(
                title = memoTitle.trim(),
                content = transcription.trim(),
                type = NoteType.VOICE,
                tags = listOf("voice-memo")
            )
            onSave()
        }
    }

    GradientBackground(
        modifier = modifier,
        animated = recorderState.isRecording
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.screenPaddingHorizontal)
                    .padding(top = Dimensions.spacingMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.Filled.Close,
                    onClick = {
                        audioRecorder.cancelRecording()
                        onNavigateBack()
                    },
                    style = GlassButtonStyle.Ghost,
                    size = 44.dp
                )

                Text(
                    text = "Voice Memo",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                if (hasRecording) {
                    if (isSaving || isLoading) {
                        CircularProgressIndicator(
                            color = TealWave,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        GlassButton(
                            text = "Save",
                            onClick = { saveVoiceMemo() },
                            style = GlassButtonStyle.Primary,
                            size = GlassButtonSize.Small,
                            enabled = memoTitle.isNotBlank() && transcription.isNotBlank()
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(44.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimensions.screenPaddingHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.1f))

                // Error message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    errorMessage?.let { error ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            size = GlassCardSize.Small
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimensions.spacingMd))
                    }
                }

                // Recording state indicator
                AnimatedContent(
                    targetState = Triple(recorderState.isRecording, hasRecording, isTranscribing),
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    }
                ) { (recording, hasRec, transcribing) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            recording -> {
                                Text(
                                    text = "Recording...",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Error
                                )
                            }
                            transcribing -> {
                                Text(
                                    text = "Transcribing...",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TealWave
                                )
                            }
                            hasRec -> {
                                Text(
                                    text = "Recording Complete",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Success
                                )
                            }
                            else -> {
                                Text(
                                    text = "Tap to Start",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                // Timer
                RecordingTimer(durationSeconds = if (recorderState.isRecording) durationSeconds else (recordingResult?.durationMs?.div(1000)?.toInt() ?: 0))

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))

                // Waveform visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTranscribing) {
                        CircularProgressIndicator(
                            color = TealWave,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        AnimatedWaveform(
                            isAnimating = recorderState.isRecording,
                            amplitudes = recorderState.amplitudes,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingXl))

                // Record button
                PulsingRecordButton(
                    isRecording = recorderState.isRecording,
                    onClick = {
                        if (recorderState.isRecording) {
                            stopRecording()
                        } else {
                            startRecording()
                        }
                    },
                    size = 88.dp,
                    enabled = !isTranscribing
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                Text(
                    text = when {
                        isTranscribing -> "Processing audio..."
                        recorderState.isRecording -> "Tap to stop"
                        hasRecording -> "Tap to re-record"
                        else -> "Tap to record"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.weight(0.1f))

                // Transcription input section
                AnimatedVisibility(
                    visible = hasRecording && !isTranscribing,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = Dimensions.spacingLg)
                    ) {
                        // Title field
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        GlassTextField(
                            value = memoTitle,
                            onValueChange = { memoTitle = it },
                            placeholder = "Enter a title for your memo",
                            leadingIcon = Icons.Filled.Title,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                        // Transcription field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = TealWave,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Transcription",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }

                            if (TranscriptionConfig.isConfigured() && transcription.isEmpty()) {
                                GlassButton(
                                    text = "Retry",
                                    onClick = {
                                        recordingResult?.let { result ->
                                            coroutineScope.launch {
                                                isTranscribing = true
                                                val service = TranscriptionService(TranscriptionConfig.apiKey)
                                                service.transcribe(result.audioData)
                                                    .onSuccess { r -> transcription = r.text }
                                                    .onFailure { e -> errorMessage = "Transcription failed: ${e.message}" }
                                                service.close()
                                                isTranscribing = false
                                            }
                                        }
                                    },
                                    style = GlassButtonStyle.Ghost,
                                    size = GlassButtonSize.Small
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        GlassTextArea(
                            value = transcription,
                            onValueChange = { transcription = it },
                            placeholder = if (TranscriptionConfig.isConfigured())
                                "AI transcription will appear here. You can edit it."
                            else
                                "Type your transcription here...",
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Dimensions.spacingMd))

                        // Info card
                        if (!TranscriptionConfig.isConfigured()) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                size = GlassCardSize.Small
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.Key,
                                        contentDescription = null,
                                        tint = Warning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "AI Transcription Not Configured",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Warning
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Add your Groq API key in Settings to enable automatic AI transcription.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        } else {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                size = GlassCardSize.Small
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Success,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI transcription enabled. You can edit the text before saving.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Tips for first time users
                AnimatedVisibility(
                    visible = !recorderState.isRecording && !hasRecording && !isTranscribing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = Dimensions.spacingXl)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            size = GlassCardSize.Medium
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = Warning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Tips for better voice memos",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TipItem("Speak clearly and at a normal pace")
                                    TipItem("Minimize background noise")
                                    TipItem("State dates, names, and numbers clearly")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = Seafoam,
                    shape = RoundedCornerShape(3.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
