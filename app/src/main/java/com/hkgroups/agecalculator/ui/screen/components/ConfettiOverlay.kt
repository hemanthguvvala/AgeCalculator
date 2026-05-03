package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.random.Random

/**
 * Confetti — small physics-driven rectangle fluttering down the screen.
 */
private data class Confetto(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val bornAt: Long,
    val lifespanMillis: Int
)

/**
 * StreakMilestoneOverlay — full-screen confetti + congrats banner.
 *
 * Shows when [trigger] is non-null. Auto-dismisses after the celebration
 * window. Tied to the user's sign palette so colors feel personal.
 *
 * Usage: keep a `var trigger: Int? by remember { mutableStateOf(null) }`
 * and set it to the new streak value when crossing a threshold; the overlay
 * re-launches each time `trigger` changes.
 */
@Composable
fun StreakMilestoneOverlay(
    trigger: Int?,
    modifier: Modifier = Modifier
) {
    val palette = LocalSignPalette.current
    val haptics = LocalHapticFeedback.current
    val cosmicFeedback = com.hkgroups.agecalculator.util.LocalCosmicFeedback.current
    val pieces = remember { mutableStateListOf<Confetto>() }
    var bannerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        // Milestone burst: vibrato pattern + (opt-in) chime arpeggio.
        cosmicFeedback?.fire(com.hkgroups.agecalculator.util.CosmicFeedback.Cue.Milestone)
            ?: haptics.performHapticFeedback(HapticFeedbackType.LongPress)

        val rand = Random(System.nanoTime())
        val palette5 = listOf(
            palette.primary,
            palette.secondary,
            SaturnGold,
            Color.White,
            palette.primary.copy(alpha = 0.7f)
        )
        val now = System.currentTimeMillis()
        repeat(80) {
            pieces.add(
                Confetto(
                    x = rand.nextFloat(),
                    y = -0.05f - rand.nextFloat() * 0.1f,
                    vx = (rand.nextFloat() - 0.5f) * 0.18f,
                    vy = 0.18f + rand.nextFloat() * 0.18f,
                    rotation = rand.nextFloat() * 360f,
                    rotationSpeed = (rand.nextFloat() - 0.5f) * 360f,
                    color = palette5[rand.nextInt(palette5.size)],
                    width = 0.012f + rand.nextFloat() * 0.014f,
                    height = 0.022f + rand.nextFloat() * 0.018f,
                    bornAt = now,
                    lifespanMillis = 2400 + rand.nextInt(1200)
                )
            )
        }
        bannerVisible = true
        delay(2400)
        bannerVisible = false
    }

    var lastFrame by remember { mutableStateOf(0L) }
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now ->
                if (lastFrame == 0L) lastFrame = now
                val dt = (now - lastFrame).coerceAtLeast(0L) / 1_000_000_000f
                lastFrame = now
                if (pieces.isEmpty()) return@withFrameNanos
                val nowMs = System.currentTimeMillis()
                val iter = pieces.iterator()
                while (iter.hasNext()) {
                    val c = iter.next()
                    c.vy += 0.18f * dt   // gravity
                    c.x += c.vx * dt
                    c.y += c.vy * dt
                    c.rotation += c.rotationSpeed * dt
                    val age = (nowMs - c.bornAt).toInt()
                    if (age >= c.lifespanMillis || c.y > 1.2f) iter.remove()
                }
                tick++
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_EXPRESSION") tick
            val w = size.width
            val h = size.height
            val nowMs = System.currentTimeMillis()
            pieces.forEach { c ->
                val age = (nowMs - c.bornAt).toFloat() / c.lifespanMillis
                val alpha = (1f - age).coerceIn(0f, 1f)
                rotate(degrees = c.rotation, pivot = Offset(c.x * w, c.y * h)) {
                    drawRect(
                        color = c.color.copy(alpha = alpha),
                        topLeft = Offset(c.x * w - c.width * w / 2, c.y * h - c.height * h / 2),
                        size = Size(c.width * w, c.height * h)
                    )
                }
            }
        }

        // Banner — slides in from the top with a streak callout.
        androidx.compose.animation.AnimatedVisibility(
            visible = bannerVisible,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { -it },
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                )
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 96.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🔥", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$trigger-day streak!",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = streakSubtitle(trigger),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaturnGold
                )
            }
        }
    }
}

private fun streakSubtitle(streak: Int?): String = when (streak) {
    7 -> "A full week with the cosmos."
    30 -> "A month of cosmic alignment."
    100 -> "Triple-digit dedication. Stellar."
    365 -> "A full orbit around the sun."
    else -> "Keep the streak alive."
}

/** Returns true when streak crosses one of the celebrated milestones. */
fun isStreakMilestone(streak: Int): Boolean =
    streak == 7 || streak == 30 || streak == 100 || streak == 365
