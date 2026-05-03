package com.hkgroups.agecalculator.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.random.Random

/**
 * Fires once a day at 8pm. Posts a gentle reminder to log today's mood — but
 * only if the user hasn't already logged one today, and only if they have an
 * existing streak (so brand-new users don't get nagged).
 *
 * Honors the daily-horoscope notification preference as a single opt-out
 * for all engagement nudges (we don't want a separate toggle per worker).
 */
@HiltWorker
class MoodReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!settingsRepository.notificationsEnabled.first()) {
            return Result.success()
        }

        val today = LocalDate.now()
        val entries = settingsRepository.moodEntries.first()
        val alreadyLogged = entries.any { it.date == today }
        if (alreadyLogged) return Result.success()

        // Don't bug users who haven't even started a streak — they're brand
        // new and a reminder for an empty habit feels presumptuous.
        val streak = settingsRepository.currentStreak.first()
        if (streak < 1) return Result.success()

        val helper = NotificationHelper(applicationContext)
        helper.createMoodReminderChannel()

        val (title, body) = MOOD_PROMPTS.random(Random.Default)
        helper.showMoodReminderNotification(title = title, body = body)

        return Result.success()
    }

    companion object {
        // Variety in copy keeps the reminder from feeling robotic — picks a
        // random one each evening.
        val MOOD_PROMPTS = listOf(
            "How was today?" to "Take a breath and log your mood before the day closes.",
            "Sky check" to "What was the weather of your inner world today?",
            "Before you sleep" to "One quick check-in keeps your streak alive.",
            "Tonight's reflection" to "Tap to log how today felt — your future self will thank you.",
            "Cosmic pause" to "How did the day land? A 5-second log keeps the cycle going."
        )
    }
}
