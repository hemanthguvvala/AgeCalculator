package com.hkgroups.agecalculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Zodiac Sign operations.
 * Defines database operations for the zodiac_signs table.
 */
@Dao
interface ZodiacDao {

    /**
     * Insert a list of zodiac signs into the database.
     * If a sign with the same name exists, it will be replaced.
     *
     * @param signs List of ZodiacSignEntity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZodiacSigns(signs: List<ZodiacSignEntity>)

    /**
     * Insert a single zodiac sign into the database.
     * If a sign with the same name exists, it will be replaced.
     *
     * @param sign ZodiacSignEntity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZodiacSign(sign: ZodiacSignEntity)

    /**
     * Get a specific zodiac sign by name.
     *
     * @param name The name of the zodiac sign (e.g., "Aries", "Taurus")
     * @return ZodiacSignEntity if found, null otherwise
     */
    @Query("SELECT * FROM zodiac_signs WHERE name = :name")
    suspend fun getZodiacSign(name: String): ZodiacSignEntity?

    /**
     * Get all zodiac signs from the database as a Flow.
     * The Flow will emit new values whenever the data changes.
     *
     * @return Flow of List<ZodiacSignEntity>
     */
    @Query("SELECT * FROM zodiac_signs ORDER BY name ASC")
    fun getAllZodiacSigns(): Flow<List<ZodiacSignEntity>>

    /**
     * Get all zodiac signs as a one-time fetch (not reactive).
     *
     * @return List<ZodiacSignEntity>
     */
    @Query("SELECT * FROM zodiac_signs ORDER BY name ASC")
    suspend fun getAllZodiacSignsOnce(): List<ZodiacSignEntity>

    /**
     * Delete all zodiac signs from the database.
     * Useful for clearing cache or resetting data.
     */
    @Query("DELETE FROM zodiac_signs")
    suspend fun deleteAllZodiacSigns()

    /**
     * Get the count of zodiac signs in the database.
     * Useful for checking if data has been seeded.
     *
     * @return Number of zodiac signs in the database
     */
    @Query("SELECT COUNT(*) FROM zodiac_signs")
    suspend fun getZodiacSignCount(): Int

    // ========== Historical Events Operations ==========

    /**
     * Insert a list of historical events into the database.
     * If an event with the same ID exists, it will be replaced.
     *
     * @param events List of HistoricalEventEntity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalEvents(events: List<HistoricalEventEntity>)

    /**
     * Get all historical events from the database as a Flow.
     * The Flow will emit new values whenever the data changes.
     *
     * @return Flow of List<HistoricalEventEntity> ordered by date ascending
     */
    @Query("SELECT * FROM historical_events ORDER BY date ASC")
    fun getAllHistoricalEvents(): Flow<List<HistoricalEventEntity>>

    /**
     * Get all historical events as a one-time fetch (not reactive).
     *
     * @return List<HistoricalEventEntity> ordered by date ascending
     */
    @Query("SELECT * FROM historical_events ORDER BY date ASC")
    suspend fun getAllHistoricalEventsOnce(): List<HistoricalEventEntity>

    /**
     * Get the count of historical events in the database.
     * Useful for checking if data has been seeded.
     *
     * @return Number of historical events in the database
     */
    @Query("SELECT COUNT(*) FROM historical_events")
    suspend fun getHistoricalEventCount(): Int

    /**
     * Delete all historical events from the database.
     */
    @Query("DELETE FROM historical_events")
    suspend fun deleteAllHistoricalEvents()
}
