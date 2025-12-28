package com.hkgroups.agecalculator.data.local

import com.hkgroups.agecalculator.data.model.Compatibility
import com.hkgroups.agecalculator.data.model.HistoricalEvent
import com.hkgroups.agecalculator.data.model.ZodiacSign
import java.time.LocalDate

/**
 * Initial data source for seeding the database.
 * Contains hardcoded zodiac signs and historical events for offline functionality.
 */
object InitialDataSource {

    /**
     * Get initial zodiac signs with complete information.
     * These will be seeded into the database on first app launch.
     */
    fun getZodiacSigns(): List<ZodiacSign> {
        return listOf(
            ZodiacSign(
                name = "Aries",
                symbol = "♈",
                dateRange = "Mar 21 - Apr 19",
                personality = "Bold, ambitious, and energetic. Aries are natural leaders who love challenges and adventures.",
                rulingPlanet = "Mars",
                element = "Fire",
                strengths = listOf("Courageous", "Determined", "Confident", "Enthusiastic", "Optimistic"),
                weaknesses = listOf("Impatient", "Moody", "Short-tempered", "Impulsive", "Aggressive"),
                compatibilities = listOf(
                    Compatibility("Leo", 9, "Excellent match with shared fire energy"),
                    Compatibility("Sagittarius", 9, "Dynamic duo with adventurous spirits"),
                    Compatibility("Gemini", 7, "Exciting but challenging combination")
                )
            ),
            ZodiacSign(
                name = "Taurus",
                symbol = "♉",
                dateRange = "Apr 20 - May 20",
                personality = "Reliable, patient, and devoted. Taurus values stability and enjoys life's pleasures.",
                rulingPlanet = "Venus",
                element = "Earth",
                strengths = listOf("Reliable", "Patient", "Practical", "Devoted", "Responsible"),
                weaknesses = listOf("Stubborn", "Possessive", "Uncompromising"),
                compatibilities = listOf(
                    Compatibility("Virgo", 9, "Perfect earth sign match"),
                    Compatibility("Capricorn", 8, "Stable and supportive relationship"),
                    Compatibility("Cancer", 8, "Emotional connection and loyalty")
                )
            ),
            ZodiacSign(
                name = "Gemini",
                symbol = "♊",
                dateRange = "May 21 - Jun 20",
                personality = "Curious, adaptable, and communicative. Gemini loves learning and socializing.",
                rulingPlanet = "Mercury",
                element = "Air",
                strengths = listOf("Gentle", "Affectionate", "Curious", "Adaptable", "Quick learner"),
                weaknesses = listOf("Nervous", "Inconsistent", "Indecisive"),
                compatibilities = listOf(
                    Compatibility("Libra", 9, "Intellectual and social harmony"),
                    Compatibility("Aquarius", 9, "Meeting of brilliant minds"),
                    Compatibility("Aries", 7, "Exciting but needs balance")
                )
            ),
            ZodiacSign(
                name = "Cancer",
                symbol = "♋",
                dateRange = "Jun 21 - Jul 22",
                personality = "Intuitive, emotional, and protective. Cancer is deeply caring and family-oriented.",
                rulingPlanet = "Moon",
                element = "Water",
                strengths = listOf("Tenacious", "Loyal", "Emotional", "Sympathetic", "Persuasive"),
                weaknesses = listOf("Moody", "Pessimistic", "Suspicious", "Manipulative", "Insecure"),
                compatibilities = listOf(
                    Compatibility("Scorpio", 9, "Deep emotional connection"),
                    Compatibility("Pisces", 9, "Intuitive and compassionate bond"),
                    Compatibility("Taurus", 8, "Nurturing and stable")
                )
            ),
            ZodiacSign(
                name = "Leo",
                symbol = "♌",
                dateRange = "Jul 23 - Aug 22",
                personality = "Creative, passionate, and generous. Leo loves being in the spotlight and inspiring others.",
                rulingPlanet = "Sun",
                element = "Fire",
                strengths = listOf("Creative", "Passionate", "Generous", "Warm-hearted", "Cheerful", "Humorous"),
                weaknesses = listOf("Arrogant", "Stubborn", "Self-centered", "Inflexible"),
                compatibilities = listOf(
                    Compatibility("Aries", 9, "Powerful fire sign combination"),
                    Compatibility("Sagittarius", 9, "Adventure and passion"),
                    Compatibility("Gemini", 7, "Fun but challenging")
                )
            ),
            ZodiacSign(
                name = "Virgo",
                symbol = "♍",
                dateRange = "Aug 23 - Sep 22",
                personality = "Analytical, practical, and hardworking. Virgo pays attention to details and loves helping others.",
                rulingPlanet = "Mercury",
                element = "Earth",
                strengths = listOf("Loyal", "Analytical", "Kind", "Hardworking", "Practical"),
                weaknesses = listOf("Shyness", "Worry", "Overly critical", "Perfectionist"),
                compatibilities = listOf(
                    Compatibility("Taurus", 9, "Grounded and harmonious"),
                    Compatibility("Capricorn", 9, "Shared values and goals"),
                    Compatibility("Cancer", 8, "Caring and supportive")
                )
            ),
            ZodiacSign(
                name = "Libra",
                symbol = "♎",
                dateRange = "Sep 23 - Oct 22",
                personality = "Diplomatic, gracious, and fair-minded. Libra seeks balance and harmony in all things.",
                rulingPlanet = "Venus",
                element = "Air",
                strengths = listOf("Cooperative", "Diplomatic", "Gracious", "Fair-minded", "Social"),
                weaknesses = listOf("Indecisive", "Avoids confrontations", "Self-pity"),
                compatibilities = listOf(
                    Compatibility("Gemini", 9, "Intellectual and social connection"),
                    Compatibility("Aquarius", 9, "Shared love for harmony"),
                    Compatibility("Leo", 7, "Attraction but different needs")
                )
            ),
            ZodiacSign(
                name = "Scorpio",
                symbol = "♏",
                dateRange = "Oct 23 - Nov 21",
                personality = "Passionate, resourceful, and brave. Scorpio is intense and deeply emotional.",
                rulingPlanet = "Pluto",
                element = "Water",
                strengths = listOf("Resourceful", "Brave", "Passionate", "Stubborn", "True friend"),
                weaknesses = listOf("Distrusting", "Jealous", "Secretive", "Violent"),
                compatibilities = listOf(
                    Compatibility("Cancer", 9, "Deep emotional understanding"),
                    Compatibility("Pisces", 9, "Intuitive and passionate"),
                    Compatibility("Virgo", 7, "Complementary strengths")
                )
            ),
            ZodiacSign(
                name = "Sagittarius",
                symbol = "♐",
                dateRange = "Nov 22 - Dec 21",
                personality = "Optimistic, adventurous, and philosophical. Sagittarius loves freedom and exploration.",
                rulingPlanet = "Jupiter",
                element = "Fire",
                strengths = listOf("Generous", "Idealistic", "Great sense of humor"),
                weaknesses = listOf("Promises more than can deliver", "Impatient", "Will say anything"),
                compatibilities = listOf(
                    Compatibility("Aries", 9, "Adventurous and energetic"),
                    Compatibility("Leo", 9, "Passionate fire connection"),
                    Compatibility("Aquarius", 8, "Freedom-loving pair")
                )
            ),
            ZodiacSign(
                name = "Capricorn",
                symbol = "♑",
                dateRange = "Dec 22 - Jan 19",
                personality = "Responsible, disciplined, and ambitious. Capricorn is focused on achieving goals.",
                rulingPlanet = "Saturn",
                element = "Earth",
                strengths = listOf("Responsible", "Disciplined", "Self-control", "Good managers"),
                weaknesses = listOf("Know-it-all", "Unforgiving", "Condescending", "Expects the worst"),
                compatibilities = listOf(
                    Compatibility("Taurus", 9, "Stable and committed"),
                    Compatibility("Virgo", 9, "Practical and supportive"),
                    Compatibility("Scorpio", 8, "Ambitious power couple")
                )
            ),
            ZodiacSign(
                name = "Aquarius",
                symbol = "♒",
                dateRange = "Jan 20 - Feb 18",
                personality = "Progressive, independent, and humanitarian. Aquarius thinks outside the box.",
                rulingPlanet = "Uranus",
                element = "Air",
                strengths = listOf("Progressive", "Original", "Independent", "Humanitarian"),
                weaknesses = listOf("Runs from emotional expression", "Temperamental", "Uncompromising"),
                compatibilities = listOf(
                    Compatibility("Gemini", 9, "Intellectual and innovative"),
                    Compatibility("Libra", 9, "Social and idealistic"),
                    Compatibility("Sagittarius", 8, "Free-spirited connection")
                )
            ),
            ZodiacSign(
                name = "Pisces",
                symbol = "♓",
                dateRange = "Feb 19 - Mar 20",
                personality = "Compassionate, artistic, and intuitive. Pisces is deeply empathetic and creative.",
                rulingPlanet = "Neptune",
                element = "Water",
                strengths = listOf("Compassionate", "Artistic", "Intuitive", "Gentle", "Wise", "Musical"),
                weaknesses = listOf("Fearful", "Overly trusting", "Sad", "Desire to escape reality"),
                compatibilities = listOf(
                    Compatibility("Cancer", 9, "Emotional and nurturing"),
                    Compatibility("Scorpio", 9, "Deep spiritual connection"),
                    Compatibility("Taurus", 8, "Grounding and romance")
                )
            )
        )
    }

    /**
     * Get initial historical events for the database.
     * These are major world events from 1990-2020.
     */
    fun getHistoricalEvents(): List<HistoricalEvent> {
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
