package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * RadarAxis — one dimension of compatibility scoring.
 */
data class RadarAxis(
    val label: String,
    val value: Float // 0..1
)

/**
 * CompatibilityRadar — animated polygon chart visualizing multi-dimensional
 * alignment between two signs. Uses 5 axes: Love, Communication, Trust,
 * Energy, Values.
 *
 * The polygon fills in over ~700ms on first composition, giving the result
 * page a "calculating then revealing" feel rather than appearing instantly.
 */
@Composable
fun CompatibilityRadar(
    axes: List<RadarAxis>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(axes) { animateIn = true }
    val progress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "radarFill"
    )

    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = Color.White.copy(alpha = 0.85f),
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (minOf(w, h) / 2f) - 48.dp.toPx()
            val n = axes.size

            // Concentric grid rings (4 rings).
            (1..4).forEach { ring ->
                val r = radius * (ring / 4f)
                val path = Path()
                for (i in 0 until n) {
                    val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / n
                    val x = cx + r * cos(a)
                    val y = cy + r * sin(a)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.06f + ring * 0.02f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Axis spokes.
            for (i in 0 until n) {
                val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / n
                drawLine(
                    color = Color.White.copy(alpha = 0.10f),
                    start = Offset(cx, cy),
                    end = Offset(cx + radius * cos(a), cy + radius * sin(a)),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Filled value polygon (animated).
            val fillPath = Path()
            val strokePath = Path()
            for (i in 0 until n) {
                val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / n
                val v = (axes[i].value.coerceIn(0f, 1f) * progress)
                val r = radius * v
                val x = cx + r * cos(a)
                val y = cy + r * sin(a)
                if (i == 0) {
                    fillPath.moveTo(x, y)
                    strokePath.moveTo(x, y)
                } else {
                    fillPath.lineTo(x, y)
                    strokePath.lineTo(x, y)
                }
            }
            fillPath.close()
            strokePath.close()

            drawPath(
                path = fillPath,
                color = accent.copy(alpha = 0.30f)
            )
            drawPath(
                path = strokePath,
                color = accent,
                style = Stroke(width = 2.dp.toPx())
            )

            // Vertex dots.
            for (i in 0 until n) {
                val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / n
                val v = (axes[i].value.coerceIn(0f, 1f) * progress)
                val r = radius * v
                drawCircle(
                    color = accent,
                    radius = 4.dp.toPx(),
                    center = Offset(cx + r * cos(a), cy + r * sin(a))
                )
            }

            // Axis labels (rotated to follow each spoke).
            for (i in 0 until n) {
                val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / n
                val labelR = radius + 22.dp.toPx()
                val labelX = cx + labelR * cos(a)
                val labelY = cy + labelR * sin(a)
                val layout = measurer.measure(
                    text = axes[i].label,
                    style = labelStyle
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = labelX - layout.size.width / 2f,
                        y = labelY - layout.size.height / 2f
                    )
                )
            }
        }
    }
}

/**
 * Derive 5 compatibility axis values from the seed rating + sign elements.
 * Pure function so it's stable across re-renders given the same inputs.
 */
fun deriveCompatibilityAxes(
    rating10: Int,
    yourElement: String,
    partnerElement: String
): List<RadarAxis> {
    // Base value scales with the seed rating (0..10 → 0..1).
    val base = (rating10.coerceIn(0, 10) / 10f).coerceIn(0.05f, 1f)
    // Element bonuses — fire+air get along, water+earth get along, same element
    // is a 1.0 multiplier, opposite combos get a small dip.
    fun pairing(ours: String, theirs: String): Float = when {
        ours.equals(theirs, ignoreCase = true) -> 1.0f
        (ours.equals("fire", true) && theirs.equals("air", true)) ||
            (ours.equals("air", true) && theirs.equals("fire", true)) -> 1.05f
        (ours.equals("earth", true) && theirs.equals("water", true)) ||
            (ours.equals("water", true) && theirs.equals("earth", true)) -> 1.05f
        else -> 0.85f
    }
    val pair = pairing(yourElement, partnerElement)
    fun jiggle(seed: Int): Float = ((seed * 17 + 31) % 9 - 4) / 100f
    return listOf(
        RadarAxis(
            "Love",
            (base * pair * 1.10f + jiggle(rating10 + 1)).coerceIn(0.10f, 1f)
        ),
        RadarAxis(
            "Communication",
            (base * pair * 0.95f + jiggle(rating10 + 2)).coerceIn(0.10f, 1f)
        ),
        RadarAxis(
            "Trust",
            (base * pair * 1.00f + jiggle(rating10 + 3)).coerceIn(0.10f, 1f)
        ),
        RadarAxis(
            "Energy",
            (base * pair * 1.05f + jiggle(rating10 + 4)).coerceIn(0.10f, 1f)
        ),
        RadarAxis(
            "Values",
            (base * pair * 0.92f + jiggle(rating10 + 5)).coerceIn(0.10f, 1f)
        )
    )
}
