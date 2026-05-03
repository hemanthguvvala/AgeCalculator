package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.GlassCardWithGlow
import com.hkgroups.agecalculator.ui.screen.components.ScoreRing
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.screen.components.pressableScale
import com.hkgroups.agecalculator.ui.screen.components.staggeredEntrance
import com.hkgroups.agecalculator.ui.screen.components.tiltable3D
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityListScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allSigns by viewModel.zodiacSignsState.collectAsState()
    val userSign = uiState.zodiacSign

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        StarryBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                CosmicTopBar(
                    title = if (userSign != null) "${userSign.name} matches" else "Compatibility",
                    subtitle = if (userSign != null) "How you align with the other 11 signs" else null,
                    onBack = { navController.popBackStack() }
                )

                when {
                    userSign == null -> EmptyCompatState(
                        title = "No sign selected yet",
                        message = "Set your birthday on the dashboard to see your cosmic matches."
                    )
                    allSigns.isEmpty() -> EmptyCompatState(
                        title = "Aligning the cosmos...",
                        message = "Loading zodiac data."
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val partners = allSigns.filter { it.name != userSign.name }
                        itemsIndexed(partners) { index, partner ->
                            CompatibilityRowCard(
                                userSign = userSign,
                                partnerSign = partner,
                                indexHint = index,
                                onClick = {
                                    navController.navigate(
                                        com.hkgroups.agecalculator.ui.navigation.Screen
                                            .Compatibility
                                            .createRoute(userSign.name, partner.name)
                                    )
                                }
                            )
                        }
                        item { Spacer(Modifier.height(160.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCompatState(title: String, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CompatibilityRowCard(
    userSign: ZodiacSign,
    partnerSign: ZodiacSign,
    indexHint: Int = 0,
    onClick: () -> Unit = {}
) {
    val info = userSign.compatibilities.find { it.signName == partnerSign.name }
    val rating10 = (info?.rating ?: 1).coerceIn(0, 10)
    val percent = rating10 * 10
    val accent = when {
        percent >= 75 -> SaturnGold
        percent >= 55 -> PrimaryNeon
        else -> PurpleAccent
    }
    val verdict = when {
        percent >= 90 -> "Cosmic soulmates"
        percent >= 75 -> "Strong alignment"
        percent >= 55 -> "Worth exploring"
        else -> "Different orbits"
    }

    GlassCardWithGlow(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(indexHint = indexHint)
            .tiltable3D(maxAngle = 5f, onTap = onClick),
        glowColor = accent,
        glowAlpha = 0.35f,
        elevation = 16.dp,
        shape = RoundedCornerShape(22.dp)
    ) {
        // Three-zone layout: glyph badge · partner identity · score ring.
        // The ring replaces the star string — ratings now feel measured, not toy.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZodiacBadge(symbol = partnerSign.symbol, name = partnerSign.name, accent = accent)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = verdict,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = info?.description?.take(80)
                        ?: elementPairLine(userSign.element, partnerSign.element),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(10.dp))
            ScoreRing(percent = percent, accent = accent, size = 48.dp)
        }
    }
}

/**
 * One-line description based on element pairing — used when seed data has no
 * explicit compatibility entry. Keeps every list row feeling distinct rather
 * than repeating the same fallback for half the signs.
 */
private fun elementPairLine(yourElement: String, partnerElement: String): String {
    val pair = setOf(yourElement, partnerElement)
    return when {
        pair == setOf("Fire") -> "Two fires — high energy, big plans, watch the burnout."
        pair == setOf("Earth") -> "Twin pragmatism. Stable, dependable, occasionally too still."
        pair == setOf("Air") -> "Endless conversation. Ideas spiral together easily."
        pair == setOf("Water") -> "Emotional resonance. You read each other before words arrive."
        pair == setOf("Fire", "Air") -> "Air feeds fire. Their ideas ignite your action."
        pair == setOf("Earth", "Water") -> "Water nourishes earth. Soft meets stable."
        pair == setOf("Fire", "Earth") -> "Earth grounds fire. Ambition becomes durable."
        pair == setOf("Air", "Water") -> "Air over water — atmosphere together."
        pair == setOf("Fire", "Water") -> "Steam dynamics. When in tune, electric."
        pair == setOf("Earth", "Air") -> "Air lifts earth out of routine."
        else -> "A meeting of distinct worlds. The contrast is the point."
    }
}

@Composable
private fun ZodiacBadge(symbol: String, name: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.32f), Color.Transparent)
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                sign = name,
                strokeColor = Color.White,
                accentColor = accent,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
