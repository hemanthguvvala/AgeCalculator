package com.hkgroups.agecalculator.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations for ZodiacDatabase.
 * Add new migration objects here when the database schema changes.
 * 
 * Migration naming convention: MIGRATION_X_Y
 * where X is the old version and Y is the new version.
 */
object DatabaseMigrations {
    
    /**
     * Example migration from version 1 to version 2.
     * Uncomment and modify when you need to add a migration.
     * 
     * Example use cases:
     * - Adding a new column
     * - Renaming a table
     * - Adding indexes
     * - Changing data types
     */
    /*
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add a new column to the zodiac_signs table
            database.execSQL(
                "ALTER TABLE zodiac_signs ADD COLUMN lucky_number INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
    */
    
    /**
     * Example migration from version 2 to version 3.
     */
    /*
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Create a new table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_favorites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    sign_name TEXT NOT NULL,
                    is_favorite INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(sign_name) REFERENCES zodiac_signs(name) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            
            // Create an index for better query performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_user_favorites_sign_name ON user_favorites(sign_name)"
            )
        }
    }
    */
    
    /**
     * Get all available migrations as an array.
     * Add new migrations to this array when you create them.
     * 
     * @return Array of all migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // MIGRATION_1_2,
            // MIGRATION_2_3,
            // Add more migrations here as needed
        )
    }
}
