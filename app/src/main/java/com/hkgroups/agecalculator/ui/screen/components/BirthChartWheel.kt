package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * BirthChartWheel — 12-segment zodiac wheel with the user's sun sign
 * highlighted. Each segment shows the sign's drawn glyph. The user's segment
 * is filled with a sign-tinted gradient and outlined with a brighter ring;
 * other segments are subtle.
 *
 * Drawn entirely on Canvas — no lazy lists, no per-segment composables —
 * because birth charts are essentially geometry diagrams.
 *
 * @param sunSignName the user's sun sign — case-insensitive
 * @param modifier sizing
 */
@Composable
fun BirthChartWheel(
    sunSignName: String?,
    modifier: Modifier = Modifier,
    diameter: Dp = 280.dp
) {
    val palette = LocalSignPalette.current
    // Slow rotation of an inner ring of stars for a subtle "alive" feel.
    val transition = rememberInfiniteTransition(label = "birthChart")
    val starsRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starsRotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(diameter),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val rOuter = w * 0.46f
            val rGlyph = w * 0.36f
            val rInner = w * 0.12f
            val signs = ZodiacOrder

            // Outer + inner rings
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = rOuter,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = rOuter * 0.78f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = rInner,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            // Twelve sector dividers
            for (i in 0 until 12) {
                val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / 12f
                drawLine(
                    color = Color.White.copy(alpha = 0.10f),
                    start = Offset(cx + rInner * cos(a), cy + rInner * sin(a)),
                    end = Offset(cx + rOuter * cos(a), cy + rOuter * sin(a)),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Highlight the user's sun-sign segment with a tinted wedge.
            val sunIndex = signs.indexOfFirst { it.equals(sunSignName, ignoreCase = true) }
            if (sunIndex >= 0) {
                val startA = -PI.toFloat() / 2f + sunIndex * 2f * PI.toFloat() / 12f - PI.toFloat() / 12f
                val sweep = 2f * PI.toFloat() / 12f
                val wedge = Path().apply {
                    fillType = PathFillType.EvenOdd
                    moveTo(cx + rInner * cos(startA), cy + rInner * sin(startA))
                    lineTo(cx + rOuter * cos(startA), cy + rOuter * sin(startA))
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            offset = Offset(cx - rOuter, cy - rOuter),
                            size = Size(rOuter * 2, rOuter * 2)
                        ),
                        startAngleDegrees = Math.toDegrees(startA.toDouble()).toFloat(),
                        sweepAngleDegrees = Math.toDegrees(sweep.toDouble()).toFloat(),
                        forceMoveTo = false
                    )
                    val endA = startA + sweep
                    lineTo(cx + rInner * cos(endA), cy + rInner * sin(endA))
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            offset = Offset(cx - rInner, cy - rInner),
                            size = Size(rInner * 2, rInner * 2)
                        ),
                        startAngleDegrees = Math.toDegrees(endA.toDouble()).toFloat(),
                        sweepAngleDegrees = -Math.toDegrees(sweep.toDouble()).toFloat(),
                        forceMoveTo = false
                    )
                    close()
                }
                drawPath(
                    path = wedge,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0.45f),
                            palette.primary.copy(alpha = 0.15f)
                        ),
                        center = Offset(cx, cy),
                        radius = rOuter
                    )
                )
                // Highlight outline
                drawPath(
                    path = wedge,
                    color = palette.primary,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Slow-rotating decorative stars on the mid ring.
            translate(left = cx, top = cy) {
                for (i in 0 until 24) {
                    val a = Math.toRadians((starsRotation + i * 15.0)).toFloat()
                    val r = rOuter * 0.62f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.18f),
                        radius = 1.4.dp.toPx(),
                        center = Offset(r * cos(a), r * sin(a))
                    )
                }
            }

            // Center medallion
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = rInner * 1.6f
                ),
                radius = rInner * 1.6f,
                center = Offset(cx, cy)
            )
        }

        // Glyph layer — 12 ZodiacGlyph composables placed around the wheel
        // so each sign reads as proper drawn vector geometry, not rasterized
        // text inside the canvas.
        val density = LocalDensity.current
        val ringRadiusPx = with(density) { diameter.toPx() * 0.36f }
        ZodiacOrder.forEachIndexed { i, name ->
            val a = -PI / 2 + i * 2 * PI / 12.0
            val xDp = with(density) { (cos(a).toFloat() * ringRadiusPx).toDp() }
            val yDp = with(density) { (sin(a).toFloat() * ringRadiusPx).toDp() }
            val isSun = name.equals(sunSignName, ignoreCase = true)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = xDp, y = yDp)
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                ZodiacGlyph(
                    sign = name,
                    strokeColor = if (isSun) Color.White else Color.White.copy(alpha = 0.55f),
                    accentColor = if (isSun) palette.primary else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(if (isSun) 28.dp else 22.dp)
                )
            }
        }

        // Center sun-sign label
        if (sunSignName != null) {
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                ZodiacGlyph(
                    sign = sunSignName,
                    strokeColor = Color.White,
                    accentColor = palette.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

private val ZodiacOrder = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)
