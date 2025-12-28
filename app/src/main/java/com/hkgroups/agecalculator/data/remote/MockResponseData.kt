package com.hkgroups.agecalculator.data.remote

import kotlin.random.Random

/**
 * Mock data source providing JSON responses for the fake API.
 * Simulates a real API backend for testing and development.
 */
object MockResponseData {

    /**
     * List of all 12 zodiac signs with simplified data.
     * Used for /signs endpoint response.
     * 
     * Note: Uses snake_case to match ZodiacSignDto's @SerializedName annotations
     */
    const val ALL_SIGNS_JSON = """
        [
            {
                "name": "Aries",
                "symbol": "♈",
                "date_range": "Mar 21 - Apr 19",
                "personality_summary": "Bold, ambitious, and confident. Aries are natural-born leaders.",
                "ruling_planet": "Mars",
                "element": "Fire",
                "strengths": ["Courageous", "Determined", "Confident", "Enthusiastic", "Optimistic"],
                "weaknesses": ["Impatient", "Moody", "Short-tempered", "Impulsive", "Aggressive"],
                "compatibilities": [
                    {"sign_name": "Leo", "rating": 5, "description": "Excellent match! Both fire signs with strong personalities."},
                    {"sign_name": "Sagittarius", "rating": 5, "description": "Great compatibility with shared love for adventure."},
                    {"sign_name": "Gemini", "rating": 4, "description": "Good match with exciting dynamics."}
                ]
            },
            {
                "name": "Taurus",
                "symbol": "♉",
                "date_range": "Apr 20 - May 20",
                "personality_summary": "Reliable, patient, and devoted. Taurus values stability and comfort.",
                "ruling_planet": "Venus",
                "element": "Earth",
                "strengths": ["Reliable", "Patient", "Practical", "Devoted", "Responsible"],
                "weaknesses": ["Stubborn", "Possessive", "Uncompromising"],
                "compatibilities": [
                    {"sign_name": "Virgo", "rating": 5, "description": "Perfect earth sign pairing."},
                    {"sign_name": "Capricorn", "rating": 5, "description": "Excellent compatibility and shared values."},
                    {"sign_name": "Cancer", "rating": 4, "description": "Strong emotional connection."}
                ]
            },
            {
                "name": "Gemini",
                "symbol": "♊",
                "date_range": "May 21 - Jun 20",
                "personality_summary": "Adaptable, outgoing, and intelligent. Gemini loves communication.",
                "ruling_planet": "Mercury",
                "element": "Air",
                "strengths": ["Gentle", "Affectionate", "Curious", "Adaptable", "Quick learner"],
                "weaknesses": ["Nervous", "Inconsistent", "Indecisive"],
                "compatibilities": [
                    {"sign_name": "Libra", "rating": 5, "description": "Perfect air sign match."},
                    {"sign_name": "Aquarius", "rating": 5, "description": "Intellectually stimulating partnership."},
                    {"sign_name": "Aries", "rating": 4, "description": "Exciting and dynamic relationship."}
                ]
            },
            {
                "name": "Cancer",
                "symbol": "♋",
                "date_range": "Jun 21 - Jul 22",
                "personality_summary": "Intuitive, emotional, and protective. Cancer is deeply caring.",
                "ruling_planet": "Moon",
                "element": "Water",
                "strengths": ["Tenacious", "Highly imaginative", "Loyal", "Emotional", "Sympathetic"],
                "weaknesses": ["Moody", "Pessimistic", "Suspicious", "Manipulative", "Insecure"],
                "compatibilities": [
                    {"sign_name": "Scorpio", "rating": 5, "description": "Deep emotional connection."},
                    {"sign_name": "Pisces", "rating": 5, "description": "Beautiful water sign pairing."},
                    {"sign_name": "Taurus", "rating": 4, "description": "Stable and nurturing relationship."}
                ]
            },
            {
                "name": "Leo",
                "symbol": "♌",
                "date_range": "Jul 23 - Aug 22",
                "personality_summary": "Creative, passionate, and generous. Leo loves being in the spotlight.",
                "ruling_planet": "Sun",
                "element": "Fire",
                "strengths": ["Creative", "Passionate", "Generous", "Warm-hearted", "Cheerful"],
                "weaknesses": ["Arrogant", "Stubborn", "Self-centered", "Inflexible", "Lazy"],
                "compatibilities": [
                    {"sign_name": "Aries", "rating": 5, "description": "Dynamic fire sign partnership."},
                    {"sign_name": "Sagittarius", "rating": 5, "description": "Fun-loving and adventurous match."},
                    {"sign_name": "Gemini", "rating": 4, "description": "Playful and exciting relationship."}
                ]
            },
            {
                "name": "Virgo",
                "symbol": "♍",
                "date_range": "Aug 23 - Sep 22",
                "personality_summary": "Analytical, practical, and hardworking. Virgo seeks perfection.",
                "ruling_planet": "Mercury",
                "element": "Earth",
                "strengths": ["Loyal", "Analytical", "Kind", "Hardworking", "Practical"],
                "weaknesses": ["Shyness", "Worry", "Overly critical", "Perfectionist"],
                "compatibilities": [
                    {"sign_name": "Taurus", "rating": 5, "description": "Stable earth sign match."},
                    {"sign_name": "Capricorn", "rating": 5, "description": "Goal-oriented partnership."},
                    {"sign_name": "Cancer", "rating": 4, "description": "Nurturing and supportive relationship."}
                ]
            },
            {
                "name": "Libra",
                "symbol": "♎",
                "date_range": "Sep 23 - Oct 22",
                "personality_summary": "Diplomatic, fair-minded, and social. Libra values harmony and balance.",
                "ruling_planet": "Venus",
                "element": "Air",
                "strengths": ["Cooperative", "Diplomatic", "Gracious", "Fair-minded", "Social"],
                "weaknesses": ["Indecisive", "Avoids confrontations", "Self-pity"],
                "compatibilities": [
                    {"sign_name": "Gemini", "rating": 5, "description": "Intellectual air sign connection."},
                    {"sign_name": "Aquarius", "rating": 5, "description": "Harmonious and balanced match."},
                    {"sign_name": "Leo", "rating": 4, "description": "Complementary opposites attract."}
                ]
            },
            {
                "name": "Scorpio",
                "symbol": "♏",
                "date_range": "Oct 23 - Nov 21",
                "personality_summary": "Passionate, resourceful, and brave. Scorpio is intensely focused.",
                "ruling_planet": "Pluto",
                "element": "Water",
                "strengths": ["Resourceful", "Brave", "Passionate", "Stubborn", "True friend"],
                "weaknesses": ["Distrusting", "Jealous", "Secretive", "Violent"],
                "compatibilities": [
                    {"sign_name": "Cancer", "rating": 5, "description": "Deeply emotional water sign bond."},
                    {"sign_name": "Pisces", "rating": 5, "description": "Intuitive and passionate match."},
                    {"sign_name": "Virgo", "rating": 4, "description": "Complementary strengths."}
                ]
            },
            {
                "name": "Sagittarius",
                "symbol": "♐",
                "date_range": "Nov 22 - Dec 21",
                "personality_summary": "Optimistic, adventurous, and philosophical. Sagittarius loves freedom.",
                "ruling_planet": "Jupiter",
                "element": "Fire",
                "strengths": ["Generous", "Idealistic", "Great sense of humor"],
                "weaknesses": ["Promises more than can deliver", "Impatient", "Tactless"],
                "compatibilities": [
                    {"sign_name": "Aries", "rating": 5, "description": "Adventurous fire sign pairing."},
                    {"sign_name": "Leo", "rating": 5, "description": "Optimistic and fun-loving match."},
                    {"sign_name": "Aquarius", "rating": 4, "description": "Freedom-loving partnership."}
                ]
            },
            {
                "name": "Capricorn",
                "symbol": "♑",
                "date_range": "Dec 22 - Jan 19",
                "personality_summary": "Ambitious, disciplined, and responsible. Capricorn is goal-oriented.",
                "ruling_planet": "Saturn",
                "element": "Earth",
                "strengths": ["Responsible", "Disciplined", "Self-control", "Good managers"],
                "weaknesses": ["Know-it-all", "Unforgiving", "Condescending", "Pessimistic"],
                "compatibilities": [
                    {"sign_name": "Taurus", "rating": 5, "description": "Stable and practical earth signs."},
                    {"sign_name": "Virgo", "rating": 5, "description": "Hardworking and dedicated match."},
                    {"sign_name": "Scorpio", "rating": 4, "description": "Ambitious partnership."}
                ]
            },
            {
                "name": "Aquarius",
                "symbol": "♒",
                "date_range": "Jan 20 - Feb 18",
                "personality_summary": "Progressive, independent, and humanitarian. Aquarius is innovative.",
                "ruling_planet": "Uranus",
                "element": "Air",
                "strengths": ["Progressive", "Original", "Independent", "Humanitarian"],
                "weaknesses": ["Runs from emotional expression", "Temperamental", "Uncompromising"],
                "compatibilities": [
                    {"sign_name": "Gemini", "rating": 5, "description": "Intellectually exciting air sign match."},
                    {"sign_name": "Libra", "rating": 5, "description": "Social and harmonious pairing."},
                    {"sign_name": "Sagittarius", "rating": 4, "description": "Independent and free-spirited."}
                ]
            },
            {
                "name": "Pisces",
                "symbol": "♓",
                "date_range": "Feb 19 - Mar 20",
                "personality_summary": "Compassionate, artistic, and intuitive. Pisces is deeply empathetic.",
                "ruling_planet": "Neptune",
                "element": "Water",
                "strengths": ["Compassionate", "Artistic", "Intuitive", "Gentle", "Wise"],
                "weaknesses": ["Fearful", "Overly trusting", "Sad", "Escape reality"],
                "compatibilities": [
                    {"sign_name": "Cancer", "rating": 5, "description": "Deeply emotional water sign connection."},
                    {"sign_name": "Scorpio", "rating": 5, "description": "Intuitive and passionate match."},
                    {"sign_name": "Taurus", "rating": 4, "description": "Grounding and nurturing relationship."}
                ]
            }
        ]
    """

