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
        PlanetEnum("Earth", 365.25, 0xFF4DA8DA),
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

        // 1900 is the Year of the Rat. Use floorMod so years before 1900 map back
        // through the 12-animal cycle correctly instead of throwing on a negative
        // index. (Note: this is a Gregorian-Jan-1 approximation; the actual lunar
        // year starts in late Jan / early Feb.)
        val index = Math.floorMod(year - 1900, 12)
        return zodiacAnimals[index]
    }

    /**
     * One-line description tailored to each Chinese zodiac sign. Used in place of the
     * old hardcoded "Power, luck, and strength..." string that appeared for every sign.
     */
    fun getChineseZodiacDescription(zodiacName: String): String = when (zodiacName) {
        "Rat" -> "Quick-witted, resourceful, and steady — small moves, big wins."
        "Ox" -> "Patient, dependable, and quietly powerful when grounded."
        "Tiger" -> "Bold and magnetic — a born leader when you trust the leap."
        "Rabbit" -> "Gentle, intuitive, and graceful under pressure."
        "Dragon" -> "Power, luck, and strength aligned with the stars."
        "Snake" -> "Wise, perceptive, and a master of strategic patience."
        "Horse" -> "Free-spirited, energetic, and happiest in motion."
        "Goat" -> "Creative, kind, and the calm in someone else's storm."
        "Monkey" -> "Clever, curious, and unstoppable when you're playing."
        "Rooster" -> "Sharp, honest, and quietly proud of the work you do."
        "Dog" -> "Loyal, fair, and the friend everyone secretly wants."
        "Pig" -> "Generous, sincere, and the one who makes everyone feel home."
        else -> "Aligned with the stars in your own quiet way."
    }

    /**
     * Calculates ages on different planets based on Earth age in days.
     * @param earthAgeInDays The age in Earth days
     * @return List of pairs containing planet name and formatted age
     */
    fun calculatePlanetaryAges(earthAgeInDays: Long): List<Pair<String, String>> {
        return planets.map { planet ->
            val planetAge = earthAgeInDays / planet.orbitalPeriodInEarthDays
            // Use 2 decimals for fast planets / young ages so e.g. a 6-month-old
            // doesn't read "0.0" Mars years; 1 decimal is enough beyond ~1 orbit.
            val pattern = if (planetAge < 10.0) "%.2f" else "%.1f"
            val formattedAge = String.format(java.util.Locale.US, pattern, planetAge)
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
        
        triviaMap[year]?.let { return it }

        // Decade-based fallback for years outside the explicit map (pre-1950 / post-2025).
        // Better than the old generic placeholder that used to leak into both the
        // Time Capsule and Did-You-Know cards.
        return when {
            year < 1900 -> "Born in the $year — a world before electricity, radio, and powered flight."
            year in 1900..1909 -> "The dawn of the 20th century — flight, radio, and the modern age were just being born."
            year in 1910..1919 -> "An era reshaped by the First World War and a wave of revolutions across the globe."
            year in 1920..1929 -> "The Roaring Twenties — jazz, cinema, and the first transatlantic flights."
            year in 1930..1939 -> "Defined by the Great Depression and a rapid scramble of scientific discovery."
            year in 1940..1949 -> "The Second World War, the dawn of the atomic age, and the first computers."
            year in 2026..2029 -> "Born into the age of generative AI, lunar return missions, and reusable rockets."
            year in 2030..2099 -> "The mid-21st century — a generation shaped by AI, climate action, and a return to deep space."
            else -> "A year filled with unique moments in history."
        }
    }
    
    /**
     * Get Chinese Zodiac emoji based on animal name.
     * @param zodiacName The Chinese zodiac animal name
     * @return The corresponding emoji
     */
    /**
     * Returns a "Did you know?" cosmos fact, varied daily for a given seed (typically
     * birth-day-of-year + today). Distinct from getBirthYearTrivia so the Time Capsule
     * and Did-You-Know sections don't display identical text.
     */
    fun getCosmicDidYouKnow(seed: Int): String {
        val facts = listOf(
            "On Venus a single day (243 Earth days) is longer than its year (225 Earth days).",
            "A teaspoon of neutron-star material would weigh about 6 billion tons on Earth.",
            "There are more stars in the observable universe than grains of sand on every beach.",
            "Saturn's density is so low it would float in a bathtub — if you found one big enough.",
            "Light from the Sun takes about 8 minutes 20 seconds to reach Earth.",
            "Jupiter's Great Red Spot is a storm that's been raging for at least 350 years.",
            "Mercury's surface temperature swings by more than 600°C between day and night.",
            "Olympus Mons on Mars is roughly three times the height of Mount Everest.",
            "A year on Neptune is 165 Earth years — it has only completed one orbit since its discovery in 1846.",
            "Earth picks up about 100 tonnes of cosmic dust every single day.",
            "The Milky Way and Andromeda are on a collision course — but it's about 4 billion years out.",
            "There's a planet, 55 Cancri e, theorised to be largely made of crystalline carbon — i.e. diamond.",
            "Pluto's largest moon, Charon, is so big the two bodies orbit a point outside Pluto itself.",
            "Black holes don't 'suck' — they have gravity like any other mass; you just can't escape past the event horizon.",
            "Astronauts can grow up to 5 cm taller in microgravity as the spine decompresses."
        )
        return facts[Math.floorMod(seed, facts.size)]
    }

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
