package com.hkgroups.agecalculator.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.ui.screen.components.CosmicEventKind
import com.hkgroups.agecalculator.ui.screen.components.upcomingCosmicEvents
import com.hkgroups.agecalculator.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Fires once a day. If a notable cosmic event lands within the next 24h, posts
 * a heads-up notification. Honors the user's `cosmicEventNotificationsEnabled`
 * preference but always returns success so the periodic schedule keeps living
 * even when the user opts out.
 */
@HiltWorker
class CosmicEventNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!settingsRepository.cosmicEventNotificationsEnabled.first()) {
            return Result.success()
        }

        val today = LocalDate.now()
        val upcoming = upcomingCosmicEvents(today = today, count = 6)
        val imminent = upcoming.firstOrNull { event ->
            val days = ChronoUnit.DAYS.between(today, event.date)
            days in 0..1L
        } ?: return Result.success()

        val helper = NotificationHelper(applicationContext)
        helper.createCosmicEventsChannel()

        val daysAway = ChronoUnit.DAYS.between(today, imminent.date)
        val whenLine = if (daysAway == 0L) "happening today" else "tomorrow"
        val body = when (imminent.kind) {
            CosmicEventKind.Retrograde ->
                "Mercury Retrograde begins $whenLine. Slow down with messages, contracts, and travel."
            CosmicEventKind.Equinox ->
                "${imminent.title} arrives $whenLine — a moment of cosmic balance."
            CosmicEventKind.Solstice ->
                "${imminent.title} arrives $whenLine — peak light or peak shadow."
            CosmicEventKind.Eclipse ->
                "${imminent.title} arrives $whenLine. Powerful threshold energy — set intentions."
            CosmicEventKind.NewMoon ->
                "New Moon $whenLine. Plant intentions for the coming cycle."
            CosmicEventKind.FullMoon ->
                "Full Moon $whenLine. Time to release and integrate."
        }

        helper.showCosmicEventNotification(
            title = imminent.title,
            body = body
        )

        return Result.success()
    }
}
