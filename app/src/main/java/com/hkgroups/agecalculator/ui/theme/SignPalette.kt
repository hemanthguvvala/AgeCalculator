package com.hkgroups.agecalculator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * SignPalette — accent colors derived from the user's sun sign.
 *
 * The whole app reaches for `LocalSignPalette.current` so every screen
 * tints itself according to who the user is. Earth signs feel grounded
 * (warm gold/sand), water signs feel cool (deep blue/teal), fire signs
 * feel hot (red/orange), air signs feel airy (lavender/cyan).
 *
 * Falls back to a generic neon palette before a sign is known.
 */
data class SignPalette(
    val primary: Color,
    val secondary: Color,
    val glow: Color,
    val background: Color
) {
    companion object {
        val Default = SignPalette(
            primary = Color(0xFF4D96FF),
            secondary = Color(0xFF9B59B6),
            glow = Color(0xFF4D96FF),
            background = Color(0xFF050B14)
        )
    }
}

val LocalSignPalette: ProvidableCompositionLocal<SignPalette> =
    compositionLocalOf { SignPalette.Default }

@Composable
fun rememberSignPalette(signName: String?): SignPalette = when (signName?.lowercase()) {
    // Fire — vibrant reds and amber
    "aries" -> SignPalette(
        primary = Color(0xFFFF6B6B),
        secondary = Color(0xFFFFB05A),
        glow = Color(0xFFFF6B6B),
        background = Color(0xFF120508)
    )
    "leo" -> SignPalette(
        primary = Color(0xFFFFB347),
        secondary = Color(0xFFFF6B6B),
        glow = Color(0xFFFFB347),
        background = Color(0xFF120903)
    )
    "sagittarius" -> SignPalette(
        primary = Color(0xFFFF8E72),
        secondary = Color(0xFFE0C097),
        glow = Color(0xFFFF8E72),
        background = Color(0xFF110704)
    )

    // Earth — grounded gold and sage
    "taurus" -> SignPalette(
        primary = Color(0xFFA8D5A2),
        secondary = Color(0xFFE0C097),
        glow = Color(0xFFA8D5A2),
        background = Color(0xFF071108)
    )
    "virgo" -> SignPalette(
        primary = Color(0xFFC4D982),
        secondary = Color(0xFFE0C097),
        glow = Color(0xFFC4D982),
        background = Color(0xFF080F05)
    )
    "capricorn" -> SignPalette(
        primary = Color(0xFFD8B97A),
        secondary = Color(0xFF8FA38D),
        glow = Color(0xFFD8B97A),
        background = Color(0xFF0E0A04)
    )

    // Air — cool lavender and cyan
    "gemini" -> SignPalette(
        primary = Color(0xFFFFD56B),
        secondary = Color(0xFF6CC8E0),
        glow = Color(0xFFFFD56B),
        background = Color(0xFF0A0E12)
    )
    "libra" -> SignPalette(
        primary = Color(0xFFFFB3D1),
        secondary = Color(0xFFB39CD0),
        glow = Color(0xFFFFB3D1),
        background = Color(0xFF110A10)
    )
    "aquarius" -> SignPalette(
        primary = Color(0xFF6CC8E0),
        secondary = Color(0xFFB39CD0),
        glow = Color(0xFF6CC8E0),
        background = Color(0xFF050E12)
    )

    // Water — deep blue and teal
    "cancer" -> SignPalette(
        primary = Color(0xFFCFD8E8),
        secondary = Color(0xFF6CC8E0),
        glow = Color(0xFFCFD8E8),
        background = Color(0xFF050911)
    )
    "scorpio" -> SignPalette(
        primary = Color(0xFFB39CD0),
        secondary = Color(0xFF8E6CDC),
        glow = Color(0xFFB39CD0),
        background = Color(0xFF09050F)
    )
    "pisces" -> SignPalette(
        primary = Color(0xFF7FD8C9),
        secondary = Color(0xFF8E9CDC),
        glow = Color(0xFF7FD8C9),
        background = Color(0xFF050F0E)
    )

    else -> SignPalette.Default
}
