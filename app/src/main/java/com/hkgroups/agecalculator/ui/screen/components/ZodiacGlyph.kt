package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ZodiacGlyph — custom-drawn zodiac symbol.
 *
 * Renders proper vector geometry (curves, arcs, lines) for each sign with a
 * gradient stroke. Replacing the emoji/unicode-glyph mix with a single drawn
 * system gives every zodiac surface a consistent, premium identity instead of
 * looking like Google emoji slapped on a cosmic background.
 *
 * @param sign zodiac sign name (case-insensitive). Falls back to a star.
 * @param strokeColor primary stroke color
 * @param accentColor optional secondary gradient stop
 * @param strokeWidthFraction stroke thickness as a fraction of the canvas size
 */
@Composable
fun ZodiacGlyph(
    sign: String,
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.White,
    accentColor: Color = strokeColor,
    strokeWidthFraction: Float = 0.06f
) {
    Canvas(modifier = modifier) {
        drawZodiacGlyph(
            sign = sign,
            strokeColor = strokeColor,
            accentColor = accentColor,
            strokeWidthFraction = strokeWidthFraction
        )
    }
}

private fun DrawScope.drawZodiacGlyph(
    sign: String,
    strokeColor: Color,
    accentColor: Color,
    strokeWidthFraction: Float
) {
    val w = size.width
    val h = size.height
    val s = minOf(w, h)
    val stroke = s * strokeWidthFraction

    // Center the drawing in a square within the canvas
    val left = (w - s) / 2f
    val top = (h - s) / 2f

    val brush = Brush.linearGradient(
        colors = listOf(strokeColor, accentColor),
        start = Offset(left, top),
        end = Offset(left + s, top + s)
    )
    val style = Stroke(
        width = stroke,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    translate(left = left, top = top) {
        when (sign.lowercase()) {
            "aries" -> drawAries(brush, style, s)
            "taurus" -> drawTaurus(brush, style, s)
            "gemini" -> drawGemini(brush, style, s)
            "cancer" -> drawCancer(brush, style, s)
            "leo" -> drawLeo(brush, style, s)
            "virgo" -> drawVirgo(brush, style, s)
            "libra" -> drawLibra(brush, style, s)
            "scorpio" -> drawScorpio(brush, style, s)
            "sagittarius" -> drawSagittarius(brush, style, s)
            "capricorn" -> drawCapricorn(brush, style, s)
            "aquarius" -> drawAquarius(brush, style, s)
            "pisces" -> drawPisces(brush, style, s)
            else -> drawStar(brush, style, s)
        }
    }
}

// ---------- Sign drawings ----------

/** Aries — ram horns: two outward-curling spirals from a central stem. */
private fun DrawScope.drawAries(brush: Brush, style: Stroke, s: Float) {
    val cx = s / 2f
    val topY = s * 0.32f
    val bottomY = s * 0.78f
    val path = Path().apply {
        // Left horn
        moveTo(cx, bottomY)
        cubicTo(cx, topY, s * 0.28f, s * 0.18f, s * 0.18f, s * 0.36f)
        // Right horn (back to center then out)
        moveTo(cx, bottomY)
        cubicTo(cx, topY, s * 0.72f, s * 0.18f, s * 0.82f, s * 0.36f)
    }
    drawPath(path, brush, style = style)
}

/** Taurus — bull's head: circle with curved horns rising above. */
private fun DrawScope.drawTaurus(brush: Brush, style: Stroke, s: Float) {
    val cx = s / 2f
    val cy = s * 0.66f
    val r = s * 0.18f
    drawCircle(brush, radius = r, center = Offset(cx, cy), style = style)
    val horns = Path().apply {
        // Left horn arc
        moveTo(cx - r, cy - r * 0.4f)
        cubicTo(s * 0.18f, cy - r * 1.2f, s * 0.10f, s * 0.30f, s * 0.22f, s * 0.18f)
        // Right horn arc
        moveTo(cx + r, cy - r * 0.4f)
        cubicTo(s * 0.82f, cy - r * 1.2f, s * 0.90f, s * 0.30f, s * 0.78f, s * 0.18f)
    }
    drawPath(horns, brush, style = style)
}

/** Gemini — two vertical pillars connected by curved caps top and bottom. */
private fun DrawScope.drawGemini(brush: Brush, style: Stroke, s: Float) {
    val leftX = s * 0.30f
    val rightX = s * 0.70f
    val topY = s * 0.22f
    val bottomY = s * 0.78f
    val path = Path().apply {
        moveTo(leftX, topY)
        lineTo(leftX, bottomY)
        moveTo(rightX, topY)
        lineTo(rightX, bottomY)
        // Top arc cap
        moveTo(leftX - s * 0.06f, topY)
        cubicTo(leftX, topY - s * 0.06f, rightX, topY - s * 0.06f, rightX + s * 0.06f, topY)
        // Bottom arc cap
        moveTo(leftX - s * 0.06f, bottomY)
        cubicTo(leftX, bottomY + s * 0.06f, rightX, bottomY + s * 0.06f, rightX + s * 0.06f, bottomY)
    }
    drawPath(path, brush, style = style)
}

/** Cancer — two opposing 69-shape spirals. */
private fun DrawScope.drawCancer(brush: Brush, style: Stroke, s: Float) {
    val r = s * 0.10f
    // Top circle + curve trailing left
    drawCircle(brush, radius = r, center = Offset(s * 0.36f, s * 0.36f), style = style)
    // Bottom circle + curve trailing right
    drawCircle(brush, radius = r, center = Offset(s * 0.64f, s * 0.64f), style = style)
    val tails = Path().apply {
        moveTo(s * 0.36f - r, s * 0.36f)
        cubicTo(s * 0.18f, s * 0.36f, s * 0.18f, s * 0.66f, s * 0.34f, s * 0.66f)
        moveTo(s * 0.64f + r, s * 0.64f)
        cubicTo(s * 0.82f, s * 0.64f, s * 0.82f, s * 0.34f, s * 0.66f, s * 0.34f)
    }
    drawPath(tails, brush, style = style)
}

/** Leo — circle with a flowing tail curling up and over. */
private fun DrawScope.drawLeo(brush: Brush, style: Stroke, s: Float) {
    val r = s * 0.16f
    val cx = s * 0.40f
    val cy = s * 0.62f
    drawCircle(brush, radius = r, center = Offset(cx, cy), style = style)
    val tail = Path().apply {
        moveTo(cx + r, cy)
        cubicTo(s * 0.78f, s * 0.62f, s * 0.86f, s * 0.32f, s * 0.62f, s * 0.22f)
        cubicTo(s * 0.50f, s * 0.18f, s * 0.46f, s * 0.30f, s * 0.58f, s * 0.36f)
    }
    drawPath(tail, brush, style = style)
}

/** Virgo — three vertical strokes connected at top with a final loop. */
private fun DrawScope.drawVirgo(brush: Brush, style: Stroke, s: Float) {
    val topY = s * 0.28f
    val bottomY = s * 0.72f
    val path = Path().apply {
        moveTo(s * 0.22f, topY)
        lineTo(s * 0.22f, bottomY)
        moveTo(s * 0.42f, topY)
        lineTo(s * 0.42f, bottomY)
        moveTo(s * 0.62f, topY)
        lineTo(s * 0.62f, bottomY * 0.92f)
        // Connector arches at top
        moveTo(s * 0.22f, topY)
        cubicTo(s * 0.22f, topY - s * 0.08f, s * 0.42f, topY - s * 0.08f, s * 0.42f, topY)
        moveTo(s * 0.42f, topY)
        cubicTo(s * 0.42f, topY - s * 0.08f, s * 0.62f, topY - s * 0.08f, s * 0.62f, topY)
        // Final M-loop curling out
        moveTo(s * 0.62f, bottomY * 0.92f)
        cubicTo(s * 0.84f, bottomY * 0.92f, s * 0.86f, s * 0.50f, s * 0.66f, s * 0.50f)
    }
    drawPath(path, brush, style = style)
}

/** Libra — horizontal bar with a hill shape above (the scales). */
private fun DrawScope.drawLibra(brush: Brush, style: Stroke, s: Float) {
    val baseY = s * 0.66f
    val path = Path().apply {
        // Base bar
        moveTo(s * 0.18f, baseY)
        lineTo(s * 0.82f, baseY)
        // Upper bar with hill
        moveTo(s * 0.18f, s * 0.46f)
        lineTo(s * 0.34f, s * 0.46f)
        cubicTo(s * 0.40f, s * 0.46f, s * 0.40f, s * 0.30f, s * 0.50f, s * 0.30f)
        cubicTo(s * 0.60f, s * 0.30f, s * 0.60f, s * 0.46f, s * 0.66f, s * 0.46f)
        lineTo(s * 0.82f, s * 0.46f)
    }
    drawPath(path, brush, style = style)
}

/** Scorpio — M with an arrow flicking up from the last leg. */
private fun DrawScope.drawScorpio(brush: Brush, style: Stroke, s: Float) {
    val topY = s * 0.36f
    val bottomY = s * 0.74f
    val path = Path().apply {
        // M body
        moveTo(s * 0.18f, bottomY)
        lineTo(s * 0.18f, topY)
        cubicTo(s * 0.18f, topY - s * 0.06f, s * 0.34f, topY - s * 0.06f, s * 0.34f, topY)
        lineTo(s * 0.34f, bottomY)
        cubicTo(s * 0.34f, bottomY + s * 0.06f, s * 0.50f, bottomY + s * 0.06f, s * 0.50f, bottomY)
        lineTo(s * 0.50f, topY)
        cubicTo(s * 0.50f, topY - s * 0.06f, s * 0.66f, topY - s * 0.06f, s * 0.66f, topY)
        lineTo(s * 0.66f, bottomY)
        // Stinger flick up-right
        lineTo(s * 0.82f, bottomY)
        lineTo(s * 0.82f, s * 0.50f)
        // Arrowhead
        moveTo(s * 0.74f, s * 0.56f)
        lineTo(s * 0.82f, s * 0.50f)
        lineTo(s * 0.90f, s * 0.56f)
    }
    drawPath(path, brush, style = style)
}

/** Sagittarius — arrow with a crossbar. */
private fun DrawScope.drawSagittarius(brush: Brush, style: Stroke, s: Float) {
    val path = Path().apply {
        // Arrow shaft from bottom-left to top-right
        moveTo(s * 0.20f, s * 0.80f)
        lineTo(s * 0.78f, s * 0.22f)
        // Arrowhead
        moveTo(s * 0.78f, s * 0.22f)
        lineTo(s * 0.78f, s * 0.42f)
        moveTo(s * 0.78f, s * 0.22f)
        lineTo(s * 0.58f, s * 0.22f)
        // Crossbar
        moveTo(s * 0.36f, s * 0.42f)
        lineTo(s * 0.58f, s * 0.64f)
    }
    drawPath(path, brush, style = style)
}

/** Capricorn — V with a closed loop trailing right (sea-goat). */
private fun DrawScope.drawCapricorn(brush: Brush, style: Stroke, s: Float) {
    val path = Path().apply {
        // V
        moveTo(s * 0.18f, s * 0.30f)
        lineTo(s * 0.32f, s * 0.62f)
        lineTo(s * 0.46f, s * 0.30f)
        // Hook continuing right
        lineTo(s * 0.50f, s * 0.62f)
        // Loop / fish-tail
        cubicTo(s * 0.66f, s * 0.62f, s * 0.78f, s * 0.78f, s * 0.66f, s * 0.74f)
        cubicTo(s * 0.56f, s * 0.70f, s * 0.62f, s * 0.58f, s * 0.74f, s * 0.62f)
    }
    drawPath(path, brush, style = style)
}

/** Aquarius — two stacked zigzag waves. */
private fun DrawScope.drawAquarius(brush: Brush, style: Stroke, s: Float) {
    fun wave(y: Float): Path = Path().apply {
        moveTo(s * 0.16f, y)
        lineTo(s * 0.30f, y - s * 0.08f)
        lineTo(s * 0.44f, y)
        lineTo(s * 0.58f, y - s * 0.08f)
        lineTo(s * 0.72f, y)
        lineTo(s * 0.86f, y - s * 0.08f)
    }
    drawPath(wave(s * 0.42f), brush, style = style)
    drawPath(wave(s * 0.66f), brush, style = style)
}

/** Pisces — two crescents back-to-back, joined by a horizontal line. */
private fun DrawScope.drawPisces(brush: Brush, style: Stroke, s: Float) {
    val cy = s / 2f
    val path = Path().apply {
        // Left fish (crescent opens right)
        moveTo(s * 0.22f, s * 0.22f)
        cubicTo(s * 0.06f, s * 0.36f, s * 0.06f, s * 0.64f, s * 0.22f, s * 0.78f)
        // Right fish (crescent opens left)
        moveTo(s * 0.78f, s * 0.22f)
        cubicTo(s * 0.94f, s * 0.36f, s * 0.94f, s * 0.64f, s * 0.78f, s * 0.78f)
        // Tie-bar
        moveTo(s * 0.18f, cy)
        lineTo(s * 0.82f, cy)
    }
    drawPath(path, brush, style = style)
}

/** Fallback star for unknown signs. */
private fun DrawScope.drawStar(brush: Brush, style: Stroke, s: Float) {
    val cx = s / 2f
    val cy = s / 2f
    val outer = s * 0.38f
    val inner = s * 0.16f
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val angle = (-PI / 2 + i * PI / 5).toFloat()
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, brush, style = style)
}
