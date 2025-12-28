package com.hkgroups.agecalculator.data.repository

import android.util.Log
import com.hkgroups.agecalculator.data.local.ZodiacDao
import com.hkgroups.agecalculator.data.local.toDomainModel
import com.hkgroups.agecalculator.data.local.toEntity
import com.hkgroups.agecalculator.data.model.Compatibility
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.data.remote.ZodiacApiService
import com.hkgroups.agecalculator.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementing offline-first approach for Zodiac data.
 * 
 * Data Flow:
 * 1. Check local database first (Room)
 * 2. If not found, fetch from API
 * 3. Cache API results in database
 * 4. Return cached data
 */
@Singleton
class ZodiacRepository @Inject constructor(
    private val apiService: ZodiacApiService,
    private val zodiacDao: ZodiacDao
) {

    companion object {
        private const val TAG = "ZodiacRepository"
    }

    /**
     * Get a specific zodiac sign by name.
     * Implements offline-first strategy:
     * 1. Check database
     * 2. If not found or incomplete, try API
     * 3. Save API result to database
     * 
     * @param name The name of the zodiac sign
     * @return ZodiacSign if found, null otherwise
     */
    suspend fun getZodiacSign(name: String): ZodiacSign? {
        // Step 1: Check local database first
        val localSign = zodiacDao.getZodiacSign(name)?.toDomainModel()
        
        // If we have complete data locally, return it
        if (localSign != null && localSign.personality.isNotEmpty()) {
            Log.d(TAG, "Returning cached zodiac sign: $name")
            return localSign
        }

        // Step 2: Fetch from network if local data is missing or incomplete
        return try {
            Log.d(TAG, "Fetching zodiac sign from API: $name")
            val dto = apiService.getZodiacSignDetails(name)
            
            // Convert DTO to domain model
            val sign = ZodiacSign(
                name = dto.name,
                symbol = dto.symbol,
                dateRange = dto.dateRange,
                personality = dto.personality,
                rulingPlanet = dto.rulingPlanet,
                element = dto.element,
                strengths = dto.strengths,
                weaknesses = dto.weaknesses,
                compatibilities = dto.compatibilities.map {
                    Compatibility(it.signName, it.rating, it.description)
                }
            )
            
            // Step 3: Save to database for offline access
            zodiacDao.insertZodiacSign(sign.toEntity())
            Log.d(TAG, "Saved zodiac sign to database: $name")
            
            sign
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch zodiac sign from API: $name", e)
            // Return local data even if incomplete, or null if nothing available
            localSign
        }
    }

    /**
     * Get a fresh zodiac sign from the API, bypassing the cache.
     * This is specifically used for notifications to ensure fresh daily horoscope data.
     * 
     * Strategy:
     * 1. Fetch directly from API (skip database check)
     * 2. Update database with fresh data
     * 3. On network failure, fall back to cached data
     * 
     * @param name The name of the zodiac sign
     * @return ZodiacSign if found, null otherwise
     */
    suspend fun getFreshZodiacSign(name: String): ZodiacSign? {
        return try {
            Log.d(TAG, "Fetching FRESH zodiac sign from API: $name")
            val dto = apiService.getZodiacSignDetails(name)
            
            // Convert DTO to domain model
            val sign = ZodiacSign(
                name = dto.name,
                symbol = dto.symbol,
                dateRange = dto.dateRange,
                personality = dto.personality,
                rulingPlanet = dto.rulingPlanet,
                element = dto.element,
                strengths = dto.strengths,
                weaknesses = dto.weaknesses,
                compatibilities = dto.compatibilities.map {
                    Compatibility(it.signName, it.rating, it.description)
                }
            )
            
            // Update database with fresh data
            zodiacDao.insertZodiacSign(sign.toEntity())
            Log.d(TAG, "Updated database with fresh zodiac sign: $name")
            
            sign
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch fresh zodiac sign from API: $name, falling back to cache", e)
            // Fall back to cached data if network fails
            zodiacDao.getZodiacSign(name)?.toDomainModel()
        }
    }

    /**
     * Get all zodiac signs as a reactive Flow with Resource wrapper.
     * 
     * Flow Emission Strategy:
     * 1. Emit Loading with cached data (if available)
     * 2. Emit Success with database data
     * 3. Attempt network refresh in background
     * 4. If network succeeds, database updates trigger new emission
     * 5. If network fails, emit Error with cached data (app still works offline)
     * 
     * @return Flow emitting Resource states with zodiac signs data
     */
    fun getZodiacSigns(): Flow<Resource<List<ZodiacSign>>> = flow {
        // Step 1: Emit loading state with cached data if available
        val cachedSigns = zodiacDao.getAllZodiacSignsOnce().map { it.toDomainModel() }
        emit(Resource.Loading(data = cachedSigns.takeIf { it.isNotEmpty() }))
        
        // Step 2: Emit success with database data
        if (cachedSigns.isNotEmpty()) {
            Log.d(TAG, "Emitting ${cachedSigns.size} cached zodiac signs")
            emit(Resource.Success(data = cachedSigns))
        } else {
            Log.d(TAG, "No cached data found, database is empty")
            emit(Resource.Success(data = emptyList()))
        }
        
        // Step 3: Attempt network refresh in background
        try {
            Log.d(TAG, "Attempting to refresh zodiac signs from network")
            val signNames = listOf(
                "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
                "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
            )
            
            // Fetch from API (this will automatically cache to database)
            val networkSigns = signNames.mapNotNull { name ->
                try {
                    getZodiacSign(name) // This handles caching internally
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch $name from network", e)
                    null
                }
            }
            
            if (networkSigns.isNotEmpty()) {
                Log.d(TAG, "Successfully refreshed ${networkSigns.size} zodiac signs from network")
                // Database update will trigger Flow update if using getZodiacSignsFlow()
                // For this single emission, we emit success with fresh data
                emit(Resource.Success(data = networkSigns))
            }
        } catch (e: Exception) {
            // Step 5: Network failed, but emit error with cached data so app still works
            Log.e(TAG, "Network refresh failed: ${e.message}", e)
            val fallbackData = zodiacDao.getAllZodiacSignsOnce().map { it.toDomainModel() }
            
            if (fallbackData.isNotEmpty()) {
                emit(Resource.Error(
                    message = "Using offline data. Network unavailable.",
                    data = fallbackData
                ))
            } else {
                emit(Resource.Error(
                    message = "No data available. Please check your connection.",
                    data = null
                ))
            }
        }
    }

    /**
     * Get all zodiac signs (legacy method for backward compatibility).
     * Returns cached data if available, otherwise creates placeholders.
     * 
     * @return List of all 12 zodiac signs
     */
    suspend fun getZodiacSignsLegacy(): List<ZodiacSign> {
        // Check if we have data in the database
        val localSigns = zodiacDao.getAllZodiacSignsOnce()
        
        if (localSigns.isNotEmpty()) {
            Log.d(TAG, "Returning ${localSigns.size} cached zodiac signs")
            return localSigns.map { it.toDomainModel() }
        }

        // If database is empty, seed it with placeholder data
        Log.d(TAG, "Database empty, creating placeholder zodiac signs")
        val placeholderSigns = createPlaceholderSigns()
        
        // Save placeholders to database
        try {
            zodiacDao.insertZodiacSigns(placeholderSigns.map { it.toEntity() })
            Log.d(TAG, "Saved placeholder signs to database")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save placeholder signs", e)
        }
        
        return placeholderSigns
    }

    /**
     * Get all zodiac signs as a reactive Flow.
     * The Flow will emit new values whenever database data changes.
     * 
     * @return Flow of List<ZodiacSign>
     */
    fun getZodiacSignsFlow(): Flow<List<ZodiacSign>> {
        return zodiacDao.getAllZodiacSigns().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Fetch and cache all zodiac signs from the API.
     * Useful for initial data loading or manual refresh.
     * 
     * @return List of fetched signs, or empty list if failed
     */
    suspend fun fetchAndCacheAllSigns(): List<ZodiacSign> {
        return try {
            Log.d(TAG, "Fetching all zodiac signs from API")
            val signNames = listOf(
                "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
                "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
            )
            
            val fetchedSigns = signNames.mapNotNull { name ->
                getZodiacSign(name) // This will automatically cache each sign
            }
            
            Log.d(TAG, "Successfully fetched ${fetchedSigns.size} zodiac signs")
            fetchedSigns
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch all zodiac signs", e)
            emptyList()
        }
    }

    /**
     * Create placeholder zodiac signs with basic information.
     * Used when API is unavailable or for initial seeding.
     */
    private fun createPlaceholderSigns(): List<ZodiacSign> {
        val names = listOf(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        )

        return names.map { name ->
            ZodiacSign(
                name = name,
                symbol = when (name) {
                    "Aries" -> "♈"; "Taurus" -> "♉"; "Gemini" -> "♊"; "Cancer" -> "♋"
                    "Leo" -> "♌"; "Virgo" -> "♍"; "Libra" -> "♎"; "Scorpio" -> "♏"
                    "Sagittarius" -> "♐"; "Capricorn" -> "♑"; "Aquarius" -> "♒"; else -> "♓"
                },
                dateRange = when (name) {
                    "Aries" -> "Mar 21 - Apr 19"; "Taurus" -> "Apr 20 - May 20"
                    "Gemini" -> "May 21 - Jun 20"; "Cancer" -> "Jun 21 - Jul 22"
                    "Leo" -> "Jul 23 - Aug 22"; "Virgo" -> "Aug 23 - Sep 22"
                    "Libra" -> "Sep 23 - Oct 22"; "Scorpio" -> "Oct 23 - Nov 21"
                    "Sagittarius" -> "Nov 22 - Dec 21"; "Capricorn" -> "Dec 22 - Jan 19"
                    "Aquarius" -> "Jan 20 - Feb 18"; else -> "Feb 19 - Mar 20"
                },
                personality = "Discover your ${name} traits in the zodiac explorer.",
                compatibilities = emptyList(),
                rulingPlanet = "",
                element = "",
                strengths = emptyList(),
                weaknesses = emptyList()
            )
        }
    }

    /**
     * Get a daily tip for a specific zodiac sign.
     * 
     * @param zodiacName Name of the zodiac sign
     * @return Daily tip string
     */
    fun getDailyTip(zodiacName: String): String {
        val genericTips = listOf(
            "Today is a great day for bold action and starting new projects.",
            "Focus on stability and comfort. Enjoy the simple pleasures.",
            "Communication is key. Express your ideas clearly and listen to others.",
            "Trust your intuition. It's a good day for self-care and reflection.",
            "Your natural creativity is shining. Don't be afraid to take the spotlight."
        )
        return "For $zodiacName: ${genericTips.random()}"
    }

    /**
     * Get a daily horoscope for a specific zodiac sign.
     * 
     * @param signName Name of the zodiac sign
     * @return Daily horoscope string
     */
    fun getDailyHoroscope(signName: String): String {
        return when (signName) {
            "Aries" -> "Your dynamic energy is at a peak. It's a great day to tackle a challenging project you've been putting off. Your leadership will shine."
            "Taurus" -> "Focus on financial matters today. An opportunity for a smart investment or saving strategy may present itself. Trust your practical instincts."
            "Gemini" -> "Your social life is buzzing. Reconnect with old friends or make new ones. A conversation today could lead to an exciting new idea."
            "Cancer" -> "Home and family take center stage. Create a cozy atmosphere and spend quality time with loved ones."
            "Leo" -> "Your charisma is magnetic today. Share your ideas with confidence and watch others rally behind you."
            "Virgo" -> "Details matter more than usual. Your analytical skills help you solve a complex problem efficiently."
            "Libra" -> "Balance and harmony are within reach. Mediate a conflict or create beauty in your surroundings."
            "Scorpio" -> "Your intensity serves you well. Dive deep into a passion project or meaningful conversation."
            "Sagittarius" -> "Adventure calls! Explore new perspectives, whether through travel, learning, or meeting new people."
            "Capricorn" -> "Your discipline pays off. Long-term goals are coming into focus. Stay the course."
            "Aquarius" -> "Innovation is your superpower today. Think outside the box and challenge conventions."
            "Pisces" -> "Your empathy and creativity flow freely. Express yourself through art or help someone in need."
            else -> "A day of reflection and calm is ahead. Take time for yourself to recharge and plan your next moves."
        }
    }

    /**
     * Get a list of historical events from the database.
     * 
     * @return List of major historical events
     */
    suspend fun getHistoricalEvents(): List<HistoricalEvent> {
        return try {
            zodiacDao.getAllHistoricalEventsOnce().map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch historical events", e)
            emptyList()
        }
    }

    /**
     * Clear all cached zodiac data from the database.
     * Useful for testing or forcing a refresh.
     */
    suspend fun clearCache() {
        try {
            zodiacDao.deleteAllZodiacSigns()
            Log.d(TAG, "Cleared all cached zodiac signs")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
}