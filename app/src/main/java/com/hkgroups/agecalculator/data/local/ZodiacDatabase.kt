package com.hkgroups.agecalculator.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Room Database for the Zodiac Age application.
 * Manages local persistence of zodiac sign data and historical events.
 */
@Database(
    entities = [ZodiacSignEntity::class, HistoricalEventEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ZodiacTypeConverters::class, DateConverter::class)
abstract class ZodiacDatabase : RoomDatabase() {

    /**
     * Provides access to the ZodiacDao for database operations.
     */
    abstract fun zodiacDao(): ZodiacDao

    companion object {
        /**
         * Database name
         */
        private const val DATABASE_NAME = "zodiac_database"
        private const val TAG = "ZodiacDatabase"

        /**
         * Singleton instance of the database.
         * Volatile ensures that changes to INSTANCE are immediately visible to other threads.
         */
        @Volatile
        private var INSTANCE: ZodiacDatabase? = null

        /**
         * Get the singleton database instance.
         * Uses double-checked locking pattern for thread safety.
         *
         * @param context Application context
         * @param useMigrations If true, use proper migrations; if false, use destructive migration (dev only)
         * @return ZodiacDatabase instance
         */
        fun getInstance(context: Context, useMigrations: Boolean = false): ZodiacDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ZodiacDatabase::class.java,
                    DATABASE_NAME
                )
                
                // Choose migration strategy based on environment
                if (useMigrations) {
                    // Production: Use proper migrations to preserve user data
                    Log.d(TAG, "Using migration strategy with ${DatabaseMigrations.getAllMigrations().size} migrations")
                    builder.addMigrations(*DatabaseMigrations.getAllMigrations())
                } else {
                    // Development: Use destructive migration for faster iteration
                    Log.d(TAG, "Using destructive migration (development mode)")
                    builder.fallbackToDestructiveMigration()
                }
                
                val instance = builder
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created - starting initial data seeding")

                            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                                try {
                                    val dao = getInstance(context).zodiacDao()

                                    // Seed zodiac signs
                                    Log.d(TAG, "Seeding zodiac signs...")
                                    val zodiacSigns = InitialDataSource.getZodiacSigns()
                                    dao.insertZodiacSigns(zodiacSigns.map { it.toEntity() })
                                    Log.d(TAG, "Successfully seeded ${zodiacSigns.size} zodiac signs")

                                    // Seed historical events
                                    Log.d(TAG, "Seeding historical events...")
                                    val events = InitialDataSource.getHistoricalEvents()
                                    dao.insertHistoricalEvents(events.map { it.toEntity() })
                                    Log.d(TAG, "Successfully seeded ${events.size} historical events")

                                    Log.d(TAG, "Database seeding completed successfully")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error seeding database", e)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
