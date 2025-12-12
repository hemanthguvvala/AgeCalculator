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

        // Find their sign and get the horoscope
        val allSigns = zodiacRepository.getZodiacSigns()
        val userSign = findZodiacSignUseCase(birthDate, allSigns) ?: return Result.failure()
        val horoscope = zodiacRepository.getDailyHoroscope(userSign.name)

        // Show the notification
        notificationHelper.showHoroscopeNotification(userSign.name, horoscope)

        return Result.success()
    }
}