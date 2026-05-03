package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette

/**
 * OnboardingStep — one screen of the onboarding sequence.
 */
private data class OnboardingStep(
    val emoji: String,
    val title: String,
    val body: String
)

private val OnboardingSteps = listOf(
    OnboardingStep(
        emoji = "🔮",
        title = "Your daily reading",
        body = "Tap the cosmic forecast card each day to reveal what the stars say. Swipe sideways for Mood, Love, Energy, and Focus."
    ),
    OnboardingStep(
        emoji = "🔥",
        title = "Build your streak",
        body = "Open the app every day to grow your cosmic streak. 7, 30, 100 days each unlock a celebration."
    ),
    OnboardingStep(
        emoji = "💞",
        title = "Match with anyone",
        body = "Explore compatibility between your sign and any of the 12. Each pairing has its own radar and reading."
    ),
    OnboardingStep(
        emoji = "✨",
        title = "Make it yours",
        body = "Your sun sign tints the entire app. Tap the avatar in the top-right to see your cosmic profile."
    )
)

/**
 * OnboardingOverlay — first-run tutorial. Translucent dim background +
 * sign-tinted glass card stepping through 4 features. Dismisses on the
 * final "Got it" button or on skip.
 *
 * Should be shown only when [SettingsRepository.onboardingCompleted] is false.
 */
@Composable
fun OnboardingOverlay(
    onComplete: () -> Unit
) {
    val palette = LocalSignPalette.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    val total = OnboardingSteps.size
    val current = OnboardingSteps[step]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            // Soak up taps so they don't pass through to the UI underneath.
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        // Skip button in the top corner
        TextButton(
            onClick = onComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Text(
                text = "Skip",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stepped reveal of the current step's emoji + copy.
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = tween(360),
                        initialOffsetY = { it / 4 }
                    ) + fadeIn(animationSpec = tween(360))) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(220),
                            targetOffsetY = { -it / 4 }
                        ) + fadeOut(animationSpec = tween(220)))
                },
                label = "onboardingStep"
            ) { targetStep ->
                val s = OnboardingSteps[targetStep]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = s.emoji, fontSize = 48.sp)
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = s.title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = s.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // Page-indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(total) { i ->
                    val isActive = i == step
                    Box(
                        modifier = Modifier
                            .size(width = if (isActive) 24.dp else 8.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) palette.primary
                                else Color.White.copy(alpha = 0.18f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Next / Got it button
            Button(
                onClick = {
                    if (step < total - 1) step++ else onComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (step < total - 1) "Next" else "Got it",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
