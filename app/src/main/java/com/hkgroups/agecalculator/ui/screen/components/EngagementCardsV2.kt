package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.content.AstronomyEngine
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.util.LunarPhase

/**
 * BirthdayBannerCard — fires in the ±7-day window around the user's birthday.
 * High-engagement moment; surfaced prominently at the top of the dashboard.
 */
@Composable
fun BirthdayBannerCard(
    message: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, SaturnGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SaturnGold.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎂", fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * QuestionOfDayCard — daily reflection prompt. The user types a short answer;
 * the answered state persists across sessions.
 */
@Composable
fun QuestionOfDayCard(
    question: String,
    hasAnswered: Boolean,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (question.isBlank()) return
    var draft by rememberSaveable(question) { mutableStateOf("") }
    var submitted by rememberSaveable(question) { mutableStateOf(hasAnswered) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryNeon.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Today's reflection",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(14.dp))

            if (submitted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓", color = SaturnGold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Answered. Come back tomorrow for a new prompt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 500) draft = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(PrimaryNeon),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        decorationBox = { inner ->
                            if (draft.isEmpty()) {
                                Text(
                                    "A short note for your future self…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val enabled = draft.isNotBlank()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (enabled) PrimaryNeon.copy(alpha = 0.85f)
                                else Color.White.copy(alpha = 0.08f)
                            )
                            .clickable(enabled = enabled) {
                                onSubmit(draft.trim())
                                submitted = true
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (enabled) Color(0xFF0E0B1F) else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * MoodInsightCard — surfaces a single sentence pattern detected in the user's
 * mood log. Hidden when the engine returns null (not enough data yet).
 */
@Composable
fun MoodInsightCard(
    insight: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = insight != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut()
    ) {
        if (insight == null) return@AnimatedVisibility
        GlassCard(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, PrimaryNeon.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryNeon.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔍", fontSize = 18.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pattern detected",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
        }
    }
}

/**
 * CosmicWeatherCard — compact summary of "what's in the sky today": today's
 * sun-sign, ruling planet of the day, moon phase, retrograde flag.
 * Drives the user's awareness that real astronomical state is shaping the
 * content they see.
 */
@Composable
fun CosmicWeatherCard(
    snapshot: AstronomyEngine.CosmicSnapshot,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today in the sky",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CosmicWeatherTile(
                    emoji = "☀",
                    label = "Sun in",
                    value = snapshot.sunSignOfDay,
                    modifier = Modifier.weight(1f)
                )
                CosmicWeatherTile(
                    emoji = LunarPhase.shortGlyph(snapshot.moonPhase),
                    label = "Moon",
                    value = "${snapshot.moonIllumination}%",
                    modifier = Modifier.weight(1f)
                )
                CosmicWeatherTile(
                    emoji = planetEmoji(snapshot.rulingPlanetOfDay),
                    label = "Day of",
                    value = snapshot.rulingPlanetOfDay,
                    modifier = Modifier.weight(1f)
                )
            }
            if (snapshot.isMercuryRetrograde) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "↺ Mercury retrograde — be deliberate with messages and contracts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF8B8B)
                    )
                }
            } else if (snapshot.daysToNextRetrograde in 1..14) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Mercury retrograde in ${snapshot.daysToNextRetrograde} days — prep your inbox.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun CosmicWeatherTile(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * StreakPillWithFreezes — extends the original [StreakPill] with a small
 * snowflake badge showing how many "freeze days" the user has banked.
 */
@Composable
fun StreakPillWithFreezes(
    days: Int,
    freezes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StreakPill(days = days)
        if (freezes > 0) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF6FB1D9).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFF6FB1D9).copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❄", fontSize = 14.sp, color = Color(0xFF8FCBE8))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = freezes.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF8FCBE8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun planetEmoji(planet: String): String = when (planet) {
    "Sun" -> "☀"
    "Moon" -> "🌙"
    "Mars" -> "♂"
    "Mercury" -> "☿"
    "Jupiter" -> "♃"
    "Venus" -> "♀"
    "Saturn" -> "♄"
    else -> "✨"
}
