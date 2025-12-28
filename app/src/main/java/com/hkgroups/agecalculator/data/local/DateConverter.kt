package com.hkgroups.agecalculator.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Type converter for Room Database to handle LocalDate conversions.
 * Converts LocalDate to Long (timestamp) and vice versa for database storage.
 */
class DateConverter {

    /**
     * Convert Long timestamp to LocalDate
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    /**
     * Convert LocalDate to Long timestamp
     */
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }
}
