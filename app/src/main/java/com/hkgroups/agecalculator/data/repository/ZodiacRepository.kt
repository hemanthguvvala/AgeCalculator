package com.hkgroups.agecalculator.data.repository

import com.hkgroups.agecalculator.data.model.Compatibility
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.data.remote.ZodiacApiService
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZodiacRepository @Inject constructor(
    private val apiService: ZodiacApiService
) {

    private val zodiacCache = mutableMapOf<String, ZodiacSign>()

    suspend fun getZodiacSign(name: String): ZodiacSign? {
        // If the sign is already in our cache, return it to save a network call
        if (zodiacCache.containsKey(name)) {
            return zodiacCache[name]
        }

        // Otherwise, fetch from the network
        return try {
            val dto = apiService.getZodiacSignDetails(name)
            // Convert the network DTO to our app's internal model
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
            // Save to cache before returning
            zodiacCache[name] = sign
            sign
        } catch (e: Exception) {
            // If the network call fails, return null
            null
        }
    }

    fun getDailyTip(zodiacName: String): String {
        val genericTips = listOf(
            "Today is a great day for bold action and starting new projects.",
            "Focus on stability and comfort. Enjoy the simple pleasures.",
            "Communication is key. Express your ideas clearly and listen to others.",
            "Trust your intuition. It's a good day for self-care and reflection.",
            "Your natural creativity is shining. Don't be afraid to take the spotlight."
        )
        // In a real app, this would be more complex or fetched from a server.
        // For now, we return a random tip.
        return "For $zodiacName: ${genericTips.random()}"
    }

    fun getDailyHoroscope(signName: String): String {
        // In a real app, this content would be updated daily, likely from a server.
        // For now, we'll provide a more detailed, static horoscope.
        return when (signName) {
            "Aries" -> "Your dynamic energy is at a peak. It's a great day to tackle a challenging project you've been putting off. Your leadership will shine."
            "Taurus" -> "Focus on financial matters today. An opportunity for a smart investment or saving strategy may present itself. Trust your practical instincts."
            "Gemini" -> "Your social life is buzzing. Reconnect with old friends or make new ones. A conversation today could lead to an exciting new idea."
            // ... add similar detailed horoscopes for all 12 signs
            else -> "A day of reflection and calm is ahead. Take time for yourself to recharge and plan your next moves."
        }
    }

    fun getHistoricalEvents(): List<HistoricalEvent> {
        // In a real app, this data would come from a larger database or an API.
        return listOf(
            HistoricalEvent(
                LocalDate.of(1990, 8, 2),
                "Invasion of Kuwait",
                "The Gulf War begins as Iraq invades its neighbor, Kuwait."
            ),
            HistoricalEvent(
                LocalDate.of(1991, 12, 26),
                "End of the Soviet Union",
                "The Soviet Union is officially dissolved, ending the Cold War."
            ),
            HistoricalEvent(
                LocalDate.of(1994, 5, 6),
                "Channel Tunnel Opens",
                "The Channel Tunnel opens, connecting the UK and France by rail for the first time."
            ),
            HistoricalEvent(
                LocalDate.of(1997, 7, 1),
                "Hong Kong Handover",
                "The United Kingdom hands over sovereignty of Hong Kong to China."
            ),
            HistoricalEvent(
                LocalDate.of(2001, 9, 11),
                "9/11 Attacks",
                "Coordinated terrorist attacks occur in the United States, changing global politics."
            ),
            HistoricalEvent(
                LocalDate.of(2007, 1, 9),
                "First iPhone Revealed",
                "Steve Jobs unveils the first iPhone, revolutionizing the mobile phone industry."
            ),
            HistoricalEvent(
                LocalDate.of(2008, 11, 4),
                "First Black US President",
                "Barack Obama is elected as the first African American President of the United States."
            ),
            HistoricalEvent(
                LocalDate.of(2015, 12, 12),
                "Paris Agreement",
                "196 countries adopt a landmark agreement to combat climate change."
            ),
            HistoricalEvent(
                LocalDate.of(2020, 3, 11),
                "COVID-19 Pandemic Declared",
                "The World Health Organization declares the COVID-19 outbreak a global pandemic."
            )
        )
    }
}