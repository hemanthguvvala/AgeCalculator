package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * A background component that displays twinkling stars using Canvas.
 * Creates a starry night effect with animated opacity for each star.
 */
@Composable
fun StarryBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Generate random star positions once and remember them
    val stars = remember {
        List(100) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                baseAlpha = Random.nextFloat() * 0.5f + 0.3f,
                twinkleSpeed = Random.nextInt(1000, 3000),
                delay = Random.nextInt(0, 2000)
            )
        }
    }

    // Create infinite transition for twinkling effect
    val infiniteTransition = rememberInfiniteTransition(label = "star_twinkle")
    
    // Create animations for each star
    val starAlphas = stars.map { star ->
        infiniteTransition.animateFloat(
            initialValue = star.baseAlpha,
            targetValue = star.baseAlpha + 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = star.twinkleSpeed,
                    delayMillis = star.delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_alpha_${star.hashCode()}"
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Draw stars on canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            stars.forEachIndexed { index, star ->
                val alpha = starAlphas[index].value

                drawCircle(
                    color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = star.size,
                    center = Offset(
                        x = star.x * canvasWidth,
                        y = star.y * canvasHeight
                    )
                )
            }
        }

        // Render content on top of stars
        content()
    }
}

/**
 * Data class representing a single star's properties.
 * @param x Horizontal position (0-1, normalized)
 * @param y Vertical position (0-1, normalized)
 * @param size Radius of the star
 * @param baseAlpha Base opacity value
 * @param twinkleSpeed Duration of twinkle animation in milliseconds
 * @param delay Initial delay before animation starts
 */
private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Int,
    val delay: Int
)
