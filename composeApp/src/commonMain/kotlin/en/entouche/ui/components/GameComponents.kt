package en.entouche.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import en.entouche.data.models.Difficulty
import en.entouche.data.models.FlashCard
import en.entouche.data.models.GameMode
import en.entouche.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Animated flip card component for flashcard mode
 */
@Composable
fun FlipCard(
    card: FlashCard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val animateFront by animateFloatAsState(
        targetValue = if (!isFlipped) 1f else 0f,
        animationSpec = tween(200)
    )

    val animateBack by animateFloatAsState(
        targetValue = if (isFlipped) 1f else 0f,
        animationSpec = tween(200)
    )

    val scale by animateFloatAsState(
        targetValue = if (isFlipped) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .scale(scale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFlip
            )
    ) {
        // Front of card (Question)
        if (rotation <= 90f) {
            GlassCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = animateFront },
                size = GlassCardSize.Large
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Difficulty badge
                    DifficultyBadge(card.difficulty)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to reveal answer",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Back of card (Answer)
        if (rotation > 90f) {
            GlassCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = animateBack
                        rotationY = 180f
                    },
                size = GlassCardSize.Large
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = TealWave,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TealWave,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (card.hint != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hint: ${card.hint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Difficulty badge with color coding
 */
@Composable
fun DifficultyBadge(difficulty: Difficulty) {
    val color = when (difficulty) {
        Difficulty.EASY -> Success
        Difficulty.MEDIUM -> Warning
        Difficulty.HARD -> Error
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = difficulty.emoji,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = difficulty.name,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Quiz option button with animation
 */
@Composable
fun QuizOptionButton(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isRevealed && isCorrect == true -> Success.copy(alpha = 0.2f)
            isRevealed && isSelected && isCorrect == false -> Error.copy(alpha = 0.2f)
            isSelected -> TealWave.copy(alpha = 0.15f)
            else -> GlassWhite
        },
        animationSpec = tween(300)
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isRevealed && isCorrect == true -> Success
            isRevealed && isSelected && isCorrect == false -> Error
            isSelected -> TealWave
            else -> GlassBorder
        },
        animationSpec = tween(300)
    )

    val optionLabel = ('A' + index).toString()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected || isRevealed) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = !isRevealed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Option letter badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected || (isRevealed && isCorrect == true))
                        TealWave.copy(alpha = 0.2f)
                    else
                        GlassSurface
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = optionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) TealWave else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Result icon
        AnimatedVisibility(
            visible = isRevealed,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = if (isCorrect == true) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (isCorrect == true) Success else Error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Animated score display
 */
@Composable
fun AnimatedScore(
    score: Int,
    modifier: Modifier = Modifier
) {
    var displayScore by remember { mutableStateOf(0) }

    LaunchedEffect(score) {
        val diff = score - displayScore
        val steps = minOf(diff, 10)
        val increment = diff / steps.coerceAtLeast(1)

        repeat(steps) {
            delay(50)
            displayScore += increment
        }
        displayScore = score
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Stars,
            contentDescription = null,
            tint = Warning,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = displayScore.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Streak indicator with fire animation
 */
@Composable
fun StreakIndicator(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (streak > 0) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    val fireEmoji = when {
        streak >= 10 -> "🔥🔥🔥"
        streak >= 5 -> "🔥🔥"
        streak > 0 -> "🔥"
        else -> ""
    }

    AnimatedVisibility(
        visible = streak > 0,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Row(
            modifier = modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Warning.copy(alpha = 0.2f),
                            Error.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Warning.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fireEmoji,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${streak}x",
                style = MaterialTheme.typography.labelLarge,
                color = Warning,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Circular progress ring
 */
@Composable
fun ProgressRing(
    progress: Float,
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = GlassBorder,
            strokeWidth = 4.dp
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = TealWave,
            strokeWidth = 4.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.titleMedium,
                color = TealWave,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/$total",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

/**
 * Game mode card for selection
 */
@Composable
fun GameModeCard(
    mode: GameMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    GlassCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    TealWave,
                    RoundedCornerShape(20.dp)
                ) else Modifier
            ),
        size = GlassCardSize.Medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mode.emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mode.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) TealWave else TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Confetti particle for celebration
 */
@Composable
fun ConfettiEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(30) {
            ConfettiParticle(
                x = Random.nextFloat(),
                delay = Random.nextInt(500),
                color = listOf(TealWave, Seafoam, AquaMist, Warning, Success).random()
            )
        }
    }

    AnimatedVisibility(
        visible = isPlaying,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            particles.forEach { particle ->
                FallingParticle(particle)
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val delay: Int,
    val color: Color
)

@Composable
private fun FallingParticle(particle: ConfettiParticle) {
    val infiniteTransition = rememberInfiniteTransition()

    val y by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + particle.delay, delayMillis = particle.delay),
            repeatMode = RepeatMode.Restart
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = particle.x * size.width
                translationY = y * size.height
                rotationZ = rotation
            }
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .rotate(rotation)
                .background(particle.color, RoundedCornerShape(2.dp))
        )
    }
}

/**
 * Result celebration screen
 */
@Composable
fun GameResultCard(
    score: Int,
    correctAnswers: Int,
    totalCards: Int,
    maxStreak: Int,
    accuracy: Float,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracyPercent = (accuracy * 100).toInt()

    val resultEmoji = when {
        accuracyPercent >= 90 -> "🏆"
        accuracyPercent >= 70 -> "⭐"
        accuracyPercent >= 50 -> "👍"
        else -> "💪"
    }

    val resultMessage = when {
        accuracyPercent >= 90 -> "Outstanding!"
        accuracyPercent >= 70 -> "Great job!"
        accuracyPercent >= 50 -> "Good effort!"
        else -> "Keep practicing!"
    }

    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (accuracyPercent >= 70) {
            showConfetti = true
            delay(3000)
            showConfetti = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ConfettiEffect(isPlaying = showConfetti)

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Center),
            size = GlassCardSize.Large
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = resultEmoji,
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = resultMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TealWave,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Score",
                        value = score.toString(),
                        icon = "🌟"
                    )
                    StatItem(
                        label = "Accuracy",
                        value = "$accuracyPercent%",
                        icon = "🎯"
                    )
                    StatItem(
                        label = "Streak",
                        value = maxStreak.toString(),
                        icon = "🔥"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$correctAnswers / $totalCards correct",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Exit",
                        onClick = onExit,
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Play Again",
                        onClick = onPlayAgain,
                        style = GlassButtonStyle.Accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

/**
 * Timer display for speed round
 */
@Composable
fun GameTimer(
    timeLeftMs: Long,
    totalTimeMs: Long,
    modifier: Modifier = Modifier
) {
    val progress = timeLeftMs.toFloat() / totalTimeMs
    val seconds = (timeLeftMs / 1000).toInt()

    val timerColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> Success
            progress > 0.2f -> Warning
            else -> Error
        },
        animationSpec = tween(300)
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = null,
            tint = timerColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${seconds}s",
            style = MaterialTheme.typography.titleLarge,
            color = timerColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== FRIEND MODE COMPONENTS ====================

/**
 * Role selection for friend mode
 */
enum class FriendModeRole(val title: String, val emoji: String, val description: String) {
    QUIZ_MASTER("Quiz Master", "🎤", "You ask the questions"),
    PLAYER("Player", "🧠", "You answer the questions")
}

/**
 * Friend mode state
 */
data class FriendModeState(
    val currentRole: FriendModeRole = FriendModeRole.PLAYER,
    val questionNumber: Int = 1,
    val currentQuestion: String = "",
    val playerAnswer: String = "",
    val isAnswerRevealed: Boolean = false,
    val wasCorrect: Boolean? = null,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0
)

/**
 * Role selection card for friend mode
 */
@Composable
fun RoleSelectionCard(
    role: FriendModeRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    GlassCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    TealWave,
                    RoundedCornerShape(20.dp)
                ) else Modifier
            ),
        size = GlassCardSize.Large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = role.emoji,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = role.title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) TealWave else TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = role.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Quiz Master input card - for typing questions
 */
@Composable
fun QuizMasterCard(
    questionNumber: Int,
    onQuestionSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var question by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale),
        size = GlassCardSize.Large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎤",
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Question #$questionNumber",
                    style = MaterialTheme.typography.titleLarge,
                    color = TealWave,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Type a question for your friend!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassTextArea(
                value = question,
                onValueChange = { question = it },
                placeholder = "Enter your question here...",
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
                text = "Ask Question",
                onClick = {
                    if (question.isNotBlank()) {
                        onQuestionSubmit(question)
                        question = ""
                    }
                },
                style = GlassButtonStyle.Accent,
                modifier = Modifier.fillMaxWidth(),
                enabled = question.isNotBlank(),
                leadingIcon = Icons.Filled.Send
            )
        }
    }
}

