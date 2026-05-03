package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.VenusGold
import kotlinx.coroutines.delay

/**
 * Engagement-loop components.
 *
 * These are stateless, drop-in composables for the daily-ritual features
 * (reveal, streak, lucky number/color, mood, missions, share card).
 * They take all data as parameters and emit clicks via callbacks — wire
 * them to ViewModels later.
 */

// ---------- Daily reveal ----------

/**
 * DailyRevealCard — the centerpiece of the daily ritual.
 * Hidden state: blurred placeholder text + "Tap to reveal" affordance.
 * Revealed state: crisp horoscope with a soft glow.
 *
 * @param horoscope today's horoscope text (may be empty before load)
 * @param revealed whether the user has tapped to reveal today's reading
 * @param onReveal called the first time the card is tapped while hidden
 */
@Composable
fun DailyRevealCard(
    horoscope: String,
    revealed: Boolean,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Today's Cosmic Forecast"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pulseModifier = if (revealed) Modifier else Modifier.attentionPulse()

    GlassCardWithGlow(
        modifier = modifier
            .fillMaxWidth()
            .then(pulseModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PrimaryNeon),
                enabled = !revealed,
                onClick = onReveal
            )
            .pressableScale(interactionSource),
        glowColor = PrimaryNeon,
        glowAlpha = if (revealed) 0.5f else 0.25f,
        elevation = 28.dp,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (revealed) "🌙" else "🔮",
                    fontSize = 22.sp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon
                )
            }
            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = revealed,
                transitionSpec = {
                    (fadeIn(tween(600)) togetherWith fadeOut(tween(300)))
                },
                label = "horoscopeReveal"
            ) { isRevealed ->
                if (isRevealed) {
                    Text(
                        text = horoscope.ifBlank { "Loading your reading..." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                } else {
                    // Inviting hint instead of a blurred wall of text — the
                    // previous blurred copy rendered as a dark void on glass.
                    Text(
                        text = "The cosmos has prepared today's reading just for you.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }

            if (!revealed) {
                Spacer(Modifier.height(20.dp))
                // Bright, contrast-rich CTA pill so the affordance is obvious.
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = SaturnGold.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "✦  TAP TO REVEAL  ✦",
                            style = MaterialTheme.typography.labelMedium,
                            color = SaturnGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ---------- Streak ----------

/**
 * StreakPill — small header pill showing the current daily-open streak.
 * Switches to a gold gradient border at 7+ days to reward consistency.
 */
@Composable
fun StreakPill(
    days: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isHotStreak = days >= 7
    val borderColor by animateColorAsState(
        targetValue = if (isHotStreak) SaturnGold else Color.White.copy(alpha = 0.18f),
        animationSpec = tween(400),
        label = "streakBorder"
    )
    val flameColor by animateColorAsState(
        targetValue = if (isHotStreak) VenusGold else Color.White.copy(alpha = 0.85f),
        animationSpec = tween(400),
        label = "streakFlame"
    )

    val transition = rememberInfiniteTransition(label = "streakFlameAnim")
    val flameScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isHotStreak) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streakFlameScale"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val baseModifier = modifier
        .height(36.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.06f))
        .border(
            width = 1.dp,
            color = borderColor,
            shape = CircleShape
        )

    val finalModifier = if (onClick != null) {
        baseModifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = SaturnGold),
                onClick = onClick
            )
            .pressableScale(interactionSource)
    } else baseModifier

    Row(
        modifier = finalModifier.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            fontSize = 16.sp,
            modifier = Modifier
                .alpha(if (days > 0) 1f else 0.4f)
                .scale(if (isHotStreak) flameScale else 1f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (days > 0) "$days-day streak" else "Start a streak",
            style = MaterialTheme.typography.labelMedium,
            color = flameColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------- Lucky number ----------

/**
 * LuckyNumberCard — slot-machine number reveal.
 * On first composition the digits roll for ~0.8s before settling on the target.
 */
@Composable
fun LuckyNumberCard(
    luckyNumber: Int,
    modifier: Modifier = Modifier
) {
    var displayValue by rememberSaveable(luckyNumber) { mutableIntStateOf(0) }

    LaunchedEffect(luckyNumber) {
        val frames = 14
        repeat(frames) { i ->
            displayValue = (Math.random() * 99).toInt()
            delay((30 + i * 8).toLong())
        }
        displayValue = luckyNumber
    }

    GlassCardWithGlow(
        modifier = modifier
            .height(140.dp),
        glowColor = SaturnGold,
        glowAlpha = 0.35f,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LUCKY NUMBER",
                style = MaterialTheme.typography.labelSmall,
                color = SaturnGold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = displayValue.toString().padStart(2, '0'),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

// ---------- Lucky color ----------

/**
 * LuckyColorCard — large color swatch + name. Tap fills the card briefly.
 */
@Composable
fun LuckyColorCard(
    colorName: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    GlassCardWithGlow(
        modifier = modifier
            .height(140.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = color),
                onClick = {}
            )
            .pressableScale(interactionSource),
        glowColor = color,
        glowAlpha = 0.5f,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LUCKY COLOR",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(color, color.copy(alpha = 0.6f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = colorName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

// ---------- Mood ----------

/**
 * MoodCard — horizontal banner: big emoji + mood label + 1-line micro-description.
 */
@Composable
fun MoodCard(
    emoji: String,
    moodName: String,
    description: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 32.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "MOOD OF THE DAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = PurpleAccent
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = moodName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

// ---------- Missions ----------

enum class MissionState { Locked, Active, Complete }

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val current: Int,
    val target: Int,
    val xpReward: Int,
    val state: MissionState
)

@Composable
fun MissionCard(
    mission: Mission,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isComplete = mission.state == MissionState.Complete
    val isLocked = mission.state == MissionState.Locked

    val borderColor by animateColorAsState(
        targetValue = when (mission.state) {
            MissionState.Complete -> SaturnGold
            MissionState.Active -> PrimaryNeon.copy(alpha = 0.4f)
            MissionState.Locked -> Color.White.copy(alpha = 0.08f)
        },
        animationSpec = tween(500),
        label = "missionBorder"
    )

    val cardAlpha = if (isLocked) 0.55f else 1f

    val baseClick = if (onClick != null && !isLocked) {
        Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = PrimaryNeon),
                onClick = onClick
            )
            .pressableScale(interactionSource)
    } else Modifier

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .border(
                width = if (isComplete) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .then(baseClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isComplete) SaturnGold.copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isLocked -> "🔒"
                        isComplete -> "✓"
                        else -> mission.emoji
                    },
                    fontSize = 22.sp
                )
            }
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = mission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1
                )
                if (mission.state == MissionState.Active && mission.target > 0) {
                    Spacer(Modifier.height(8.dp))
                    CosmicProgressBar(
                        progress = mission.current.toFloat() / mission.target.toFloat()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${mission.current} / ${mission.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))
            XpBadge(xp = mission.xpReward, highlighted = isComplete)
        }
    }
}

@Composable
private fun XpBadge(xp: Int, highlighted: Boolean) {
    val bg = if (highlighted) SaturnGold.copy(alpha = 0.25f)
             else Color.White.copy(alpha = 0.08f)
    val fg = if (highlighted) SaturnGold else Color.White.copy(alpha = 0.7f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "+$xp XP",
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------- Share card ----------

data class ShareablePlanetAge(
    val planetEmoji: String,
    val planetName: String,
    val age: String
)

/**
 * CosmicShareCard — a 9:16-friendly vertical card designed to be rendered
 * to a Bitmap and shared. Self-contained: pass everything in, no theme
 * dependencies beyond the cosmic palette.
 *
 * Use Modifier.fillMaxWidth() + Modifier.aspectRatio(9f/16f) when capturing.
 */
@Composable
fun CosmicShareCard(
    userName: String,
    zodiacSymbol: String,
    zodiacName: String,
    earthAge: String,
    planetAges: List<ShareablePlanetAge>,
    modifier: Modifier = Modifier,
    appName: String = "ZodaicAge"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0F2C),
                        Color(0xFF1B1140),
                        Color(0xFF050B14)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "✨ MY COSMIC IDENTITY ✨",
                style = MaterialTheme.typography.labelMedium,
                color = SaturnGold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryNeon.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = zodiacSymbol, fontSize = 56.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = zodiacName,
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryNeon
            )
            Text(
                text = "$earthAge on Earth",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            Spacer(Modifier.height(20.dp))

            Text(
                text = "MY AGE ACROSS THE COSMOS",
                style = MaterialTheme.typography.labelSmall,
                color = SaturnGold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(14.dp))

            planetAges.take(6).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = row.planetEmoji, fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = row.planetName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.age,
                        style = MaterialTheme.typography.titleSmall,
                        color = SaturnGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = "Discover yours →",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = appName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ---------- Compatibility result ----------

/**
 * CompatibilityResultHero — animated number-counts-up score with two
 * zodiac symbols flanking it. Drop into a result screen after the loading
 * "aligning the cosmos" sequence.
 */
@Composable
fun CompatibilityResultHero(
    yourSymbol: String,
    yourSign: String,
    partnerSymbol: String,
    partnerSign: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    // Cinematic reveal sequence:
    //  - 0ms: orbs start ~120dp off-screen on opposite sides, faded
    //  - 120ms: they slide toward center
    //  - 480ms: connector pops in with a spring
    //  - 600ms: score number tumbles up
    //  - 1300ms: verdict label fades in
    var stage by remember(score) { androidx.compose.runtime.mutableIntStateOf(0) }
    LaunchedEffect(score) {
        stage = 0
        delay(120); stage = 1   // orbs slide in
        delay(360); stage = 2   // connector + score
        delay(700); stage = 3   // verdict
    }

    val orbOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (stage >= 1) 0.dp else 80.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "orbSlide"
    )
    val orbAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 360, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "orbAlpha"
    )
    val connectorScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "connectorScale"
    )

    var displayScore by rememberSaveable(score) { mutableIntStateOf(0) }
    LaunchedEffect(score, stage) {
        if (stage < 2) return@LaunchedEffect
        val steps = 32
        repeat(steps) { i ->
            val t = (i + 1) / steps.toFloat()
            // Cubic ease-out so the number lands cinematically.
            val eased = 1f - (1f - t) * (1f - t) * (1f - t)
            displayScore = (score * eased).toInt().coerceAtMost(score)
            delay(28L)
        }
        displayScore = score
    }

    val accent = when {
        score >= 80 -> SaturnGold
        score >= 50 -> PrimaryNeon
        else -> PurpleAccent
    }
    val verdict = when {
        score >= 90 -> "Cosmic Soulmates"
        score >= 75 -> "Deep Resonance"
        score >= 60 -> "Strong Alignment"
        score >= 40 -> "Worth Exploring"
        else -> "Different Orbits"
    }

    // Sparkle burst when the score lands.
    val emitter = rememberParticleEmitter()
    LaunchedEffect(stage) {
        if (stage == 2) {
            emitter.burst(0.5f, 0.5f, count = 28, colors = listOf(accent, Color.White), speed = 0.6f)
        }
    }

    GlassCardWithGlow(
        modifier = modifier.fillMaxWidth(),
        glowColor = accent,
        glowAlpha = 0.5f,
        elevation = 32.dp,
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = -orbOffset.toPx()
                        alpha = orbAlpha
                    }
                ) {
                    ZodiacBubble(symbol = yourSymbol, label = yourSign, accent = accent)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .graphicsLayer {
                            scaleX = connectorScale
                            scaleY = connectorScale
                        }
                ) {
                    Text(
                        text = if (score >= 70) "💕" else if (score >= 40) "🌙" else "⚡",
                        fontSize = 32.sp,
                        modifier = Modifier.attentionPulse(maxScale = 1.10f, durationMillis = 1100)
                    )
                }
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = orbOffset.toPx()
                        alpha = orbAlpha
                    }
                ) {
                    ZodiacBubble(symbol = partnerSymbol, label = partnerSign, accent = accent)
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = if (stage >= 2) 1f else 0f
                }
            ) {
                Text(
                    text = "$displayScore%",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.displayLarge
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = stage >= 3,
                enter = androidx.compose.animation.fadeIn(animationSpec = tween(420)) +
                    androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(420)
                    )
            ) {
                Text(
                    text = verdict.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        // Particle overlay sits on top of the hero so the score-reveal sparkle
        // appears to explode out from the number.
        ParticleField(
            emitter = emitter,
            modifier = Modifier.matchParentSize()
        )
        }
    }
}

@Composable
private fun ZodiacBubble(symbol: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = symbol, fontSize = 36.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
