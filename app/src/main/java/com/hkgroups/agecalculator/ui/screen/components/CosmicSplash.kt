package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CosmicSplash — animated launch sequence shown after the system splash.
 *
 * Sequence (~1.6s total):
 *  - 0ms: black screen with center logo at scale 0
 *  - 200ms: gradient ring expands + zodiac glyph fades in inside it
 *  - 700ms: "ZODIAC AGE" wordmark slides up from below
 *  - 1100ms: subtitle fades in
 *  - 1400ms: whole screen fades out, [onComplete] fires
 *
 * Sign-tinted via [LocalSignPalette] so the splash already feels personal
 * for returning users.
 *
 * @param signName user's sun sign (or null for first-launch fallback)
 * @param onComplete called once the fade-out finishes
 */
@Composable
fun CosmicSplash(
    signName: String?,
    onComplete: () -> Unit
) {
    val palette = LocalSignPalette.current
    val ringScale = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }
    val wordmarkOffset = remember { Animatable(40f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val rootAlpha = remember { Animatable(1f) }

    // Slow rotation of the ring brush during display.
    val ringRotation by rememberInfiniteTransition(label = "splashRingRot")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "splashRingRotation"
        )

    LaunchedEffect(Unit) {
        // 200ms gate, then ring + glyph appear with spring
        delay(200)
        kotlinx.coroutines.coroutineScope {
            launch {
                ringScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
            }
            launch {
                ringAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }
        }
        delay(500)
        kotlinx.coroutines.coroutineScope {
            launch {
                wordmarkOffset.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
            }
            launch {
                wordmarkAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
            }
        }
        delay(380)
        subtitleAlpha.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        delay(450)
        rootAlpha.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .graphicsLayer { alpha = rootAlpha.value },
        contentAlignment = Alignment.Center
    ) {
        // Sign-tinted halo behind the ring
        Box(
            modifier = Modifier
                .size(360.dp)
                .graphicsLayer {
                    val s = (ringScale.value * 0.7f + 0.3f)
                    scaleX = s
                    scaleY = s
                    alpha = ringAlpha.value * 0.9f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0.45f),
                            palette.secondary.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gradient ring + drawn zodiac glyph
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = ringScale.value
                        scaleY = ringScale.value
                        alpha = ringAlpha.value
                        rotationZ = ringRotation * 0.05f
                    },
                contentAlignment = Alignment.Center
            ) {
                GradientBorderRing(
                    size = 168.dp,
                    strokeWidth = 2.dp,
                    colors = listOf(
                        palette.primary,
                        palette.secondary,
                        palette.primary
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1A1F36),
                                        Color(0xFF06080F)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ZodiacGlyph(
                            sign = signName ?: "",
                            strokeColor = Color.White,
                            accentColor = palette.primary,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Wordmark
            Text(
                text = "ZODIAC AGE",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                ),
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = wordmarkAlpha.value
                    translationY = wordmarkOffset.value * density
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your cosmic identity",
                style = MaterialTheme.typography.labelLarge,
                color = palette.primary.copy(alpha = 0.85f),
                modifier = Modifier.graphicsLayer { alpha = subtitleAlpha.value }
            )
        }
    }
}
