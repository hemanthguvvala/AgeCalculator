package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * staggeredEntrance — fades + slides a card up on first appearance.
 *
 * Drop on any composable that should "land" into view. Each call site gets
 * its own spring instance, so when used inside a list with `key()` the cards
 * naturally stagger as they enter the composition tree.
 *
 * @param indexHint optional position hint (0..N) so list items stagger in
 *                  reading order. Pass null for non-list contexts.
 * @param baseDelayMs delay applied per indexHint step
 */
fun Modifier.staggeredEntrance(
    indexHint: Int? = null,
    baseDelayMs: Int = 28
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        if (indexHint != null) delay((indexHint * baseDelayMs).toLong())
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        if (indexHint != null) delay((indexHint * baseDelayMs).toLong())
        translateY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    this.graphicsLayer {
        this.alpha = alpha.value
        translationY = translateY.value * density
    }
}

/**
 * tiltable3D — gives a card subtle 3D tilt when pressed.
 *
 * Tracks the press position relative to the card center, then applies
 * rotationX/rotationY via graphicsLayer so the card visually leans toward
 * the press point. Releases back to flat with a spring.
 *
 * @param maxAngle maximum tilt in degrees at the corners
 * @param onTap optional click handler — keeps the gesture detector single-purpose
 */
fun Modifier.tiltable3D(
    maxAngle: Float = 8f,
    onTap: (() -> Unit)? = null
): Modifier = composed {
    var sizeWidth by remember { mutableFloatStateOf(1f) }
    var sizeHeight by remember { mutableFloatStateOf(1f) }
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    this
        .graphicsLayer {
            // Centerd transform so tilt feels like the card pivots around its middle.
            transformOrigin = TransformOrigin(0.5f, 0.5f)
            rotationX = rotX.value
            rotationY = rotY.value
            scaleX = scale.value
            scaleY = scale.value
            cameraDistance = 14f * density
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    sizeWidth = size.width.toFloat()
                    sizeHeight = size.height.toFloat()
                    val nx = ((offset.x / sizeWidth) - 0.5f) * 2f   // -1..1
                    val ny = ((offset.y / sizeHeight) - 0.5f) * 2f // -1..1
                    // rotationX is positive when tilting top-toward-camera; we
                    // want top to lift away from press, so negate ny.
                    rotY.snapTo(nx * maxAngle)
                    rotX.snapTo(-ny * maxAngle)
                    scale.snapTo(0.97f)

                    val released = tryAwaitRelease()

                    val springSpec = spring<Float>(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                    rotY.animateTo(0f, springSpec)
                    rotX.animateTo(0f, springSpec)
                    scale.animateTo(1f, springSpec)

                    if (released) onTap?.invoke()
                }
            )
        }
}

/**
 * TumblingDigit — vertical "slot machine" digit that animates from previous to
 * target value. Used by [TumblingCounter].
 */
@Composable
private fun TumblingDigit(
    target: Int,
    style: TextStyle,
    color: Color,
    durationMillis: Int
) {
    var prev by remember { mutableIntStateOf(target) }
    val translation = remember { Animatable(0f) }
    val density = LocalDensity.current

    val lineHeightPx = with(density) { style.lineHeight.toPx() }
    val cellHeightPx = if (lineHeightPx > 0f) lineHeightPx else with(density) { 96.dp.toPx() }

    LaunchedEffect(target) {
        if (target == prev) return@LaunchedEffect
        translation.snapTo(0f)
        translation.animateTo(
            targetValue = -cellHeightPx,
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        )
        prev = target
        translation.snapTo(0f)
    }

    Box(
        modifier = Modifier
            .height(with(density) { cellHeightPx.toDp() })
            .graphicsLayer { clip = true }
    ) {
        Box(
            modifier = Modifier.graphicsLayer { translationY = translation.value }
        ) {
            Text(text = prev.toString(), style = style, color = color)
            Box(
                modifier = Modifier.graphicsLayer { translationY = cellHeightPx }
            ) {
                Text(text = target.toString(), style = style, color = color)
            }
        }
    }
}

/**
 * TumblingCounter — counts up to `target` with rolling-digit animation.
 *
 * Each digit slot tumbles from 0→target, starting from the most-significant
 * digit so the eye reads the number "settling" rather than racing through
 * thousands of intermediate values like a stopwatch.
 *
 * @param target final value
 * @param durationMillis total ramp duration across all digits
 * @param style text style for digits
 * @param color digit color
 * @param suffix string appended after the digits
 */
@Composable
fun TumblingCounter(
    target: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 1200,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = Color.White,
    suffix: String = ""
) {
    var current by rememberSaveable(target) { mutableIntStateOf(0) }

    LaunchedEffect(target) {
        if (target <= 0) {
            current = target
            return@LaunchedEffect
        }
        val frames = 36
        val frameDelay = (durationMillis / frames).coerceAtLeast(20).toLong()
        repeat(frames) { i ->
            val t = (i + 1) / frames.toFloat()
            // Cubic ease-out so digits settle, not race.
            val eased = 1f - (1f - t) * (1f - t) * (1f - t)
            current = (target * eased).toInt().coerceAtMost(target)
            delay(frameDelay)
        }
        current = target
    }

    Box(modifier = modifier) {
        Text(
            text = "$current$suffix",
            style = style,
            color = color
        )
    }
}
