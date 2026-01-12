package en.entouche.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import en.entouche.data.models.MoodLevel
import en.entouche.data.models.MoodLog
import en.entouche.ui.theme.*

@Composable
fun MoodTracker(
    todayMood: MoodLog?,
    moodStreak: Int,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMood by remember(todayMood) { mutableStateOf(todayMood?.mood) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        size = GlassCardSize.Medium
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "How are you feeling?",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (todayMood != null) "Tap to update" else "Tap to log your mood",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Streak badge
                if (moodStreak > 0) {
                    StreakBadge(streak = moodStreak)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood emoji row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MoodLevel.entries.forEach { mood ->
                    MoodButton(
                        moodLevel = mood,
                        isSelected = selectedMood == mood.value,
                        onClick = {
                            selectedMood = mood.value
                            onMoodSelected(mood.value)
                        }
                    )
                }
            }

            // Show selected mood label
            AnimatedVisibility(
                visible = selectedMood != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedMood?.let { mood ->
                    val moodLevel = MoodLevel.fromValue(mood)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Feeling ${moodLevel.label.lowercase()} today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TealWave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodButton(
    moodLevel: MoodLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) TealWave.copy(alpha = 0.2f) else GlassWhite,
        animationSpec = tween(200)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) TealWave else GlassBorder,
        animationSpec = tween(200)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Text(
            text = moodLevel.emoji,
            fontSize = 28.sp
        )
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Warning.copy(alpha = 0.2f),
                        Warning.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Warning.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streak day${if (streak > 1) "s" else ""}",
            style = MaterialTheme.typography.labelMedium,
            color = Warning
        )
    }
}

@Composable
fun MoodHistoryRow(
    moodHistory: List<MoodLog>,
    modifier: Modifier = Modifier
) {
    if (moodHistory.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        moodHistory.take(7).reversed().forEach { log ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = MoodLevel.fromValue(log.mood).emoji,
                    fontSize = 20.sp
                )
                Text(
                    text = log.loggedAt.takeLast(5).replace("-", "/"),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
