package com.hkgroups.agecalculator.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.data.repository.ZodiacRepository
import com.hkgroups.agecalculator.domain.usecase.FindZodiacSignUseCase
import com.hkgroups.agecalculator.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.util.Date

@HiltWorker
class HoroscopeNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val zodiacRepository: ZodiacRepository,
    private val findZodiacSignUseCase: FindZodiacSignUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.createNotificationChannel()

        // Fetch the user's saved birth date
        val savedBirthDateMillis =
            settingsRepository.savedBirthDate.first() ?: return Result.failure()
        val birthDate =
            Instant.ofEpochMilli(savedBirthDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()

        // Find their sign using cached data first (for quick lookup)
        val cachedSigns = zodiacRepository.getZodiacSignsLegacy()
        val userSign = findZodiacSignUseCase(birthDate, cachedSigns) ?: return Result.failure()
        
        // Fetch FRESH sign data from network (mock API) to get randomized daily horoscope
        // This bypasses the cache to ensure we get new randomized data from MockApiInterceptor
        val freshSign = zodiacRepository.getFreshZodiacSign(userSign.name)
        
        // Get the horoscope from fresh data if available, otherwise use repository method
        val horoscope = freshSign?.let { 
            zodiacRepository.getDailyHoroscope(it.name) 
        } ?: zodiacRepository.getDailyHoroscope(userSign.name)

        // Show the notification with fresh data
        notificationHelper.showHoroscopeNotification(userSign.name, horoscope)

        return Result.success()
    }
}