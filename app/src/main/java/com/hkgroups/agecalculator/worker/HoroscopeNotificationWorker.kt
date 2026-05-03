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
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class HoroscopeNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val zodiacRepository: ZodiacRepository,
    private val findZodiacSignUseCase: FindZodiacSignUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Honor the user's notification preference. Returning success (not failure)
        // keeps the daily WorkManager schedule alive so re-enabling immediately works.
        if (!settingsRepository.notificationsEnabled.first()) {
            return Result.success()
        }

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.createNotificationChannel()

        val savedBirthDateMillis =
            settingsRepository.savedBirthDate.first() ?: return Result.failure()
        val birthDate =
            Instant.ofEpochMilli(savedBirthDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()

        val cachedSigns = zodiacRepository.getZodiacSignsLegacy()
        val userSign = findZodiacSignUseCase(birthDate, cachedSigns) ?: return Result.failure()

        // Deterministic daily horoscope from the on-device content engine —
        // no network, but produces fresh content every day from astronomical state.
        val horoscope = zodiacRepository.getDailyHoroscope(userSign.name, LocalDate.now())

        notificationHelper.showHoroscopeNotification(userSign.name, horoscope)
        return Result.success()
    }
}
