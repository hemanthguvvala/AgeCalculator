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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import kotlin.math.cos
import kotlin.math.sin

/**
 * CosmicEmptyState — illustrated empty/no-data placeholder.
 *
 * Replaces "no events found" plain text with a small animated cosmos:
 * a faded planet on an orbital path with a slow-drifting moon, plus a
 * supportive title + body. The illustration is sign-tinted so a Taurus
 * empty state feels green-gold, an Aries one feels red-amber.
 */
@Composable
fun CosmicEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalSignPalette.current
    val transition = rememberInfiniteTransition(label = "cosmicEmpty")
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "emptyOrbit"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustration — drawn directly with Canvas so it scales crisply.
            Canvas(modifier = Modifier.size(180.dp)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val orbitR = w * 0.40f
                val planetR = w * 0.16f
                val moonR = w * 0.06f

                // Soft halo behind the planet
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0.32f),
                            palette.secondary.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(cx, cy)
                )
                // Orbital ring
                drawCircle(
                    color = palette.primary.copy(alpha = 0.18f),
                    radius = orbitR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // Central planet
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.primary,
                            palette.secondary
                        ),
                        center = Offset(cx - planetR * 0.3f, cy - planetR * 0.3f),
                        radius = planetR
                    ),
                    radius = planetR,
                    center = Offset(cx, cy)
                )
                // Moon orbiting the planet
                val rad = Math.toRadians(orbit.toDouble()).toFloat()
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = moonR,
                    center = Offset(
                        cx + orbitR * cos(rad),
                        cy + orbitR * sin(rad)
                    )
                )
                // A second smaller dot offset
                val rad2 = Math.toRadians((orbit * 0.6 + 130).toDouble()).toFloat()
                drawCircle(
                    color = palette.secondary.copy(alpha = 0.9f),
                    radius = moonR * 0.6f,
                    center = Offset(
                        cx + orbitR * 0.7f * cos(rad2),
                        cy + orbitR * 0.7f * sin(rad2)
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
