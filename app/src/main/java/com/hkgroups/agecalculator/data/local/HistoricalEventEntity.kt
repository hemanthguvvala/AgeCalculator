package com.hkgroups.agecalculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import java.time.LocalDate

/**
 * Room Entity representing a Historical Event in the local database.
 * Maps the domain model HistoricalEvent to a database table.
 */
@Entity(tableName = "historical_events")
@TypeConverters(DateConverter::class)
data class HistoricalEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val title: String,
    val description: String
)

/**
 * Extension function to convert HistoricalEventEntity to domain model HistoricalEvent
 */
fun HistoricalEventEntity.toDomainModel(): HistoricalEvent {
    return HistoricalEvent(
        date = date,
        title = title,
        description = description
    )
}

/**
 * Extension function to convert domain model HistoricalEvent to HistoricalEventEntity
 */
fun HistoricalEvent.toEntity(): HistoricalEventEntity {
    return HistoricalEventEntity(
        date = date,
        title = title,
        description = description
    )
}
