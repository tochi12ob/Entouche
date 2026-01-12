package en.entouche.data.repository

import en.entouche.data.SupabaseClient
import en.entouche.data.models.CreateMoodLog
import en.entouche.data.models.MoodLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.*

class MoodRepository {
    private val postgrest = SupabaseClient.postgrest
    private val authRepository = AuthRepository()

    private fun getCurrentUserId(): String? = authRepository.currentUserId

    private fun today(): String {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    }

    /**
     * Get today's mood log
     */
    suspend fun getTodayMood(): Result<MoodLog?> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val todayDate = today()

        val result = postgrest.from("mood_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("logged_at", todayDate)
                }
            }
            .decodeList<MoodLog>()

        result.firstOrNull()
    }

    /**
     * Log or update today's mood
     */
    suspend fun logMood(mood: Int, note: String? = null): Result<MoodLog> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val todayDate = today()

        // Check if mood already logged today
        val existingMood = getTodayMood().getOrNull()

        if (existingMood != null) {
            // Update existing mood
            val result = postgrest.from("mood_logs")
                .update({
                    set("mood", mood)
                    note?.let { set("note", it) }
                }) {
                    filter {
                        eq("id", existingMood.id)
                    }
                    select()
                }
                .decodeSingle<MoodLog>()
            result
        } else {
            // Create new mood log
            val newMood = CreateMoodLog(
                userId = userId,
                mood = mood,
                note = note,
                loggedAt = todayDate
            )

            val result = postgrest.from("mood_logs")
                .insert(newMood) {
                    select()
                }
                .decodeSingle<MoodLog>()
            result
        }
    }

    /**
     * Get mood history for the last N days
     */
    suspend fun getMoodHistory(days: Int = 7): Result<List<MoodLog>> = runCatching {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val startDate = Clock.System.now()
            .minus(days.toLong(), DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()

        val result = postgrest.from("mood_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("logged_at", startDate)
                }
                order("logged_at", Order.DESCENDING)
            }
            .decodeList<MoodLog>()

        result
    }

    /**
     * Get mood streak (consecutive days logged)
     */
    suspend fun getMoodStreak(): Result<Int> = runCatching {
        val history = getMoodHistory(30).getOrThrow()
        if (history.isEmpty()) return@runCatching 0

        var streak = 0
        var currentDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        for (log in history.sortedByDescending { it.loggedAt }) {
            val logDate = LocalDate.parse(log.loggedAt)
            if (logDate == currentDate || logDate == currentDate.minus(1, DateTimeUnit.DAY)) {
                streak++
                currentDate = logDate
            } else {
                break
            }
        }

        streak
    }
}
