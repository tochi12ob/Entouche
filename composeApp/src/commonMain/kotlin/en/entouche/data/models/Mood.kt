package en.entouche.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mood levels from 1-5
 */
enum class MoodLevel(val value: Int, val emoji: String, val label: String) {
    VERY_SAD(1, "😢", "Very Sad"),
    SAD(2, "😕", "Sad"),
    NEUTRAL(3, "😐", "Okay"),
    HAPPY(4, "🙂", "Good"),
    VERY_HAPPY(5, "😊", "Great");

    companion object {
        fun fromValue(value: Int): MoodLevel = entries.find { it.value == value } ?: NEUTRAL
    }
}

/**
 * Mood log entry
 */
@Serializable
data class MoodLog(
    val id: String = "",
    @SerialName("user_id")
    val userId: String,
    val mood: Int,
    val note: String? = null,
    @SerialName("logged_at")
    val loggedAt: String,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val moodLevel: MoodLevel get() = MoodLevel.fromValue(mood)
}

/**
 * For creating/updating mood logs
 */
@Serializable
data class CreateMoodLog(
    @SerialName("user_id")
    val userId: String,
    val mood: Int,
    val note: String? = null,
    @SerialName("logged_at")
    val loggedAt: String
)
