package com.hkgroups.agecalculator.domain.usecase

import com.hkgroups.agecalculator.data.model.ZodiacSign
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FindZodiacSignUseCaseTest {

    private lateinit var findZodiacSignUseCase: FindZodiacSignUseCase
    private lateinit var allSigns: List<ZodiacSign>

    @Before
    fun setUp() {
        findZodiacSignUseCase = FindZodiacSignUseCase()
        allSigns = listOf(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        ).map { name ->
            ZodiacSign(
                name = name,
                symbol = "",
                dateRange = "",
                personality = "",
                compatibilities = emptyList(),
                rulingPlanet = "",
                element = "",
                strengths = emptyList(),
                weaknesses = emptyList()
            )
        }
    }

    @Test
    fun `Aries date returns Aries sign`() {
        val result = findZodiacSignUseCase(LocalDate.of(2024, 3, 25), allSigns)
        assertEquals("Aries", result?.name)
    }

    @Test
    fun `Capricorn date returns Capricorn sign`() {
        val result = findZodiacSignUseCase(LocalDate.of(2024, 1, 15), allSigns)
        assertEquals("Capricorn", result?.name)
    }

    @Test
    fun `cusp date returns later sign`() {
        val result = findZodiacSignUseCase(LocalDate.of(2024, 8, 23), allSigns)
        assertEquals("Virgo", result?.name)
    }

    @Test
    fun `February 19 returns Pisces`() {
        val result = findZodiacSignUseCase(LocalDate.of(2024, 2, 19), allSigns)
        assertEquals("Pisces", result?.name)
    }
}
