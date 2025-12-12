package com.hkgroups.agecalculator

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hkgroups.agecalculator.worker.HoroscopeNotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ZodiacAgeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // --- THIS IS THE CORRECTED IMPLEMENTATION ---
    // Overriding as a 'val' with a custom getter to satisfy the interface
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyHoroscope()
    }

    private fun scheduleDailyHoroscope() {
        // Create a periodic work request to run roughly every 24 hours
        val dailyWorkRequest = PeriodicWorkRequestBuilder<HoroscopeNotificationWorker>(
            24, TimeUnit.HOURS
        ).build()

        // Enqueue the work, keeping the existing work if it's already scheduled
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyHoroscopeNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}