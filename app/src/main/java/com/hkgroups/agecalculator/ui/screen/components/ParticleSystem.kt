package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Particle — single short-lived sparkle in the system.
 *
 * Position is in normalized 0..1 coordinates so emitters work at any size.
 * Velocity is in normalized units per second. Life decays from 1f → 0f over
 * `lifespanMillis`, modulating size and alpha.
 */
internal data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val bornAt: Long,
    val lifespanMillis: Int
)

/**
 * Emitter — exposes a single `burst(originNorm)` call from the host
 * composable. The host wraps a Box around its content and lays a
 * [ParticleField] on top. Calling burst spawns sparkles at the origin.
 */
class ParticleEmitter {
    internal val active = mutableStateListOf<Particle>()
    private var seed = 0L

    /**
     * Spawn a burst of sparkles at the given normalized origin (0..1).
     */
    fun burst(
        originXNorm: Float,
        originYNorm: Float,
        count: Int = 14,
        colors: List<Color> = listOf(Color.White),
        speed: Float = 0.45f,
        lifespanMillis: Int = 900
    ) {
        val rand = Random(System.nanoTime() + seed++)
        val now = System.currentTimeMillis()
        repeat(count) {
            val angle = rand.nextFloat() * 2f * PI.toFloat()
            val v = speed * (0.5f + rand.nextFloat() * 0.7f)
            active.add(
                Particle(
                    x = originXNorm,
                    y = originYNorm,
                    vx = cos(angle) * v,
                    vy = sin(angle) * v - 0.18f, // slight upward bias
                    size = 0.004f + rand.nextFloat() * 0.012f,
                    color = colors[rand.nextInt(colors.size)],
                    bornAt = now,
                    lifespanMillis = (lifespanMillis * (0.7f + rand.nextFloat() * 0.6f)).toInt()
                )
            )
        }
    }
}

@Composable
fun rememberParticleEmitter(): ParticleEmitter = remember { ParticleEmitter() }

/**
 * ParticleField — overlay that renders an emitter's active particles.
 *
 * Drives a single animation loop via withFrameNanos, advances each particle
 * each frame, and prunes dead ones. Cheap because the loop only runs while
 * particles are alive (re-armed by burst()).
 */
@Composable
fun BoxScope.ParticleField(
    emitter: ParticleEmitter,
    modifier: Modifier = Modifier
) {
    var lastFrameNanos by remember { mutableStateOf(0L) }
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(emitter) {
        while (true) {
            withFrameNanos { now ->
                if (lastFrameNanos == 0L) lastFrameNanos = now
                val dt = ((now - lastFrameNanos).coerceAtLeast(0L)) / 1_000_000_000f
                lastFrameNanos = now

                if (emitter.active.isEmpty()) return@withFrameNanos

                val nowMs = System.currentTimeMillis()
                val iter = emitter.active.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    p.x += p.vx * dt
                    p.y += p.vy * dt + 0.45f * dt * dt * 0.5f // subtle gravity
                    val age = (nowMs - p.bornAt).toInt()
                    if (age >= p.lifespanMillis) iter.remove()
                }
                tick++
            }
        }
    }

    // matchParentSize is a BoxScope-only modifier that measures the parent's
    // actual size without contributing to the box's measurement loop —
    // critical so the field doesn't blow out a header row layout.
    Canvas(modifier = modifier.matchParentSize()) {
        // Read tick to ensure invalidation each frame the emitter is active.
        @Suppress("UNUSED_EXPRESSION") tick
        val w = size.width
        val h = size.height
        val nowMs = System.currentTimeMillis()
        emitter.active.forEach { p ->
            val age = (nowMs - p.bornAt).toFloat() / p.lifespanMillis
            val life = (1f - age).coerceIn(0f, 1f)
            // Sparkle fades in fast, holds, fades out
            val alpha = if (age < 0.15f) age / 0.15f else life
            val r = p.size * w * (0.5f + life * 0.5f)
            drawCircle(
                color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = r,
                center = Offset(p.x * w, p.y * h)
            )
        }
    }
}

/**
 * SparkleBurst — convenience wrapper. Wraps `content` in a Box, exposes a
 * particle field overlay, and returns the emitter so the caller can burst
 * from any handler (clickable, gesture, etc).
 *
 * Usage:
 * ```
 * SparkleBurst { emitter ->
 *     IconButton(onClick = { emitter.burst(0.5f, 0.5f) }) { ... }
 * }
 * ```
 */
@Composable
fun SparkleBurst(
    modifier: Modifier = Modifier,
    content: @Composable (ParticleEmitter) -> Unit
) {
    val palette = LocalSignPalette.current
    val emitter = rememberParticleEmitter()
    Box(modifier = modifier) {
        content(emitter)
        ParticleField(emitter = emitter)
    }
}