/**
 * Player answer card - for answering questions
 */
@Composable
fun PlayerAnswerCard(
    question: String,
    questionNumber: Int,
    onAnswerSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var answer by remember { mutableStateOf("") }

    val enterTransition = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = enterTransition
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        size = GlassCardSize.Large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❓",
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Question #$questionNumber",
                    style = MaterialTheme.typography.titleMedium,
                    color = Seafoam
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TealWave.copy(alpha = 0.1f))
                    .border(1.dp, TealWave.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your Answer",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            GlassTextArea(
                value = answer,
                onValueChange = { answer = it },
                placeholder = "Type your answer...",
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
                text = "Submit Answer",
                onClick = {
                    if (answer.isNotBlank()) {
                        onAnswerSubmit(answer)
                        answer = ""
                    }
                },
                style = GlassButtonStyle.Accent,
                modifier = Modifier.fillMaxWidth(),
                enabled = answer.isNotBlank(),
                leadingIcon = Icons.Filled.Check
            )
        }
    }
}

/**
 * Answer reveal card - Quiz Master judges the answer
 */
@Composable
fun AnswerRevealCard(
    question: String,
    playerAnswer: String,
    onJudge: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showButtons = true
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        size = GlassCardSize.Large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯",
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Time to Judge!",
                style = MaterialTheme.typography.titleLarge,
                color = TealWave,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question
            Text(
                text = "Question:",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Player's answer
            Text(
                text = "Their Answer:",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Seafoam.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Text(
                    text = playerAnswer,
                    style = MaterialTheme.typography.titleMedium,
                    color = Seafoam,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Was the answer correct?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn() + scaleIn()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Incorrect",
                        onClick = { onJudge(false) },
                        style = GlassButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Filled.Close
                    )
                    GlassButton(
                        text = "Correct!",
                        onClick = { onJudge(true) },
                        style = GlassButtonStyle.Accent,
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Filled.Check
                    )
                }
            }
        }
    }
}

