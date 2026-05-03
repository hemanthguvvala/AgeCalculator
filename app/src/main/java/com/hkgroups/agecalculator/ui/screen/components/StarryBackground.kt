package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Atmospheric cosmic background — three layered passes:
 *   1. A vertical gradient using the user's sign palette (deep top → black bottom)
 *   2. Two slow-moving nebula glows that drift around the screen
 *   3. A sparse, twinkling star field tied to the user's sign palette
 */
@Composable
fun StarryBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalSignPalette.current

    // 18 stars — atmospheric, but few enough that the background never becomes
    // the frame-rate bottleneck.
    val stars = remember {
        val rand = Random(42)
        List(18) {
            Star(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                size = rand.nextFloat() * 1.6f + 0.8f,
                baseAlpha = rand.nextFloat() * 0.35f + 0.30f,
                twinkleSpeed = rand.nextInt(2400, 4200),
                delay = rand.nextInt(0, 2000)
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "atmoBg")

    // Single shared twinkle animation drives all stars via fixed phase offsets.
    // Cuts 18 individual animateFloat instances down to 1 — frame-time win.
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    // Nebula drift — circular orbits, very slow (~60s per loop), so the bg
    // breathes without distracting from foreground content.
    val driftA by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebulaDriftA"
    )
    val driftB by transition.animateFloat(
        initialValue = (PI / 2f).toFloat(),
        targetValue = (PI / 2f + 2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebulaDriftB"
    )

    // Base vertical gradient — sign-tinted top → deep space bottom.
    val baseGradient = remember(palette) {
        Brush.verticalGradient(
            colors = listOf(
                palette.background,
                Color(0xFF02040A),
                Color.Black
            )
        )
    }

    Box(modifier = modifier
        .fillMaxSize()
        .background(baseGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Nebula glow A — primary-tinted, drifting in a slow ellipse near the top.
            val ax = w * (0.5f + 0.20f * cos(driftA))
            val ay = h * (0.22f + 0.06f * sin(driftA))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.22f),
                        palette.primary.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(ax, ay),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(ax, ay)
            )

            // Nebula glow B — secondary-tinted, drifting in a slow ellipse below center.
            val bx = w * (0.5f + 0.18f * cos(driftB))
            val by = h * (0.62f + 0.08f * sin(driftB))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.secondary.copy(alpha = 0.18f),
                        palette.secondary.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(bx, by),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(bx, by)
            )

            // Sparse star field — single twinkle var phased per star index.
            stars.forEachIndexed { index, star ->
                val phase = (index * 0.137f) % 1f
                val t = (twinkle + phase) % 1f
                val swing = kotlin.math.abs(t - 0.5f) * 2f // 0..1..0
                val alpha = (star.baseAlpha + (1f - swing) * 0.32f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = star.size,
                    center = Offset(
                        x = star.x * w,
                        y = star.y * h
                    )
                )
            }
        }

        content()
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Int,
    val delay: Int
)
