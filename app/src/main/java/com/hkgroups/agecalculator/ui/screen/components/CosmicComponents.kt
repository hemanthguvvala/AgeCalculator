package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hkgroups.agecalculator.ui.theme.BorderGlass
import com.hkgroups.agecalculator.ui.theme.SurfaceGlass

/**
 * GlassCard - A reusable glassmorphic card component with blur effect
 * Matches the CSS .glass-card aesthetic
 * 
 * @param modifier Custom modifier for positioning and sizing
 * @param shape Shape of the card (default: rounded corners)
 * @param blur Amount of blur to apply (only on Android 12+)
 * @param borderWidth Width of the glass border
 * @param content Composable content to display inside the card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    blur: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = SurfaceGlass,
                shape = shape
            )
            .border(
                width = borderWidth,
                color = BorderGlass,
                shape = shape
            )
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
            .width(160.dp)
            .height(200.dp),
        glowColor = planetColor,
        glowAlpha = 0.2f,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Planet icon/image at the top
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(planetColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                planetImage()
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Planet info at the bottom
            Column {
                Text(
                    text = planetName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = planetColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = planetAge,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = "Years",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
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
            .height(72.dp)
            .padding(horizontal = 16.dp),
        shape = CircleShape,
        blur = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
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
 * NavBarItem - animated, haptic, ripple-aware nav item.
 * - Selection animates: pill background fades in/out and icon scales up subtly.
 * - Haptic tick on tap so the bar feels physical.
 * - Branded ripple replaces the previously-disabled feedback.
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
    val primary = MaterialTheme.colorScheme.primary

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) primary.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "navPillColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) primary else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "navIconTint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navIconScale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(pillColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = primary),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier
                .size(28.dp)
                .scale(iconScale)
        )
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
