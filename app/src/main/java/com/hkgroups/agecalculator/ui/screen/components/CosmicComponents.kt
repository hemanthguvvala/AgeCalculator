package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.BorderGlass
import com.hkgroups.agecalculator.ui.theme.BorderGlassBottom
import com.hkgroups.agecalculator.ui.theme.BorderGlassTop
import com.hkgroups.agecalculator.ui.theme.GlassBase
import com.hkgroups.agecalculator.ui.theme.GlassBaseTop
import com.hkgroups.agecalculator.ui.theme.GlassRimLight
import com.hkgroups.agecalculator.ui.theme.GlassSpecular
import com.hkgroups.agecalculator.ui.theme.SurfaceGlass
import com.hkgroups.agecalculator.ui.theme.SurfaceGlassBottom
import com.hkgroups.agecalculator.ui.theme.SurfaceGlassMid
import com.hkgroups.agecalculator.ui.theme.SurfaceGlassTop

/**
 * GlassCard — solid-base premium glass surface.
 *
 * The card body is fully opaque (GlassBase dark blue-purple) so empty
 * regions never read as a hole. The "glass" feel comes from styling painted
 * on top of the solid base:
 *   1. Vertical gradient highlight in the top 30% (the "lit" zone)
 *   2. Specular hotspot in the top-left, faking a directional light source
 *   3. 1px inner top rim light — the bright pixel that makes it read as glass
 *   4. Gradient border (brighter top, softer bottom)
 *
 * All decorative drawing is done with `drawWithCache` so brushes are
 * allocated once per size change rather than per frame.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    blur: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceBrush = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to GlassBaseTop,
                0.30f to GlassBase,
                1.0f to GlassBase
            )
        )
    }
    val borderBrush = remember {
        Brush.verticalGradient(
            colors = listOf(BorderGlassTop, BorderGlassBottom)
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(surfaceBrush)
            .drawWithCache {
                val w = this.size.width
                val h = this.size.height
                val specCenter = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.15f)
                val specRadius = w.coerceAtMost(h * 1.4f) * 0.55f
                val specularBrush = Brush.radialGradient(
                    colors = listOf(GlassSpecular, Color.Transparent),
                    center = specCenter,
                    radius = specRadius
                )
                val rimBrush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.18f to GlassRimLight,
                        0.5f to GlassRimLight.copy(alpha = 0.55f),
                        0.82f to GlassRimLight,
                        1.0f to Color.Transparent
                    )
                )
                val rimY = 1.dp.toPx()
                val rimStartX = w * 0.06f
                val rimEndX = w * 0.94f
                val rimStrokePx = 1.dp.toPx()

                onDrawBehind {
                    drawCircle(
                        brush = specularBrush,
                        radius = specRadius,
                        center = specCenter
                    )
                    drawLine(
                        brush = rimBrush,
                        start = androidx.compose.ui.geometry.Offset(rimStartX, rimY),
                        end = androidx.compose.ui.geometry.Offset(rimEndX, rimY),
                        strokeWidth = rimStrokePx
                    )
                }
            }
            .border(width = borderWidth, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}

/**
 * GlassCardWithGlow - Glass card with a real soft halo glow.
 * Uses Modifier.shadow with a colored spot/ambient — produces actual outer glow,
 * not just a flat overlay.
 */
@Composable
fun GlassCardWithGlow(
    modifier: Modifier = Modifier,
    glowColor: Color = Color.Transparent,
    glowAlpha: Float = 0.6f,
    elevation: Dp = 24.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val tinted = if (glowColor == Color.Transparent) Color.Transparent
                 else glowColor.copy(alpha = glowAlpha.coerceIn(0f, 1f))

    GlassCard(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = tinted,
            spotColor = tinted,
            clip = false
        ),
        shape = shape,
        content = content
    )
}

/**
 * StatCard - A compact glass card for displaying statistics
 * Used in the age ticker and cosmic stats sections
 * 
 * @param label The label text (e.g., "Years", "Days")
 * @param value The value to display
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}

/**
 * PlanetCard - A specialized card for displaying planetary information
 * 
 * @param planetName Name of the planet
 * @param planetAge Age on that planet
 * @param planetColor Specific color accent for the planet
 * @param planetImage Composable for the planet image/icon
 */
