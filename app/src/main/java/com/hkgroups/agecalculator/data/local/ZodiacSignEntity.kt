package com.hkgroups.agecalculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hkgroups.agecalculator.data.model.Compatibility
import com.hkgroups.agecalculator.data.model.ZodiacSign

/**
 * Room Entity representing a Zodiac Sign in the local database.
 * Maps the domain model ZodiacSign to a database table.
 */
@Entity(tableName = "zodiac_signs")
@TypeConverters(ZodiacTypeConverters::class)
data class ZodiacSignEntity(
    @PrimaryKey
    val name: String,
    val symbol: String,
    val dateRange: String,
    val personality: String,
    val compatibilities: List<Compatibility>,
    val rulingPlanet: String,
    val element: String,
    val strengths: List<String>,
    val weaknesses: List<String>
)

/**
 * Extension function to convert ZodiacSignEntity to domain model ZodiacSign
 */
fun ZodiacSignEntity.toDomainModel(): ZodiacSign {
    return ZodiacSign(
        name = name,
        symbol = symbol,
        dateRange = dateRange,
        personality = personality,
        compatibilities = compatibilities,
        rulingPlanet = rulingPlanet,
        element = element,
        strengths = strengths,
        weaknesses = weaknesses
    )
}

/**
 * Extension function to convert domain model ZodiacSign to ZodiacSignEntity
 */
fun ZodiacSign.toEntity(): ZodiacSignEntity {
    return ZodiacSignEntity(
        name = name,
        symbol = symbol,
        dateRange = dateRange,
        personality = personality,
        compatibilities = compatibilities,
        rulingPlanet = rulingPlanet,
        element = element,
        strengths = strengths,
        weaknesses = weaknesses
    )
}
