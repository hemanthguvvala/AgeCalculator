package com.hkgroups.agecalculator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Deep Space Glassmorphism Color Scheme
private val DeepSpaceColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = NeptuneBlue,
    tertiary = PurpleAccent,
    background = BackgroundDark,
    surface = SurfaceGlass,
    surfaceVariant = Color(0xFFFFFFFF).copy(alpha = 0.08f),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF4D96FF).copy(alpha = 0.15f),
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF9B59B6).copy(alpha = 0.15f),
    onSecondaryContainer = Color.White,
    tertiaryContainer = Color(0xFFFF6B6B).copy(alpha = 0.15f),
    onTertiaryContainer = Color.White,
    outline = BorderGlass,
    outlineVariant = Color(0xFFFFFFFF).copy(alpha = 0.05f)
)

// Legacy light scheme for compatibility
private val LightColorScheme = lightColorScheme(
    primary = PrimaryNeon,
    secondary = Color(0xFF36454F),
    background = Color(0xFFF4EAD5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF36454F),
    onSurface = Color(0xFF36454F)
)

@Composable
fun ZodiacAgeTheme(
    darkTheme: Boolean = true, // Default to dark theme for Deep Space aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DeepSpaceColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}