@Composable
fun PlanetCard(
    planetName: String,
    planetAge: String,
    planetColor: Color,
    modifier: Modifier = Modifier,
    planetImage: @Composable () -> Unit
) {
    GlassCardWithGlow(
        modifier = modifier
            .width(140.dp)
            .height(180.dp),
        glowColor = planetColor,
        glowAlpha = 0.18f,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                planetColor.copy(alpha = 0.4f),
                                planetColor.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                planetImage()
            }

            Column {
                Text(
                    text = planetName,
                    style = MaterialTheme.typography.labelMedium,
                    color = planetColor,
                    letterSpacing = 1.5.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = planetAge,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "yr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * FloatingNavBar - The floating pill-shaped navigation bar
 * Matches the bottom navigation design from the mockup
 * 
 * @param items List of navigation items (icon + action)
 * @param selectedIndex Currently selected item index
 * @param onItemSelected Callback when an item is selected
 */
@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .height(68.dp)
            .padding(horizontal = 16.dp),
        shape = CircleShape,
        blur = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                NavBarItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = index == selectedIndex,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

/**
 * NavBarItem — animated, haptic, ripple-aware nav item.
 * - Selection animates: pill background fades in/out and icon scales up subtly.
 * - Haptic tick on tap so the bar feels physical.
 * - Branded ripple keyed off the user's sign palette.
 */
@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val cosmicFeedback = com.hkgroups.agecalculator.util.LocalCosmicFeedback.current
    val primary = MaterialTheme.colorScheme.primary

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) primary.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "navPillColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) primary else Color.White.copy(alpha = 0.55f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "navIconTint"
    )

    // Selected pill expands to show the label inline; unselected stays as a dot.
    val targetWidth = if (isSelected) 96.dp else 48.dp
    val pillWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navPillWidth"
    )

    Row(
        modifier = Modifier
            .height(44.dp)
            .width(pillWidth)
            .clip(CircleShape)
            .background(pillColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = primary),
                onClick = {
                    cosmicFeedback?.fire(com.hkgroups.agecalculator.util.CosmicFeedback.Cue.Select)
                        ?: haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        if (isSelected) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Data class for navigation items
 */
data class NavItem(
    val icon: ImageVector,
    val label: String
)

/**
 * CosmicProgressBar — gradient progress with a smooth fill animation and a
 * subtle moving shimmer that runs across the filled portion. Static bars feel
 * dead; a shimmer makes progress feel alive.
 *
 * @param progress 0f..1f
 * @param animated when true, the fill animates and a shimmer sweeps across it
 */
@Composable
fun CosmicProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    brush: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4D96FF),
            Color(0xFF9B59B6)
        )
    )
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cosmicProgressFill"
    )

    val transition = rememberInfiniteTransition(label = "cosmicProgressShimmer")
    val shimmerAlpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 0.55f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cosmicProgressShimmerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        ) {
            if (animated && animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White.copy(alpha = shimmerAlpha),
                                    Color.White.copy(alpha = 0f)
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * StatsGrid - Two-column grid for displaying cosmic stats
 * Used in profile screen for zodiac sign, moon phase, etc.
 */
@Composable
fun StatsGrid(
    items: List<StatsItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    StatsGridItem(
                        icon = item.icon,
                        label = item.label,
                        value = item.value,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of items
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatsGridItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

data class StatsItem(
    val icon: String,
    val label: String,
    val value: String
)

/**
 * pressableScale — gives any composable a tactile press animation.
 * Scales down to ~0.96 on press with a springy release. Combine with .clickable
 * BEFORE this modifier so the interaction source feeds the scale state.
 *
 * Usage: Modifier.clickable(interactionSource, ripple(...)) { ... }.pressableScale(interactionSource)
 */
fun Modifier.pressableScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressableScale"
    )
    this.scale(scale)
}

/**
 * attentionPulse — gentle breathing animation for cards that should draw the eye
 * (e.g. "Tap to reveal today's reading"). Subtle on purpose; never feels noisy.
 */
fun Modifier.attentionPulse(
    minScale: Float = 1f,
    maxScale: Float = 1.025f,
    durationMillis: Int = 1800
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "attentionPulse")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "attentionPulseScale"
    )
    this.scale(scale)
}
