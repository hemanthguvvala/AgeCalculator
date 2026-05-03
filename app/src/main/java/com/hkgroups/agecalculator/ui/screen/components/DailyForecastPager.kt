package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import kotlin.math.absoluteValue

/**
 * ForecastFacet — one dimension of today's reading.
 */
data class ForecastFacet(
    val key: String,
    val title: String,
    val emoji: String,
    val accent: Color,
    val body: String
)

/**
 * DailyForecastPager — swipeable forecast with 4 facets (Mood, Love, Energy,
 * Focus). Each card is a glass surface with the facet emoji + title + body.
 *
 * Replaces the single forecast card on the dashboard. Gives the daily ritual
 * more depth — the user can swipe through to see all dimensions of the day.
 *
 * Uses HorizontalPager with a slight neighbor preview so users see they can
 * swipe. Pager dots underneath show the active facet.
 */
@Composable
fun DailyForecastPager(
    horoscope: String?,
    seed: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalSignPalette.current
    val facets = remember(horoscope, seed, palette) {
        buildFacets(horoscope = horoscope, seed = seed, palette = palette)
    }
    val pagerState = rememberPagerState(pageCount = { facets.size })

    // Light haptic on each page change so swipes feel responsive.
    val cosmicFeedback = com.hkgroups.agecalculator.util.LocalCosmicFeedback.current
    androidx.compose.runtime.LaunchedEffect(pagerState) {
        androidx.compose.runtime.snapshotFlow { pagerState.settledPage }.collect {
            cosmicFeedback?.fire(com.hkgroups.agecalculator.util.CosmicFeedback.Cue.Swipe)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 28.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction
                ).coerceIn(-1.5f, 1.5f)
            val abs = pageOffset.absoluteValue.coerceAtMost(1f)
            ForecastCard(
                facet = facets[page],
                modifier = Modifier
                    .graphicsLayer {
                        // Side cards drop a touch so the focused card pops.
                        scaleY = 1f - 0.06f * abs
                        alpha = 1f - 0.4f * abs
                    }
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            facets.forEachIndexed { i, _ ->
                val isActive = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (isActive) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) palette.primary else Color.White.copy(alpha = 0.18f)
                        )
                )
            }
        }
    }
}

@Composable
private fun ForecastCard(
    facet: ForecastFacet,
    modifier: Modifier = Modifier
) {
    GlassCardWithGlow(
        modifier = modifier.fillMaxWidth(),
        glowColor = facet.accent,
        glowAlpha = 0.30f,
        elevation = 22.dp,
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(facet.accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = facet.emoji, fontSize = 22.sp)
                }
                Spacer(Modifier.size(width = 12.dp, height = 0.dp))
                Column {
                    Text(
                        text = facet.key.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = facet.accent,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = facet.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = facet.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
        }
    }
}

private fun buildFacets(
    horoscope: String?,
    seed: Int,
    palette: com.hkgroups.agecalculator.ui.theme.SignPalette
): List<ForecastFacet> {
    val moods = listOf(
        "Radiant" to "Confidence flows through you. Lead with what you know.",
        "Reflective" to "A quiet day for inner work — listen before deciding.",
        "Charged" to "Channel today's energy into one bold move.",
        "Magnetic" to "People will gravitate to your light. Show up fully."
    )
    val loves = listOf(
        "Open Heart" to "Vulnerability rewards you today — say the thing.",
        "Slow Burn" to "A quieter connection deepens. No need to rush.",
        "Magnetic Pull" to "Someone is paying attention. Notice them back.",
        "Self-Love" to "Pour into yourself first. The rest follows."
    )
    val energies = listOf(
        "High Tide" to "Strong physical energy — move, build, finish things.",
        "Steady" to "Pace yourself. Marathon, not sprint, suits today.",
        "Recharging" to "Rest is productive too. Don't fight the lull.",
        "Electric" to "Try the unfamiliar — your body wants something new."
    )
    val focuses = listOf(
        "One Thing" to "Pick a single priority. Let everything else wait.",
        "Big Picture" to "Zoom out today — strategy beats tactics.",
        "Detail Mode" to "Small precision wins. Polish matters.",
        "Listen First" to "What goes unsaid is the real signal."
    )
    // floorMod keeps the index non-negative even when seed*N overflows Int.
    val mood = moods[Math.floorMod(seed, moods.size)]
    val love = loves[Math.floorMod(seed * 7, loves.size)]
    val energy = energies[Math.floorMod(seed * 13, energies.size)]
    val focus = focuses[Math.floorMod(seed * 19, focuses.size)]

    val baseHoroscope = horoscope?.takeIf { it.isNotBlank() }
        ?: "The cosmos is gently rearranging itself in your favor today."

    return listOf(
        ForecastFacet(
            key = "Reading",
            title = "Today's forecast",
            emoji = "🔮",
            accent = palette.primary,
            body = baseHoroscope
        ),
        ForecastFacet(
            key = "Mood",
            title = mood.first,
            emoji = "🌙",
            accent = palette.secondary,
            body = mood.second
        ),
        ForecastFacet(
            key = "Love",
            title = love.first,
            emoji = "💞",
            accent = SaturnGold,
            body = love.second
        ),
        ForecastFacet(
            key = "Energy",
            title = energy.first,
            emoji = "⚡",
            accent = palette.primary,
            body = energy.second
        ),
        ForecastFacet(
            key = "Focus",
            title = focus.first,
            emoji = "🎯",
            accent = palette.secondary,
            body = focus.second
        )
    )
}
