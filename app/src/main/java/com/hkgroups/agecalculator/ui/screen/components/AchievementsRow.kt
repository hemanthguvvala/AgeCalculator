package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SaturnGold

/**
 * Achievement — a single unlockable.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean
)

/**
 * Pure derivation: given the user's current state, returns the full
 * achievement list (locked + unlocked) in display order.
 *
 * Splitting the data out from the UI keeps unlocking logic testable.
 */
fun deriveAchievements(
    currentStreak: Int,
    longestStreak: Int,
    hasBirthDate: Boolean,
    visitedSignsCount: Int,
    completedCompatibilityChecks: Int
): List<Achievement> = listOf(
    Achievement(
        id = "first_light",
        title = "First Light",
        description = "Set your birth date",
        emoji = "✨",
        isUnlocked = hasBirthDate
    ),
    Achievement(
        id = "streak_3",
        title = "Aligned",
        description = "3-day streak",
        emoji = "🌱",
        isUnlocked = longestStreak >= 3
    ),
    Achievement(
        id = "streak_7",
        title = "Weekly Orbit",
        description = "7-day streak",
        emoji = "🔥",
        isUnlocked = longestStreak >= 7
    ),
    Achievement(
        id = "streak_30",
        title = "Lunar Cycle",
        description = "30-day streak",
        emoji = "🌙",
        isUnlocked = longestStreak >= 30
    ),
    Achievement(
        id = "streak_100",
        title = "Century Star",
        description = "100-day streak",
        emoji = "💫",
        isUnlocked = longestStreak >= 100
    ),
    Achievement(
        id = "streak_365",
        title = "Solar Return",
        description = "Full year streak",
        emoji = "☀️",
        isUnlocked = longestStreak >= 365
    ),
    Achievement(
        id = "explored_all",
        title = "Astrologer",
        description = "Explored all 12 signs",
        emoji = "🪐",
        isUnlocked = visitedSignsCount >= 12
    ),
    Achievement(
        id = "first_match",
        title = "Heart Reader",
        description = "First compatibility check",
        emoji = "💞",
        isUnlocked = completedCompatibilityChecks >= 1
    )
)

/**
 * AchievementsRow — horizontally-scrolling badge list. Locked badges are
 * dimmed and grayscale; unlocked badges show the user's palette.
 */
@Composable
fun AchievementsRow(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(achievements) { ach ->
            AchievementBadge(ach)
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    val palette = LocalSignPalette.current
    val accent = if (achievement.isUnlocked) palette.primary else Color.White.copy(alpha = 0.18f)
    val cardAlpha = if (achievement.isUnlocked) 1f else 0.55f

    Column(
        modifier = Modifier
            .width(110.dp)
            .alpha(cardAlpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (achievement.isUnlocked)
                        Brush.radialGradient(
                            colors = listOf(
                                palette.primary.copy(alpha = 0.45f),
                                palette.secondary.copy(alpha = 0.20f)
                            )
                        )
                    else
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                )
                .border(1.dp, accent.copy(alpha = if (achievement.isUnlocked) 0.7f else 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (achievement.isUnlocked) achievement.emoji else "🔒",
                fontSize = 28.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = achievement.title,
            style = MaterialTheme.typography.labelMedium,
            color = if (achievement.isUnlocked) Color.White else Color.White.copy(alpha = 0.55f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = achievement.description,
            style = MaterialTheme.typography.labelSmall,
            color = if (achievement.isUnlocked) SaturnGold else Color.White.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
