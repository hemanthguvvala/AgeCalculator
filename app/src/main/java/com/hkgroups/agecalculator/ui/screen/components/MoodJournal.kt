package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.util.LocalCosmicFeedback
import com.hkgroups.agecalculator.util.CosmicFeedback
import java.time.LocalDate

/**
 * MoodOption — discrete mood category with a glyph + sentiment color.
 * Five steps preserve emotional nuance without becoming a rating slider.
 */
enum class MoodOption(
    val key: String,
    val emoji: String,
    val label: String,
    val color: Color
) {
    Joyful("joyful", "🌟", "Joyful", Color(0xFFFFC857)),
    Calm("calm", "🌊", "Calm", Color(0xFF6FB7E6)),
    Neutral("neutral", "🌙", "Neutral", Color(0xFFB7AFC4)),
    Anxious("anxious", "🌪", "Anxious", Color(0xFFE08A6E)),
    Low("low", "🌑", "Low", Color(0xFF7B8AC1));

    companion object {
        fun fromKey(key: String?): MoodOption? = values().firstOrNull { it.key == key }
    }
}

/**
 * Sign-tinted reflection that maps a mood to a one-line cosmic prompt.
 * Keeps the journaling loop feeling intentional, not transactional.
 */
fun moodReflection(mood: MoodOption, signName: String?): String {
    val suffix = signName?.let { " ${it}" } ?: ""
    return when (mood) {
        MoodOption.Joyful ->
            "Ride the high$suffix — channel it into something you've been postponing."
        MoodOption.Calm ->
            "A grounded$suffix. Make a decision today that future-you will thank you for."
        MoodOption.Neutral ->
            "A holding pattern$suffix. Listen for the small signal beneath the surface."
        MoodOption.Anxious ->
            "Restless skies$suffix. Move your body — let the energy out before it loops."
        MoodOption.Low ->
            "A quiet phase$suffix. Be gentle. Not every day is for harvesting."
    }
}

/**
 * MoodJournalCard — dashboard card for today's check-in. Tap to open the
 * sheet. Shows today's mood (if logged) and the past-week strip.
 */
@Composable
fun MoodJournalCard(
    todayEntry: com.hkgroups.agecalculator.data.repository.MoodEntry?,
    recentEntries: List<com.hkgroups.agecalculator.data.repository.MoodEntry>,
    signName: String?,
    onLogToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalSignPalette.current
    val mood = todayEntry?.let { MoodOption.fromKey(it.mood) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (mood?.color ?: palette.primary).copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, (mood?.color ?: palette.primary).copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = mood?.emoji ?: "✨", fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (mood != null) "TODAY · ${mood.label.uppercase()}" else "DAILY CHECK-IN",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.primary,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (mood != null) moodReflection(mood, signName)
                           else "How are you feeling today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.primary.copy(alpha = 0.18f))
                    .border(1.dp, palette.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onLogToday() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (mood != null) "Update" else "Log",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (recentEntries.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            MoodStrip(entries = recentEntries)
        }
    }
}

/** 7-day strip of past mood logs, today on the right. */
@Composable
private fun MoodStrip(entries: List<com.hkgroups.agecalculator.data.repository.MoodEntry>) {
    val today = LocalDate.now()
    val days = remember(entries) {
        val byDate = entries.associateBy { it.date }
        (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            d to byDate[d]
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { (date, entry) ->
            val mood = entry?.let { MoodOption.fromKey(it.mood) }
            val isToday = date == today
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfWeek.toString().take(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = if (isToday) 0.85f else 0.45f),
                    fontSize = 9.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            mood?.color?.copy(alpha = 0.35f)
                                ?: Color.White.copy(alpha = 0.06f)
                        )
                        .border(
                            width = if (isToday) 1.5.dp else 0.5.dp,
                            color = (mood?.color ?: Color.White).copy(alpha = if (isToday) 0.7f else 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mood?.emoji ?: "·",
                        fontSize = if (mood != null) 14.sp else 12.sp
                    )
                }
            }
        }
    }
}

/**
 * MoodJournalSheet — modal bottom sheet for logging today. Pickable mood,
 * optional one-line note. Logs immediately when the user picks a mood (note
 * is optional polish, not a gate).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MoodJournalSheet(
    initialMood: MoodOption? = null,
    initialNote: String = "",
    onDismiss: () -> Unit,
    onSave: (MoodOption, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = LocalSignPalette.current
    val feedback = LocalCosmicFeedback.current
    var picked by rememberSaveable(initialMood) {
        mutableStateOf(initialMood?.key)
    }
    var note by rememberSaveable { mutableStateOf(initialNote) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14182B),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "HOW ARE YOU FEELING?",
                style = MaterialTheme.typography.labelSmall,
                color = palette.primary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Today's mood",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoodOption.values().forEach { option ->
                    MoodPickerChip(
                        option = option,
                        isSelected = picked == option.key,
                        onClick = {
                            picked = option.key
                            feedback?.fire(CosmicFeedback.Cue.Select)
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { if (it.length <= 90) note = it },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(palette.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        picked?.let { key ->
                            MoodOption.fromKey(key)?.let { onSave(it, note) }
                        }
                    }),
                    decorationBox = { inner ->
                        if (note.isEmpty()) {
                            Text(
                                text = "One line (optional) — what's coloring today?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Save button — disabled until a mood is picked.
            val enabled = picked != null
            val bgAlpha by animateColorAsState(
                targetValue = if (enabled) palette.primary else Color.White.copy(alpha = 0.10f),
                animationSpec = tween(200),
                label = "saveBg"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgAlpha)
                    .clickable(enabled = enabled) {
                        picked?.let { key ->
                            MoodOption.fromKey(key)?.let { onSave(it, note) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save today's mood",
                    color = if (enabled) Color(0xFF0B0E1F) else Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MoodPickerChip(
    option: MoodOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ringSize by animateDpAsState(
        targetValue = if (isSelected) 60.dp else 52.dp,
        animationSpec = spring(),
        label = "moodRing"
    )
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) option.color else Color.White.copy(alpha = 0.16f),
        animationSpec = tween(200),
        label = "moodRingColor"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isSelected)
                            listOf(option.color.copy(alpha = 0.55f), Color.Transparent)
                        else listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = ringColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = option.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}
