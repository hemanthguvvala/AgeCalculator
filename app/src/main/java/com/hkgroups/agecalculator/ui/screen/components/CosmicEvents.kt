package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit

/**
 * CosmicEventKind — categorizes recurring astronomical/astrological events
 * for icon + color selection.
 */
enum class CosmicEventKind { Retrograde, Equinox, Solstice, Eclipse, NewMoon, FullMoon }

data class CosmicEvent(
    val title: String,
    val date: LocalDate,
    val kind: CosmicEventKind
)

/**
 * Recurring cosmic events expressed as month/day so we can render them for
 * any year. Real ephemeris data lives on a server; this static set keeps the
 * feature working offline as a "what's coming up" preview.
 */
private val RecurringEvents = listOf(
    Triple("Spring Equinox", MonthDay.of(3, 20), CosmicEventKind.Equinox),
    Triple("Summer Solstice", MonthDay.of(6, 21), CosmicEventKind.Solstice),
    Triple("Autumn Equinox", MonthDay.of(9, 22), CosmicEventKind.Equinox),
    Triple("Winter Solstice", MonthDay.of(12, 21), CosmicEventKind.Solstice),
    Triple("Mercury Retrograde", MonthDay.of(4, 1), CosmicEventKind.Retrograde),
    Triple("Mercury Retrograde", MonthDay.of(8, 5), CosmicEventKind.Retrograde),
    Triple("Mercury Retrograde", MonthDay.of(11, 25), CosmicEventKind.Retrograde),
    Triple("Solar Eclipse", MonthDay.of(4, 8), CosmicEventKind.Eclipse),
    Triple("Lunar Eclipse", MonthDay.of(10, 17), CosmicEventKind.Eclipse)
)

/**
 * Returns the next [count] cosmic events starting from [today]. Wraps around
 * to next year for events that have already passed.
 */
fun upcomingCosmicEvents(today: LocalDate = LocalDate.now(), count: Int = 4): List<CosmicEvent> {
    return RecurringEvents
        .map { (title, monthDay, kind) ->
            val thisYear = monthDay.atYear(today.year)
            val target = if (thisYear.isBefore(today)) monthDay.atYear(today.year + 1) else thisYear
            CosmicEvent(title = title, date = target, kind = kind)
        }
        .sortedBy { it.date }
        .take(count)
}

@Composable
fun CosmicEventsSection(
    events: List<CosmicEvent>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        events.forEachIndexed { index, event ->
            CosmicEventRow(event = event)
            if (index < events.lastIndex) {
                Spacer(Modifier.height(1.dp))
            }
        }
    }
}

@Composable
private fun CosmicEventRow(event: CosmicEvent) {
    val palette = LocalSignPalette.current
    val today = remember { LocalDate.now() }
    val daysAway = remember(event.date) {
        ChronoUnit.DAYS.between(today, event.date).coerceAtLeast(0L)
    }
    val accent = when (event.kind) {
        CosmicEventKind.Retrograde -> Color(0xFFFF6B6B)
        CosmicEventKind.Equinox -> palette.primary
        CosmicEventKind.Solstice -> SaturnGold
        CosmicEventKind.Eclipse -> Color(0xFF9B59B6)
        CosmicEventKind.NewMoon -> Color(0xFF6E7AAA)
        CosmicEventKind.FullMoon -> Color(0xFFE0C097)
    }
    val emoji = when (event.kind) {
        CosmicEventKind.Retrograde -> "↺"
        CosmicEventKind.Equinox -> "☯"
        CosmicEventKind.Solstice -> "☀"
        CosmicEventKind.Eclipse -> "🌑"
        CosmicEventKind.NewMoon -> "🌒"
        CosmicEventKind.FullMoon -> "🌕"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp, color = accent)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = event.date.format(
                    java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
        // Countdown pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(accent.copy(alpha = 0.16f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    daysAway == 0L -> "Today"
                    daysAway == 1L -> "Tomorrow"
                    else -> "in $daysAway days"
                },
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
