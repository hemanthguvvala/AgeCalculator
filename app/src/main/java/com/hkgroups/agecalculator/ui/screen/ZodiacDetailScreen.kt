package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.GlassCard
import com.hkgroups.agecalculator.ui.screen.components.GradientBorderRing
import com.hkgroups.agecalculator.ui.screen.components.SectionHeader
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.GreenAccent
import com.hkgroups.agecalculator.ui.theme.MarsRed
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZodiacDetailScreen(
    navController: NavController,
    signName: String?,
    viewModel: MainViewModel = hiltViewModel()
) {
    // Reactive: collect the sign list as state so we re-render when the DB finishes
    // loading. The previous one-shot `getSignByName` returned null before signs
    // arrived and the screen rendered blank forever.
    val signs by viewModel.zodiacSignsState.collectAsState()
    val sign = signName?.let { name -> signs.find { it.name == name } }

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
                    title = sign?.name ?: "Zodiac",
                    subtitle = sign?.let { "${it.element} · ${it.dateRange}" },
                    onBack = { navController.popBackStack() }
                )

                if (sign == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        com.hkgroups.agecalculator.ui.screen.components.CosmicLoading()
                    }
                } else {
                    val accent = elementColor(sign.element)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero with element-tinted halo + rotating gradient ring + glyph.
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                accent.copy(alpha = 0.4f),
                                                accent.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            GradientBorderRing(
                                size = 168.dp,
                                strokeWidth = 2.dp,
                                colors = listOf(accent, PrimaryNeon, PurpleAccent, accent)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF15192C),
                                                    Color(0xFF06080F)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                                        sign = sign.name,
                                        strokeColor = Color.White,
                                        accentColor = accent,
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Hero name — display weight at 56sp so the sign feels
                        // monumental on its detail page.
                        Text(
                            text = sign.name,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 60.sp
                            ),
                            color = Color.White
                        )
                        // Date range pill — sits under the name like a badge.
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.18f))
                                .border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = sign.dateRange,
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = sign.personality,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(Modifier.height(28.dp))

                        // Element + Ruling planet stat row — element-tinted accent text
                        // makes water signs feel cool, fire signs feel warm, etc.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatChip(
                                label = "ELEMENT",
                                value = sign.element,
                                accent = accent,
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                label = "RULING PLANET",
                                value = sign.rulingPlanet,
                                accent = SaturnGold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        SectionHeader(
                            eyebrow = "WHAT YOU BRING",
                            title = "Strengths",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        TraitsCard(traits = sign.strengths, accent = GreenAccent)

                        Spacer(Modifier.height(20.dp))

                        SectionHeader(
                            eyebrow = "WATCH OUT FOR",
                            title = "Shadow side",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        TraitsCard(traits = sign.weaknesses, accent = MarsRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TraitsCard(traits: List<String>, accent: Color) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            traits.forEach { trait ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(accent, accent.copy(alpha = 0.4f))
                                )
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = trait,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

private fun elementColor(element: String): Color = when (element.lowercase()) {
    "fire" -> Color(0xFFFF6B6B)
    "earth" -> Color(0xFFE0C097)
    "air" -> Color(0xFF4ECDC4)
    "water" -> Color(0xFF4D96FF)
    else -> PurpleAccent
}

@Composable
fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TraitSection(title: String, traits: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        traits.forEach { trait ->
            Text(
                text = "• $trait",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}