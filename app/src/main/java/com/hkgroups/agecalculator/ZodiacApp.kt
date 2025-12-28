package com.hkgroups.agecalculator

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hkgroups.agecalculator.worker.HoroscopeNotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
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