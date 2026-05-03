package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import kotlin.math.cos
import kotlin.math.sin

/**
 * CosmicLoading — orbiting-planet loading indicator.
 *
 * Replaces `CircularProgressIndicator` everywhere. A central glowing core
 * with three planets orbiting at different radii and speeds gives loading
 * states actual personality — and stays on-brand with the cosmic theme.
 *
 * Tinted by the user's sign palette so the loader matches the app context.
 *
 * @param size diameter of the loader
 */
@Composable
fun CosmicLoading(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val palette = LocalSignPalette.current
    val transition = rememberInfiniteTransition(label = "cosmicLoading")

    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )
    val coreScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coreScale"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val rOuter = w * 0.42f
        val rMid = w * 0.30f
        val rInner = w * 0.18f

        // Faint orbit rings
        listOf(rOuter, rMid).forEach { r ->
            drawCircle(
                color = palette.primary.copy(alpha = 0.08f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Three planets at staggered angle offsets and speeds
        val rads = Math.toRadians(angle.toDouble()).toFloat()
        val rads2 = Math.toRadians((angle * 1.6 + 120).toDouble()).toFloat()
        val rads3 = Math.toRadians((angle * 0.7 + 240).toDouble()).toFloat()

        // Outer planet — primary palette
        drawCircle(
            color = palette.primary,
            radius = w * 0.05f,
            center = Offset(cx + rOuter * cos(rads), cy + rOuter * sin(rads))
        )
        // Mid planet — secondary palette, smaller
        drawCircle(
            color = palette.secondary,
            radius = w * 0.04f,
            center = Offset(cx + rMid * cos(rads2), cy + rMid * sin(rads2))
        )
        // Inner sparkle
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.025f,
            center = Offset(cx + rInner * cos(rads3), cy + rInner * sin(rads3))
        )

        // Pulsing central core with halo
        val coreR = w * 0.07f * coreScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.primary.copy(alpha = 0.6f),
                    palette.primary.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = coreR * 3f
            ),
            radius = coreR * 3f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White,
            radius = coreR,
            center = Offset(cx, cy)
        )
    }
}

/**
 * CosmicLoadingScreen — full-screen centered loading with optional caption.
 */
@Composable
fun CosmicLoadingScreen(
    caption: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CosmicLoading(size = 72.dp)
            if (caption != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}