    /**
     * Randomized horoscopes for variety in responses.
     * Simulates daily updates in the API.
     */
    private val horoscopeVariations = listOf(
        "Today brings exciting opportunities for growth and self-discovery.",
        "Your creativity will shine bright today. Trust your instincts.",
        "A great day for meaningful connections and deep conversations.",
        "Focus on your goals today. Success is within reach.",
        "Take time for self-care and reflection. Balance is key.",
        "Unexpected surprises await. Stay open to new possibilities.",
        "Your hard work is about to pay off. Keep pushing forward.",
        "Communication is highlighted today. Express yourself clearly.",
        "Trust your intuition today. It won't lead you astray.",
        "A perfect day for collaboration and teamwork.",
        "Embrace change today. New beginnings are on the horizon.",
        "Your patience and persistence will be rewarded soon."
    )

    /**
     * Returns a JSON string for a specific zodiac sign with randomized horoscope.
     * Used for /signs/{name} endpoint response.
     *
     * @param name The name of the zodiac sign
     * @return JSON string representing the sign details
     */
    fun singleSignJson(name: String): String {
        // Get random horoscope to simulate daily updates
        val randomHoroscope = horoscopeVariations.random()
        
        return when (name.lowercase()) {
            "aries" -> """
                {
                    "name": "Aries",
                    "symbol": "♈",
                    "dateRange": "Mar 21 - Apr 19",
                    "personality": "Bold, ambitious, and confident. Aries are natural-born leaders who aren't afraid to take charge. They possess incredible energy and enthusiasm that inspires others.",
                    "rulingPlanet": "Mars",
                    "element": "Fire",
                    "strengths": ["Courageous", "Determined", "Confident", "Enthusiastic", "Optimistic", "Honest", "Passionate"],
                    "weaknesses": ["Impatient", "Moody", "Short-tempered", "Impulsive", "Aggressive"],
                    "compatibilities": [
                        {"signName": "Leo", "rating": 5, "description": "Excellent match! Both fire signs with strong personalities and mutual respect."},
                        {"signName": "Sagittarius", "rating": 5, "description": "Great compatibility with shared love for adventure and new experiences."},
                        {"signName": "Gemini", "rating": 4, "description": "Good match with exciting dynamics and intellectual stimulation."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "taurus" -> """
                {
                    "name": "Taurus",
                    "symbol": "♉",
                    "dateRange": "Apr 20 - May 20",
                    "personality": "Reliable, patient, and devoted. Taurus values stability, comfort, and the finer things in life. They are grounded and practical.",
                    "rulingPlanet": "Venus",
                    "element": "Earth",
                    "strengths": ["Reliable", "Patient", "Practical", "Devoted", "Responsible", "Stable"],
                    "weaknesses": ["Stubborn", "Possessive", "Uncompromising"],
                    "compatibilities": [
                        {"signName": "Virgo", "rating": 5, "description": "Perfect earth sign pairing with shared values."},
                        {"signName": "Capricorn", "rating": 5, "description": "Excellent compatibility and mutual understanding."},
                        {"signName": "Cancer", "rating": 4, "description": "Strong emotional connection and loyalty."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "gemini" -> """
                {
                    "name": "Gemini",
                    "symbol": "♊",
                    "dateRange": "May 21 - Jun 20",
                    "personality": "Adaptable, outgoing, and intelligent. Gemini loves communication, learning, and social connections. They are versatile and curious.",
                    "rulingPlanet": "Mercury",
                    "element": "Air",
                    "strengths": ["Gentle", "Affectionate", "Curious", "Adaptable", "Quick learner", "Witty"],
                    "weaknesses": ["Nervous", "Inconsistent", "Indecisive"],
                    "compatibilities": [
                        {"signName": "Libra", "rating": 5, "description": "Perfect air sign match with intellectual harmony."},
                        {"signName": "Aquarius", "rating": 5, "description": "Intellectually stimulating and innovative partnership."},
                        {"signName": "Aries", "rating": 4, "description": "Exciting and dynamic relationship full of energy."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "cancer" -> """
                {
                    "name": "Cancer",
                    "symbol": "♋",
                    "dateRange": "Jun 21 - Jul 22",
                    "personality": "Intuitive, emotional, and protective. Cancer is deeply caring and values family and home. They are nurturing and empathetic.",
                    "rulingPlanet": "Moon",
                    "element": "Water",
                    "strengths": ["Tenacious", "Highly imaginative", "Loyal", "Emotional", "Sympathetic", "Persuasive"],
                    "weaknesses": ["Moody", "Pessimistic", "Suspicious", "Manipulative", "Insecure"],
                    "compatibilities": [
                        {"signName": "Scorpio", "rating": 5, "description": "Deep emotional connection and mutual understanding."},
                        {"signName": "Pisces", "rating": 5, "description": "Beautiful water sign pairing with emotional depth."},
                        {"signName": "Taurus", "rating": 4, "description": "Stable and nurturing relationship."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "leo" -> """
                {
                    "name": "Leo",
                    "symbol": "♌",
                    "dateRange": "Jul 23 - Aug 22",
                    "personality": "Creative, passionate, and generous. Leo loves being in the spotlight and inspiring others. They are natural performers with big hearts.",
                    "rulingPlanet": "Sun",
                    "element": "Fire",
                    "strengths": ["Creative", "Passionate", "Generous", "Warm-hearted", "Cheerful", "Humorous"],
                    "weaknesses": ["Arrogant", "Stubborn", "Self-centered", "Inflexible", "Lazy"],
                    "compatibilities": [
                        {"signName": "Aries", "rating": 5, "description": "Dynamic fire sign partnership with mutual admiration."},
                        {"signName": "Sagittarius", "rating": 5, "description": "Fun-loving and adventurous match."},
                        {"signName": "Gemini", "rating": 4, "description": "Playful and exciting relationship."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "virgo" -> """
                {
                    "name": "Virgo",
                    "symbol": "♍",
                    "dateRange": "Aug 23 - Sep 22",
                    "personality": "Analytical, practical, and hardworking. Virgo seeks perfection and pays attention to every detail. They are helpful and organized.",
                    "rulingPlanet": "Mercury",
                    "element": "Earth",
                    "strengths": ["Loyal", "Analytical", "Kind", "Hardworking", "Practical"],
                    "weaknesses": ["Shyness", "Worry", "Overly critical", "Perfectionist"],
                    "compatibilities": [
                        {"signName": "Taurus", "rating": 5, "description": "Stable earth sign match with shared values."},
                        {"signName": "Capricorn", "rating": 5, "description": "Goal-oriented and practical partnership."},
                        {"signName": "Cancer", "rating": 4, "description": "Nurturing and supportive relationship."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "libra" -> """
                {
                    "name": "Libra",
                    "symbol": "♎",
                    "dateRange": "Sep 23 - Oct 22",
                    "personality": "Diplomatic, fair-minded, and social. Libra values harmony, balance, and beauty. They are peacemakers who seek justice.",
                    "rulingPlanet": "Venus",
                    "element": "Air",
                    "strengths": ["Cooperative", "Diplomatic", "Gracious", "Fair-minded", "Social"],
                    "weaknesses": ["Indecisive", "Avoids confrontations", "Self-pity"],
                    "compatibilities": [
                        {"signName": "Gemini", "rating": 5, "description": "Intellectual air sign connection with great communication."},
                        {"signName": "Aquarius", "rating": 5, "description": "Harmonious and balanced match."},
                        {"signName": "Leo", "rating": 4, "description": "Complementary opposites attract."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "scorpio" -> """
                {
                    "name": "Scorpio",
                    "symbol": "♏",
                    "dateRange": "Oct 23 - Nov 21",
                    "personality": "Passionate, resourceful, and brave. Scorpio is intensely focused and mysterious. They possess powerful emotions and determination.",
                    "rulingPlanet": "Pluto",
                    "element": "Water",
                    "strengths": ["Resourceful", "Brave", "Passionate", "Stubborn", "True friend"],
                    "weaknesses": ["Distrusting", "Jealous", "Secretive", "Violent"],
                    "compatibilities": [
                        {"signName": "Cancer", "rating": 5, "description": "Deeply emotional water sign bond."},
                        {"signName": "Pisces", "rating": 5, "description": "Intuitive and passionate match."},
                        {"signName": "Virgo", "rating": 4, "description": "Complementary strengths and loyalty."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "sagittarius" -> """
                {
                    "name": "Sagittarius",
                    "symbol": "♐",
                    "dateRange": "Nov 22 - Dec 21",
                    "personality": "Optimistic, adventurous, and philosophical. Sagittarius loves freedom, travel, and exploring new ideas. They are enthusiastic seekers.",
                    "rulingPlanet": "Jupiter",
                    "element": "Fire",
                    "strengths": ["Generous", "Idealistic", "Great sense of humor"],
                    "weaknesses": ["Promises more than can deliver", "Impatient", "Tactless"],
                    "compatibilities": [
                        {"signName": "Aries", "rating": 5, "description": "Adventurous fire sign pairing with endless energy."},
                        {"signName": "Leo", "rating": 5, "description": "Optimistic and fun-loving match."},
                        {"signName": "Aquarius", "rating": 4, "description": "Freedom-loving partnership."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "capricorn" -> """
                {
                    "name": "Capricorn",
                    "symbol": "♑",
                    "dateRange": "Dec 22 - Jan 19",
                    "personality": "Ambitious, disciplined, and responsible. Capricorn is goal-oriented and values tradition. They are patient climbers of success.",
                    "rulingPlanet": "Saturn",
                    "element": "Earth",
                    "strengths": ["Responsible", "Disciplined", "Self-control", "Good managers"],
                    "weaknesses": ["Know-it-all", "Unforgiving", "Condescending", "Pessimistic"],
                    "compatibilities": [
                        {"signName": "Taurus", "rating": 5, "description": "Stable and practical earth signs."},
                        {"signName": "Virgo", "rating": 5, "description": "Hardworking and dedicated match."},
                        {"signName": "Scorpio", "rating": 4, "description": "Ambitious and determined partnership."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "aquarius" -> """
                {
                    "name": "Aquarius",
                    "symbol": "♒",
                    "dateRange": "Jan 20 - Feb 18",
                    "personality": "Progressive, independent, and humanitarian. Aquarius is innovative and values intellectual connections. They are unique visionaries.",
                    "rulingPlanet": "Uranus",
                    "element": "Air",
                    "strengths": ["Progressive", "Original", "Independent", "Humanitarian"],
                    "weaknesses": ["Runs from emotional expression", "Temperamental", "Uncompromising"],
                    "compatibilities": [
                        {"signName": "Gemini", "rating": 5, "description": "Intellectually exciting air sign match."},
                        {"signName": "Libra", "rating": 5, "description": "Social and harmonious pairing."},
                        {"signName": "Sagittarius", "rating": 4, "description": "Independent and free-spirited."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            "pisces" -> """
                {
                    "name": "Pisces",
                    "symbol": "♓",
                    "dateRange": "Feb 19 - Mar 20",
                    "personality": "Compassionate, artistic, and intuitive. Pisces is deeply empathetic and imaginative. They are dreamers with profound emotional depth.",
                    "rulingPlanet": "Neptune",
                    "element": "Water",
                    "strengths": ["Compassionate", "Artistic", "Intuitive", "Gentle", "Wise", "Musical"],
                    "weaknesses": ["Fearful", "Overly trusting", "Sad", "Escape reality", "Victim mentality"],
                    "compatibilities": [
                        {"signName": "Cancer", "rating": 5, "description": "Deeply emotional water sign connection."},
                        {"signName": "Scorpio", "rating": 5, "description": "Intuitive and passionate match."},
                        {"signName": "Taurus", "rating": 4, "description": "Grounding and nurturing relationship."}
                    ],
                    "dailyHoroscope": "$randomHoroscope"
                }
            """.trimIndent()
            
            else -> """
                {
                    "error": "Sign not found",
                    "message": "The zodiac sign '$name' was not recognized."
                }
            """.trimIndent()
        }
    }
}
