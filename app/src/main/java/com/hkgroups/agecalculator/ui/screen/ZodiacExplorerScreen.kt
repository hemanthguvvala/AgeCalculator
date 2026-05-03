package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.GlassCard
import com.hkgroups.agecalculator.ui.screen.components.GlassCardWithGlow
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.screen.components.pressableScale
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Brush
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZodiacExplorerScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val signs by viewModel.zodiacSignsState.collectAsState()

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
                    title = "Explore the zodiac",
                    subtitle = if (signs.isNotEmpty()) "${signs.size} signs · tap to dive in" else "Aligning the cosmos…",
                    onBack = { navController.popBackStack() }
                )

                if (signs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            com.hkgroups.agecalculator.ui.screen.components.CosmicLoading()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Aligning the cosmos...",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    com.hkgroups.agecalculator.ui.screen.components.ZodiacWheel(
                        signs = signs,
                        onSignClick = { sign ->
                            navController.navigate(Screen.ZodiacDetail.createRoute(sign.name))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ZodiacGridCard(sign: ZodiacSign, onClick: () -> Unit) {
    val accent = elementAccent(sign.element)
    val interactionSource = remember { MutableInteractionSource() }

    GlassCardWithGlow(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accent),
                onClick = onClick
            )
            .pressableScale(interactionSource),
        glowColor = accent,
        glowAlpha = 0.45f,
        elevation = 18.dp,
        shape = RoundedCornerShape(22.dp)
    ) {
        // Subtle element-tinted radial wash so each card reads as its element
        // (fire/water/earth/air) without yelling.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        radius = 320f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glyph disc with halo + thin element-colored border for richness.
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.32f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                    sign = sign.name,
                    strokeColor = Color.White,
                    accentColor = accent,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = sign.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = sign.element.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (sign.dateRange.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sign.dateRange,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}

private fun elementAccent(element: String): Color = when (element.lowercase()) {
    "fire" -> Color(0xFFFF6B6B)
    "earth" -> Color(0xFFE0C097)
    "air" -> Color(0xFF4ECDC4)
    "water" -> Color(0xFF4D96FF)
    else -> PurpleAccent
}
