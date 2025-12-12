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
        // This function runs before each test
        findZodiacSignUseCase = FindZodiacSignUseCase()

        // Create a dummy list of signs for the test. In a real project,
        // you might load this from a test resource file.
        allSigns = listOf(
            ZodiacSign("Aries", "♈", "", "", emptyList()),
            ZodiacSign("Taurus", "♉", "", "", emptyList()),
            ZodiacSign("Gemini", "♊", "", "", emptyList()),
            ZodiacSign("Cancer", "♋", "", "", emptyList()),
            ZodiacSign("Leo", "♌", "", "", emptyList()),
            ZodiacSign("Virgo", "♍", "", "", emptyList()),
            ZodiacSign("Libra", "♎", "", "", emptyList()),
            ZodiacSign("Scorpio", "♏", "", "", emptyList()),
            ZodiacSign("Sagittarius", "♐", "", "", emptyList()),
            ZodiacSign("Capricorn", "♑", "", "", emptyList()),
            ZodiacSign("Aquarius", "♒", "", "", emptyList()),
            ZodiacSign("Pisces", "♓", "", "", emptyList())
        )
    }

    @Test
    fun `invoke with date for Aries returns correct sign`() {
        // Arrange: Set up the specific data for this test
        val ariesDate = LocalDate.of(2024, 3, 25)

        // Act: Run the code we are testing
        val result = findZodiacSignUseCase(ariesDate, allSigns)

        // Assert: Check if the result is what we expect
        assertEquals("Aries", result?.name)
    }

    @Test
    fun `invoke with date for Capricorn returns correct sign`() {
        // Arrange
        val capricornDate = LocalDate.of(2024, 1, 15)

        // Act
        val result = findZodiacSignUseCase(capricornDate, allSigns)

        // Assert
        assertEquals("Capricorn", result?.name)
    }

    @Test
    fun `invoke with date on a cusp (Leo-Virgo) returns correct sign`() {
        // Arrange
        val virgoCuspDate = LocalDate.of(2024, 8, 23)

        // Act
        val result = findZodiacSignUseCase(virgoCuspDate, allSigns)

        // Assert
        assertEquals("Virgo", result?.name)
    }
}