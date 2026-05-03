package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.BorderGlass
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.Space
import com.hkgroups.agecalculator.ui.theme.SurfaceGlass
import kotlinx.coroutines.delay

/**
 * CosmicTopBar — single source of truth for screen headers.
 * Replaces the half-dozen ad-hoc top bars across the app so every sub-screen
 * has identical alignment, height, and back-affordance.
 *
 * @param title screen title
 * @param subtitle optional one-line subtitle below the title
 * @param onBack tap handler for the back arrow; null hides the arrow (root screens)
 * @param trailing optional trailing action (e.g. settings, share)
 */
@Composable
fun CosmicTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.xs, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        } else {
            Spacer(Modifier.width(Space.md))
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = if (onBack != null) 0.dp else 0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1
                )
            }
        }
        if (trailing != null) {
            trailing()
            Spacer(Modifier.width(Space.xs))
        } else {
            Spacer(Modifier.width(Space.md))
        }
    }
}

/**
 * SectionHeader — small uppercase eyebrow + larger title on the next line.
 * Gives every list/grid section the same rhythm so screens read as one design.
 *
 * @param eyebrow tiny uppercase label above the title
 * @param title primary section title
 * @param trailing optional trailing slot (e.g. "View all" link)
 */
@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.xxs, vertical = Space.xxs),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SaturnGold,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (trailing != null) trailing()
    }
}

/**
 * AnimatedCounter — counts a numeric target up from 0 on first composition.
 * Sets the dashboard apart from a "value just appears" screen — the counter
 * gives the cosmic age a sense of arrival.
 *
 * @param target final value to count up to
 * @param durationMillis total ramp duration; tune up for hero numbers
 * @param style text style for the digits
 * @param color text color
 * @param suffix string appended after the digits (e.g. "%")
 */
@Composable
fun AnimatedCounter(
    target: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 900,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = Color.White,
    suffix: String = ""
) {
    var displayValue by rememberSaveable(target) { mutableIntStateOf(0) }

    LaunchedEffect(target) {
        if (target <= 0) {
            displayValue = target
            return@LaunchedEffect
        }
        val frames = 28
        val frameDelay = (durationMillis / frames).coerceAtLeast(16).toLong()
        repeat(frames) { i ->
            // Ease out for a satisfying settle at the end.
            val t = (i + 1) / frames.toFloat()
            val eased = 1f - (1f - t) * (1f - t)
            displayValue = (target * eased).toInt().coerceAtMost(target)
            delay(frameDelay)
        }
        displayValue = target
    }

    Text(
        text = "$displayValue$suffix",
        style = style,
        color = color,
        modifier = modifier
    )
}

/**
 * GradientBorderRing — circular avatar holder with an animated rotating
 * gradient border. The continuous rotation is subtle (~14s per turn) so it
 * reads as a halo, not a loading spinner.
 *
 * @param size outer ring diameter
 * @param strokeWidth border thickness
 * @param colors gradient stops; default is the cosmic neon→purple→gold sweep
 * @param content slotted inside the ring (avatar emoji, glyph, etc.)
 */
@Composable
fun GradientBorderRing(
    modifier: Modifier = Modifier,
    size: Dp = 132.dp,
    strokeWidth: Dp = 2.dp,
    colors: List<Color> = listOf(
        PrimaryNeon,
        PurpleAccent,
        SaturnGold,
        PrimaryNeon
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "gradientBorderRing")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val brush = ShaderBrush(
                SweepGradientShader(
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                    colors = colors,
                    colorStops = null
                )
            )
            val stroke = strokeWidth.toPx()
            drawArc(
                brush = brush,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke)
            )
        }
        Box(
            modifier = Modifier
                .size(size - (strokeWidth * 4))
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * CurvedGauge — semicircular arc that fills from left to right based on
 * progress 0f..1f. Unlike a flat bar, the arc gives a "dial" feel suited to
 * lifetime / journey indicators on the profile screen.
 *
 * @param progress 0f..1f
 * @param trackColor unfilled track color
 * @param fillColors gradient stops for the filled arc
 */
@Composable
fun CurvedGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    fillColors: List<Color> = listOf(PrimaryNeon, PurpleAccent, SaturnGold)
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "curvedGaugeFill"
    )

    Canvas(modifier = modifier) {
        val stroke = 14.dp.toPx()
        val pad = stroke / 2f
        // Use a square frame for the arc to avoid an oval sweep.
        val arcSide = kotlin.math.min(this.size.width, this.size.height * 2f) - stroke
        val left = (this.size.width - arcSide) / 2f
        val top = (this.size.height - arcSide / 2f) - pad

        // Track
        drawArc(
            color = trackColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = Size(arcSide, arcSide),
            style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        // Fill
        if (animated > 0f) {
            val brush = Brush.horizontalGradient(
                colors = fillColors,
                startX = left,
                endX = left + arcSide
            )
            drawArc(
                brush = brush,
                startAngle = 180f,
                sweepAngle = 180f * animated,
                useCenter = false,
                topLeft = Offset(left, top),
                size = Size(arcSide, arcSide),
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

/**
 * ScoreRing — compact circular gauge for showing a 0..100 score in lists.
 * Used on each compatibility row to give the rating a richer, more glanceable
 * visual than a star string.
 */
@Composable
fun ScoreRing(
    percent: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val target = (percent.coerceIn(0, 100)) / 100f
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scoreRingFill"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val pad = stroke / 2f
            // Track
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Fill
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        Text(
            text = "$percent",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * TimelineRow — vertical timeline row with a gradient dot + connecting line.
 * Caller is responsible for rendering the row's content (typically a card).
 *
 * @param isFirst hides the line above the dot
 * @param isLast hides the line below the dot
 * @param dotColor color of the dot and line
 */
@Composable
fun TimelineRow(
    isFirst: Boolean = false,
    isLast: Boolean = false,
    dotColor: Color = PrimaryNeon,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Rail column — 24dp wide, dot in the middle, line stretches above/below
        Column(
            modifier = Modifier
                .width(24.dp)
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top line segment
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (isFirst) 0.dp else 14.dp)
                    .background(dotColor.copy(alpha = 0.35f))
            )
            // Dot with halo
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            // Bottom line segment fills remaining height
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(dotColor.copy(alpha = 0.35f))
                )
            }
        }
        Spacer(Modifier.width(Space.sm))
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/**
 * IconChip — circular glass icon button. Used as trailing actions in
 * CosmicTopBar (settings, share) and as small affordances elsewhere.
 */
@Composable
fun IconChip(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceGlass)
            .border(1.dp, BorderGlass, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accent),
                onClick = onClick
            )
            .pressableScale(interactionSource),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent.copy(alpha = 0.85f),
            modifier = Modifier.size(18.dp)
        )
    }
}
