package com.hkgroups.agecalculator.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Space Glassmorphism Palette
val BackgroundDark = Color(0xFF050B14)      // Deep Space background
val PrimaryNeon = Color(0xFF4D96FF)         // Electric Blue
val SurfaceGlass = Color(0xFFFFFFFF).copy(alpha = 0.08f)  // Glass surface (increased from 0.05f)
val BorderGlass = Color(0xFFFFFFFF).copy(alpha = 0.15f)   // Glass border (increased from 0.1f)

// Planet-specific colors
val MarsRed = Color(0xFFFF6B6B)
val JupiterBeige = Color(0xFFE0C097)
val NeptuneBlue = Color(0xFF4D96FF)
val SaturnGold = Color(0xFFE6BE8A)
val VenusGold = Color(0xFFFFC857)
val EarthBlue = Color(0xFF4DA8DA)
val MercuryGray = Color(0xFF8D8D8D)
val UranusGreen = Color(0xFF4ECDC4)

// Additional accent colors
val PurpleAccent = Color(0xFF9B59B6)
val GreenAccent = Color(0xFF2ECC71)

// Cosmic Gradient Brush
val CosmicGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF020408),
        Color(0xFF0B1021)
    )
)

// Legacy colors for compatibility
@Deprecated("Use BackgroundDark instead", ReplaceWith("BackgroundDark"))
val MutedGold = SaturnGold
@Deprecated("Use SurfaceGlass instead", ReplaceWith("SurfaceGlass"))
val WarmSand = Color(0xFFF4EAD5)
@Deprecated("Use Color.White instead", ReplaceWith("Color.White"))
val Charcoal = Color(0xFF36454F)
@Deprecated("Use Color.White.copy(alpha=0.7f) instead", ReplaceWith("Color.White.copy(alpha=0.7f)"))
val SoftGray = Color(0xFF8D8D8D)