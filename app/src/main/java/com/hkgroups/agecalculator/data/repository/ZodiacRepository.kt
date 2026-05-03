package com.hkgroups.agecalculator.data.repository

import android.util.Log
import com.hkgroups.agecalculator.content.ContentEngine
import com.hkgroups.agecalculator.data.local.ZodiacDao
import com.hkgroups.agecalculator.data.local.toDomainModel
import com.hkgroups.agecalculator.data.local.toEntity
import com.hkgroups.agecalculator.data.local.InitialDataSource
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-first repository. Reads from Room (seeded by [InitialDataSource]),
 * generates daily horoscope/tip text via [ContentEngine] — no network.
 */
@Singleton
class ZodiacRepository @Inject constructor(
    private val zodiacDao: ZodiacDao
) {

    companion object {
        private const val TAG = "ZodiacRepository"
    }

    suspend fun getZodiacSign(name: String): ZodiacSign? {
        val local = zodiacDao.getZodiacSign(name)?.toDomainModel()
        if (local != null) return local

        // Database hasn't been seeded yet — fall back to in-memory seed and
        // persist asynchronously. Avoids a "blank dashboard on first launch"
        // race when the dashboard reads before the seed callback finishes.
        val seeded = InitialDataSource.getZodiacSigns().firstOrNull { it.name == name }
        seeded?.let {
            try {
                zodiacDao.insertZodiacSign(it.toEntity())
            } catch (e: Exception) {
                Log.e(TAG, "Seed-on-miss failed for $name", e)
            }
        }
        return seeded
    }

    fun getZodiacSigns(): Flow<Resource<List<ZodiacSign>>> = flow {
        val cached = zodiacDao.getAllZodiacSignsOnce().map { it.toDomainModel() }
        if (cached.isNotEmpty()) {
            emit(Resource.Success(data = cached))
            return@flow
        }
        // First launch / pre-seed: emit the in-memory seed immediately, then
        // persist it so the reactive flow on the dashboard fires once.
        val seeded = InitialDataSource.getZodiacSigns()
        emit(Resource.Success(data = seeded))
        try {
            zodiacDao.insertZodiacSigns(seeded.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(TAG, "Bulk seed-on-miss failed", e)
        }
    }

    fun getZodiacSignsFlow(): Flow<List<ZodiacSign>> =
        zodiacDao.getAllZodiacSigns().map { entities -> entities.map { it.toDomainModel() } }

    /**
     * Legacy compatibility shim — fetches signs as a one-time list.
     * Kept for the notification worker; new callers should prefer the Flow APIs.
     */
    suspend fun getZodiacSignsLegacy(): List<ZodiacSign> {
        val local = zodiacDao.getAllZodiacSignsOnce()
        if (local.isNotEmpty()) return local.map { it.toDomainModel() }
        val seeded = InitialDataSource.getZodiacSigns()
        try {
            zodiacDao.insertZodiacSigns(seeded.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(TAG, "Legacy seed-on-miss failed", e)
        }
        return seeded
    }

    /** Daily tip via the content engine. Pure deterministic per (sign, date). */
    fun getDailyTip(zodiacName: String, date: LocalDate = LocalDate.now()): String =
        ContentEngine.dailyTip(zodiacName, date)

    /** Daily horoscope via the content engine. Pure deterministic per (sign, date). */
    fun getDailyHoroscope(signName: String, date: LocalDate = LocalDate.now()): String =
        ContentEngine.dailyHoroscope(signName, date)

    /** Question of the day for engagement loops. */
    fun getQuestionOfTheDay(signName: String, date: LocalDate = LocalDate.now()): String =
        ContentEngine.questionOfTheDay(signName, date)

    /** 7-day forecast — premium-gated in the UI but generated for everyone. */
    fun getWeeklyForecast(signName: String, weekStart: LocalDate = LocalDate.now()): List<String> =
        ContentEngine.weeklyForecast(signName, weekStart)

    suspend fun getHistoricalEvents(): List<HistoricalEvent> {
        return try {
            zodiacDao.getAllHistoricalEventsOnce().map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch historical events", e)
            emptyList()
        }
    }

    suspend fun clearCache() {
        try {
            zodiacDao.deleteAllZodiacSigns()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
}
