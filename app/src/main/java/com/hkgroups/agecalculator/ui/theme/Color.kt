package com.hkgroups.agecalculator.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Space Glassmorphism Palette
val BackgroundDark = Color(0xFF050B14)      // Deep Space background
val SurfaceDark = Color(0xFF0E1530)         // Slightly lifted surface for dialogs/sheets
val PrimaryNeon = Color(0xFF4D96FF)         // Electric Blue
// Solid-base glass — cards are opaque dark blue-purple with the "glass" feel
// coming from layered styling (gradient highlight + rim light + specular)
// rather than transparency. This is how premium apps (iOS Control Center,
// Linear, Notion) actually render glass — the surface is solid, the glass
// affordances are painted on top. Result: tall cards never look like hollow
// rectangles because no background ever bleeds through.
val GlassBase = Color(0xFF161A2E)        // Solid card base
val GlassBaseTop = Color(0xFF252B4A)     // Slightly lifted top for the gradient highlight
val SurfaceGlassTop = GlassBaseTop
val SurfaceGlassMid = GlassBase
val SurfaceGlassBottom = GlassBase
val SurfaceGlass = GlassBase
val GlassRimLight = Color(0xFFFFFFFF).copy(alpha = 0.42f)
val GlassSpecular = Color(0xFFFFFFFF).copy(alpha = 0.10f)
val BorderGlassTop = Color(0xFFFFFFFF).copy(alpha = 0.22f)
val BorderGlassBottom = Color(0xFFFFFFFF).copy(alpha = 0.04f)
val BorderGlass = Color(0xFFFFFFFF).copy(alpha = 0.14f)

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