/**
 * Answer feedback animation
 */
@Composable
fun AnswerFeedback(
    isCorrect: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        delay(1500)
        onContinue()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            Text(
                text = if (isCorrect) "🎉" else "😅",
                fontSize = 80.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isCorrect) "Correct!" else "Not quite!",
                style = MaterialTheme.typography.displaySmall,
                color = if (isCorrect) Success else Warning,
                fontWeight = FontWeight.Bold
            )
            if (isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+20 points",
                    style = MaterialTheme.typography.titleMedium,
                    color = TealWave
                )
            }
        }
    }
}

/**
 * Waiting for friend animation
 */
@Composable
fun WaitingForFriend(
    message: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        )
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⏳",
            fontSize = 64.sp,
            modifier = Modifier.offset(y = (-bounce).dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message + ".".repeat(dotCount.toInt()),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pass the device when ready!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

/**
 * Friend mode scoreboard
 */
@Composable
fun FriendModeScoreboard(
    score: Int,
    correctAnswers: Int,
    totalQuestions: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🌟",
                fontSize = 20.sp
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge,
                color = TealWave,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        // Correct/Total
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "✅",
                fontSize = 20.sp
            )
            Text(
                text = "$correctAnswers/$totalQuestions",
                style = MaterialTheme.typography.titleLarge,
                color = Success,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Correct",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        // Streak
        if (streak > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔥",
                    fontSize = 20.sp
                )
                Text(
                    text = "${streak}x",
                    style = MaterialTheme.typography.titleLarge,
                    color = Warning,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
