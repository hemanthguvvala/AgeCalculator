package com.hkgroups.agecalculator.util

/**
 * Represents a planet with its orbital period in Earth days.
 * @param name The name of the planet
 * @param orbitalPeriodInEarthDays The number of Earth days for one orbit around the sun
 * @param color The hex color associated with the planet
 */
data class PlanetEnum(
    val name: String,
    val orbitalPeriodInEarthDays: Double,
    val color: Long
)

object CosmicUtils {

    // Define planets with their orbital periods (in Earth days)
    val planets = listOf(
        PlanetEnum("Mercury", 87.97, 0xFFB0B0B0),
        PlanetEnum("Venus", 224.70, 0xFFFFC649),
        PlanetEnum("Mars", 686.98, 0xFFE27B58),
        PlanetEnum("Jupiter", 4332.59, 0xFFD8A87B),
        PlanetEnum("Saturn", 10759.22, 0xFFFAD5A5),
        PlanetEnum("Uranus", 30688.5, 0xFF4FD0E7),
        PlanetEnum("Neptune", 60182.0, 0xFF4C6EF5)
    )

    /**
     * Calculates the Chinese Zodiac sign based on the birth year.
     * @param year The birth year
     * @return The Chinese Zodiac sign name
     */
    fun getChineseZodiac(year: Int): String {
        val zodiacAnimals = listOf(
            "Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake",
            "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig"
        )
        
        // Chinese zodiac starts from 1900 being the year of Rat
        // Calculate the index based on the year
        val baseYear = 1900
        val index = (year - baseYear) % 12
        
        return zodiacAnimals[index]
    }

    /**
     * Calculates ages on different planets based on Earth age in days.
     * @param earthAgeInDays The age in Earth days
     * @return List of pairs containing planet name and formatted age
     */
    fun calculatePlanetaryAges(earthAgeInDays: Long): List<Pair<String, String>> {
        return planets.map { planet ->
            val planetAge = earthAgeInDays / planet.orbitalPeriodInEarthDays
            val formattedAge = String.format("%.1f", planetAge)
            Pair(planet.name, formattedAge)
        }
    }

    /**
     * Returns an interesting trivia fact about a birth year.
     * @param year The birth year (1950-2025)
     * @return A trivia fact about that year
     */
    fun getBirthYearTrivia(year: Int): String {
        val triviaMap = mapOf(
            1950 to "The Korean War began",
            1951 to "Color TV was introduced in the USA",
            1952 to "The first hydrogen bomb was tested",
            1953 to "DNA's double helix structure was discovered",
            1954 to "The first successful organ transplant was performed",
            1955 to "Disneyland opened in California",
            1956 to "Elvis Presley released 'Heartbreak Hotel'",
            1957 to "The Space Age began with Sputnik 1",
            1958 to "NASA was established",
            1959 to "The microchip was invented",
            1960 to "The laser was invented",
            1961 to "Yuri Gagarin became the first human in space",
            1962 to "The Cuban Missile Crisis occurred",
            1963 to "Martin Luther King Jr. gave his 'I Have a Dream' speech",
            1964 to "The Beatles appeared on The Ed Sullivan Show",
            1965 to "The first spacewalk was performed",
            1966 to "The first Star Trek episode aired",
            1967 to "The first human heart transplant was performed",
            1968 to "Apollo 8 orbited the Moon",
            1969 to "Humans first landed on the Moon",
            1970 to "The first Earth Day was celebrated",
            1971 to "The microprocessor was invented",
            1972 to "The first video game console (Magnavox Odyssey) was released",
            1973 to "The first mobile phone call was made",
            1974 to "The Rubik's Cube was invented",
            1975 to "Microsoft was founded by Bill Gates and Paul Allen",
            1976 to "Apple Computer was founded",
            1977 to "Star Wars revolutionized cinema",
            1978 to "The first test-tube baby was born",
            1979 to "Sony introduced the Walkman",
            1980 to "CNN launched 24-hour news coverage",
            1981 to "IBM released the first personal computer",
            1982 to "The first artificial heart was implanted",
            1983 to "The Internet officially adopted TCP/IP",
            1984 to "Apple launched the Macintosh computer",
            1985 to "Windows 1.0 was released",
            1986 to "The Mir space station was launched",
            1987 to "The world population reached 5 billion",
            1988 to "The first transatlantic fiber optic cable was laid",
            1989 to "The World Wide Web was invented by Tim Berners-Lee",
            1990 to "The Hubble Space Telescope was launched",
            1991 to "The first web page went live",
            1992 to "The first SMS text message was sent",
            1993 to "The web browser Mosaic was released",
            1994 to "Amazon.com was founded",
            1995 to "eBay and Yahoo were founded",
            1996 to "The first DVD players were released",
            1997 to "Google.com was registered as a domain",
            1998 to "Google Inc. was officially founded",
            1999 to "The Euro currency was introduced",
            2000 to "The Y2K bug fears proved largely unfounded",
            2001 to "Wikipedia was launched",
            2002 to "The first camera phone was released",
            2003 to "The Human Genome Project was completed",
            2004 to "Facebook was launched",
            2005 to "YouTube was founded",
            2006 to "Twitter was launched",
            2007 to "The iPhone was introduced",
            2008 to "Bitcoin was invented",
            2009 to "WhatsApp was founded",
            2010 to "Instagram was launched",
            2011 to "Snapchat was launched",
            2012 to "The Higgs boson particle was discovered",
            2013 to "Edward Snowden revealed NSA surveillance",
            2014 to "The first commercial drone regulations were introduced",
            2015 to "The Paris Climate Agreement was signed",
            2016 to "AlphaGo beat the world champion Go player",
            2017 to "CRISPR gene editing made major advances",
            2018 to "SpaceX launched the Falcon Heavy rocket",
            2019 to "The first image of a black hole was captured",
            2020 to "The COVID-19 pandemic changed the world",
            2021 to "James Webb Space Telescope was launched",
            2022 to "DALL-E 2 revolutionized AI image generation",
            2023 to "ChatGPT reached 100 million users",
            2024 to "AI technologies transformed global industries",
            2025 to "Advanced AI assistants became mainstream"
        )
        
        return triviaMap[year] ?: "A year filled with unique moments in history"
    }
    
    /**
     * Get Chinese Zodiac emoji based on animal name.
     * @param zodiacName The Chinese zodiac animal name
     * @return The corresponding emoji
     */
    fun getChineseZodiacEmoji(zodiacName: String): String {
        return when (zodiacName) {
            "Rat" -> "🐀"
            "Ox" -> "🐂"
            "Tiger" -> "🐅"
            "Rabbit" -> "🐇"
            "Dragon" -> "🐉"
            "Snake" -> "🐍"
            "Horse" -> "🐎"
            "Goat" -> "🐐"
            "Monkey" -> "🐒"
            "Rooster" -> "🐓"
            "Dog" -> "🐕"
            "Pig" -> "🐖"
            else -> "✨"
        }
    }
}
