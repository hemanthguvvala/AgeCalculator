package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.util.CosmicUtils

/**
 * Displays a horizontal scrolling row of planetary ages.
 * Each planet shows its name and the user's age on that planet.
 */
@Composable
fun PlanetaryAgeRow(
    planetaryAges: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "🌌 Your Age Across the Galaxy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(planetaryAges) { (planetName, age) ->
                val planet = CosmicUtils.planets.find { it.name == planetName }
                planet?.let {
                    PlanetCard(
                        planetName = planetName,
                        age = age,
                        color = Color(it.color)
                    )
                }
            }
        }
    }
}

/**
 * Individual planet card displaying planet name and age.
 */
@Composable
private fun PlanetCard(
    planetName: String,
    age: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Planet icon circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getPlanetEmoji(planetName),
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Planet name
                Text(
                    text = planetName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Age on planet
                Text(
                    text = "$age yrs",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}

/**
 * Returns an emoji representing the planet.
 */
private fun getPlanetEmoji(planetName: String): String {
    return when (planetName) {
        "Mercury" -> "☿"
        "Venus" -> "♀"
        "Mars" -> "♂"
        "Jupiter" -> "♃"
        "Saturn" -> "♄"
        "Uranus" -> "♅"
        "Neptune" -> "♆"
        else -> "🪐"
    }
}
