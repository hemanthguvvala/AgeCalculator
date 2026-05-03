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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.util.LunarPhase
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/**
 * MoonPhaseGlyph — Canvas-drawn moon disk + terminator. The lit side is
 * geometric (semicircle clipped against the disk, with an elliptical
 * terminator added for crescent/gibbous shaping). Northern hemisphere
 * convention: waxing → light on the right, waning → light on the left.
 */
@Composable
fun MoonPhaseGlyph(
    fraction: Double,
    modifier: Modifier = Modifier,
    litColor: Color = Color(0xFFEFE7C8),
    darkColor: Color = Color(0xFF1B1F38),
    haloColor: Color = SaturnGold
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2 * 0.82f
        val cx = size.width / 2
        val cy = size.height / 2
        val center = Offset(cx, cy)

        // Soft halo behind the disk
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(haloColor.copy(alpha = 0.28f), Color.Transparent),
                center = center,
                radius = r * 1.6f
            ),
            radius = r * 1.6f,
            center = center
        )

        val diskRect = Rect(cx - r, cy - r, cx + r, cy + r)
        val diskPath = Path().apply { addOval(diskRect) }

        // Base dark disk
        drawCircle(darkColor, r, center)

        val phase = LunarPhase.phase(fraction)
        if (phase == LunarPhase.Phase.NewMoon) {
            // Just a thin rim — disk stays dark.
            drawCircle(
                color = litColor.copy(alpha = 0.22f),
                radius = r,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f)
            )
            return@Canvas
        }
        if (phase == LunarPhase.Phase.FullMoon) {
            drawCircle(litColor, r, center)
            // gentle inner gradient for dimensionality
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(cx - r * 0.25f, cy - r * 0.25f),
                    radius = r
                ),
                radius = r,
                center = center
            )
            return@Canvas
        }

        clipPath(diskPath) {
            drawMoonInterior(fraction, cx, cy, r, litColor, darkColor)
        }
    }
}

private fun DrawScope.drawMoonInterior(
    fraction: Double,
    cx: Float,
    cy: Float,
    r: Float,
    litColor: Color,
    darkColor: Color
) {
    val isWaxing = fraction < 0.5
    val illumination = (1 - cos(2 * PI * fraction)) / 2  // 0..1

    // Light up the appropriate semicircle (already clipped to disk)
    val halfTopLeft = if (isWaxing) Offset(cx, cy - r) else Offset(cx - r, cy - r)
    drawRect(
        color = litColor,
        topLeft = halfTopLeft,
        size = Size(r, r * 2)
    )

    // Terminator ellipse: width is 0 at quarter (illumination=0.5), grows toward new/full.
    val terminatorWidth = (abs(2 * illumination - 1) * 2 * r).toFloat()
    val terminatorRect = Rect(
        cx - terminatorWidth / 2,
        cy - r,
        cx + terminatorWidth / 2,
        cy + r
    )

    val terminatorColor = if (illumination < 0.5) darkColor else litColor
    drawOval(
        color = terminatorColor,
        topLeft = terminatorRect.topLeft,
        size = Size(terminatorRect.width, terminatorRect.height)
    )

    // Subtle inner shading on the lit side for dimensionality
    val highlightCx = if (isWaxing) cx + r * 0.4f else cx - r * 0.4f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(highlightCx, cy - r * 0.3f),
            radius = r * 0.7f
        ),
        radius = r * 0.7f,
        center = Offset(highlightCx, cy - r * 0.3f)
    )
}

/**
 * MoonPhaseCard — dashboard card showing the current lunar phase.
 * Ties the abstract sky-state into the user's daily flow with a one-line
 * energy hint and a countdown to the next major phase.
 */
@Composable
fun MoonPhaseCard(
    today: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier
) {
    val palette = LocalSignPalette.current
    val fraction = remember(today) { LunarPhase.phaseFraction(today) }
    val phase = remember(fraction) { LunarPhase.phase(fraction) }
    val illumination = remember(fraction) { LunarPhase.illuminationPercent(fraction) }
    val (nextPhase, daysToNext) = remember(fraction) { LunarPhase.daysToNextMajorPhase(fraction) }
    val hint = remember(phase) { LunarPhase.energyHint(phase) }

    // Very slow rotational shimmer on the halo to feel "alive" without distracting.
    val transition = rememberInfiniteTransition(label = "moon-shimmer")
    val shimmer by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            MoonPhaseGlyph(
                fraction = fraction,
                modifier = Modifier.size((84 * shimmer).dp),
                haloColor = palette.primary
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phase.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$illumination% illuminated",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 2
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 6.dp)
                        .padding(end = 4.dp)
                ) {
                    Canvas(Modifier.fillMaxWidth()) {
                        drawCircle(SaturnGold, radius = size.minDimension / 2)
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${nextPhase.title} in $daysToNext day${if (daysToNext == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SaturnGold,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * MoonPhaseStrip — compact 7-day forecast strip for the lunar cycle.
 * Each cell shows a tiny glyph for that day's phase, today highlighted.
 */
@Composable
fun MoonPhaseStrip(
    today: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier,
    daySpan: Int = 7
) {
    val palette = LocalSignPalette.current
    val days = remember(today, daySpan) {
        (0 until daySpan).map { offset ->
            val date = today.plusDays(offset.toLong())
            Triple(date, LunarPhase.phaseFraction(date), date == today)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { (date, frac, isToday) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfWeek.toString().take(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) palette.primary else Color.White.copy(alpha = 0.55f),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                )
                Spacer(Modifier.height(4.dp))
                MoonPhaseGlyph(
                    fraction = frac,
                    modifier = Modifier.size(28.dp),
                    haloColor = if (isToday) palette.primary else Color.Transparent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) Color.White else Color.White.copy(alpha = 0.45f),
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 10.sp
                )
            }
        }
    }
}
