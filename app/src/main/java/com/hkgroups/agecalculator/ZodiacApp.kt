package com.hkgroups.agecalculator

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.util.BillingController
import com.hkgroups.agecalculator.util.FanAdsController
import com.hkgroups.agecalculator.worker.CosmicEventNotificationWorker
import com.hkgroups.agecalculator.worker.HoroscopeNotificationWorker
import com.hkgroups.agecalculator.worker.MoodReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ZodiacAgeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsRepository: SettingsRepository

    /** Process-scoped controller. Reads the latest `adsDisabled` value
     *  per request — flipping the flag at runtime takes effect immediately. */
    lateinit var adsController: FanAdsController
        private set

    /** Process-scoped Play Billing controller. Mirrors ownership into
     *  [SettingsRepository.adsDisabled] so the rest of the app reads one flag. */
    lateinit var billingController: BillingController
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile
    private var cachedAdsDisabled: Boolean = false

    // --- THIS IS THE CORRECTED IMPLEMENTATION ---
    // Overriding as a 'val' with a custom getter to satisfy the interface
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyHoroscope()
        scheduleCosmicEventChecks()
        scheduleMoodReminder()

        // Cache the ads-disabled flag so the controller can read it
        // synchronously without blocking ad-load callbacks. Updates flow
        // through asynchronously when the user flips the flag in settings.
        appScope.launch {
            settingsRepository.adsDisabled.collect { cachedAdsDisabled = it }
        }
        adsController = FanAdsController(
            applicationContext = applicationContext,
            adsDisabledProvider = { cachedAdsDisabled }
        )
        adsController.initialize()

        billingController = BillingController(
            context = applicationContext,
            settingsRepository = settingsRepository,
            scope = appScope
        )
        billingController.start()
    }

    /** Daily 8pm reminder to log today's mood — second daily-visit pull
     *  alongside the 8am horoscope. Worker self-skips if user already logged. */
    private fun scheduleMoodReminder() {
        val current = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(current)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelay = target.timeInMillis - current.timeInMillis
        val request = PeriodicWorkRequestBuilder<MoodReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MoodReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Daily 7am check for imminent cosmic events. The worker itself decides
     *  whether to notify based on the user's preference. */
    private fun scheduleCosmicEventChecks() {
        val current = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(current)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelay = target.timeInMillis - current.timeInMillis

        val request = PeriodicWorkRequestBuilder<CosmicEventNotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CosmicEventNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleDailyHoroscope() {
        // Calculate the initial delay to next 8:00 AM
        val currentCalendar = Calendar.getInstance()
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If 8:00 AM has already passed today, schedule for tomorrow
            if (before(currentCalendar)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        // Calculate delay in milliseconds
        val initialDelayMillis = targetCalendar.timeInMillis - currentCalendar.timeInMillis
        
        // Create a periodic work request that runs every 24 hours starting at 8:00 AM
        val dailyWorkRequest = PeriodicWorkRequestBuilder<HoroscopeNotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        // Enqueue the work, replacing any existing work to respect new schedule
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyHoroscopeNotification",
            ExistingPeriodicWorkPolicy.REPLACE, // Replace to ensure 8:00 AM schedule
            dailyWorkRequest
        )
    }
}