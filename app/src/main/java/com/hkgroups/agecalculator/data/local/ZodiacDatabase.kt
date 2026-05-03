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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Room Database for the Zodiac Age application.
 * Manages local persistence of zodiac sign data and historical events.
 */
@Database(
    entities = [ZodiacSignEntity::class, HistoricalEventEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(ZodiacTypeConverters::class, DateConverter::class)
abstract class ZodiacDatabase : RoomDatabase() {

    abstract fun zodiacDao(): ZodiacDao

    companion object {
        private const val DATABASE_NAME = "zodiac_database"
        private const val TAG = "ZodiacDatabase"

        @Volatile
        private var INSTANCE: ZodiacDatabase? = null

        // Single seed scope so the seeding job is observable / cancellable, not
        // a fire-and-forget anonymous scope.
        private val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        @Volatile
        private var seedJob: Job? = null

        fun getInstance(context: Context): ZodiacDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ZodiacDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                ZodiacDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(*DatabaseMigrations.getAllMigrations())
                // Safety net: if a migration is missing (e.g. dev forgets to add
                // one before bumping version), fall back to destructive migration
                // *from version 1 only*. This protects existing v1 users from a
                // hard crash on launch when v2+ ships without explicit migrations.
                .fallbackToDestructiveMigrationFrom(false, 1)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedJob = seedScope.launch { seedDatabase(context) }
                    }
                })

            return builder.build()
        }

        private suspend fun seedDatabase(context: Context) {
            try {
                val dao = getInstance(context).zodiacDao()
                val zodiacSigns = InitialDataSource.getZodiacSigns()
                dao.insertZodiacSigns(zodiacSigns.map { it.toEntity() })
                val events = InitialDataSource.getHistoricalEvents()
                dao.insertHistoricalEvents(events.map { it.toEntity() })
                Log.d(TAG, "Seeded ${zodiacSigns.size} signs, ${events.size} events")
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding database", e)
            }
        }
    }
}
