package com.hkgroups.agecalculator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Design tokens. The whole app should reach for these instead of inline magic
 * numbers, so spacing and corner radii stay consistent across screens.
 */

object Space {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object Radius {
    val sm = 12.dp
    val md = 20.dp
    val lg = 28.dp
    val xl = 36.dp
    val pill = 999.dp
}

val ShapeSm = RoundedCornerShape(Radius.sm)
val ShapeMd = RoundedCornerShape(Radius.md)
val ShapeLg = RoundedCornerShape(Radius.lg)
val ShapeXl = RoundedCornerShape(Radius.xl)

/**
 * Motion durations. Use named values so animations stay in sync — `quick` for
 * tap feedback, `standard` for state changes, `slow` for scene transitions.
 */
object Motion {
    const val Quick = 180
    const val Standard = 320
    const val Slow = 560
    const val Hero = 900
}
