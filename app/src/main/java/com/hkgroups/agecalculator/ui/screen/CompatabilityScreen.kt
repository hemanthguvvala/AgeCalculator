package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.screen.components.CompatibilityResultHero
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.GlassCard
import com.hkgroups.agecalculator.ui.screen.components.GlassCardWithGlow
import com.hkgroups.agecalculator.ui.screen.components.SectionHeader
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.Space
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityDetailScreen(
    navController: NavController,
    userSignName: String?,
    partnerSignName: String?,
    viewModel: MainViewModel = hiltViewModel()
) {
    // Reactive: pre-fix this used non-reactive getSignByName which returned null
    // before the DB had loaded, leaving the screen stuck on a spinner forever.
    val signs by viewModel.zodiacSignsState.collectAsState()
    val userSign = userSignName?.let { name -> signs.find { it.name == name } }
    val partnerSign = partnerSignName?.let { name -> signs.find { it.name == name } }

    // Try to show an interstitial when this screen opens. The controller
    // applies its own rate-limit (90s gap, 30s session grace) so most opens
    // won't actually serve an ad — keeps the screen feeling fast.
    val adController = com.hkgroups.agecalculator.util.LocalAdController.current
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    androidx.compose.runtime.LaunchedEffect(userSignName, partnerSignName) {
        if (activity != null && userSignName != null && partnerSignName != null) {
            adController?.showInterstitialIfEligible(activity)
        }
    }
    // The seed data only stores high-affinity pairings explicitly; absence means
    // "low compatibility" — render the fallback rather than spinning forever.
    val compatibilityInfo = userSign?.compatibilities?.find { it.signName == partnerSign?.name }

    // Seed data stores rating on a 1-10 scale; surface as a percentage and a
    // capped 1-5 star display so neither view shows nonsense values like 160%.
    val rawRating = compatibilityInfo?.rating
        ?: if (userSign != null && partnerSign != null) 1 else 0
    val rating10 = rawRating.coerceIn(0, 10)
    val ratingStars = ((rating10 + 1) / 2).coerceIn(0, 5) // 1-2→1★, 3-4→2★, 5-6→3★, 7-8→4★, 9-10→5★
    val percent = rating10 * 10
    val description = compatibilityInfo?.description
        ?: elementPairFallback(userSign?.element, partnerSign?.element)
    val accent = when {
        ratingStars >= 4 -> SaturnGold
        ratingStars >= 3 -> PrimaryNeon
        else -> PurpleAccent
    }
    val verdict = when {
        ratingStars == 5 -> "Cosmic soulmates"
        ratingStars == 4 -> "Strong alignment"
        ratingStars == 3 -> "Worth exploring"
        ratingStars == 2 -> "Different orbits"
        ratingStars == 1 -> "Different orbits"
        else -> "Calibrating…"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        StarryBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                CosmicTopBar(
                    title = if (userSign != null && partnerSign != null)
                        "${userSign.name} & ${partnerSign.name}"
                    else "Compatibility",
                    subtitle = if (userSign != null && partnerSign != null) verdict else null,
                    onBack = { navController.popBackStack() }
                )

                if (userSign == null || partnerSign == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        com.hkgroups.agecalculator.ui.screen.components.CosmicLoading()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))

                        // Animated score hero — uses the shared component which
                        // animates the percent count-up and pulses the connector.
                        CompatibilityResultHero(
                            yourSymbol = userSign.symbol,
                            yourSign = userSign.name,
                            partnerSymbol = partnerSign.symbol,
                            partnerSign = partnerSign.name,
                            score = percent
                        )

                        Spacer(Modifier.height(20.dp))

                        SectionHeader(
                            eyebrow = "WHAT THE STARS SAY",
                            title = "Reading",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        GlassCardWithGlow(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = accent,
                            glowAlpha = 0.18f,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        SectionHeader(
                            eyebrow = "WHERE YOU ALIGN",
                            title = "Compatibility radar",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = Space.md)) {
                                com.hkgroups.agecalculator.ui.screen.components.CompatibilityRadar(
                                    axes = com.hkgroups.agecalculator.ui.screen.components
                                        .deriveCompatibilityAxes(
                                            rating10 = rating10,
                                            yourElement = userSign.element,
                                            partnerElement = partnerSign.element
                                        ),
                                    accent = accent
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        SectionHeader(
                            eyebrow = "SIDE BY SIDE",
                            title = "Element & ruling planet",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CompatChip(label = userSign.name, value = "${userSign.element} · ${userSign.rulingPlanet}", modifier = Modifier.weight(1f))
                            CompatChip(label = partnerSign.name, value = "${partnerSign.element} · ${partnerSign.rulingPlanet}", modifier = Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(20.dp))

                        // Strengths + friction + ritual — three actionable cards
                        // derived from the element pairing and rating.
                        SectionHeader(
                            eyebrow = "HOW TO NURTURE THIS",
                            title = "Reading between the stars",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        val advice = remember(userSign.element, partnerSign.element, rating10) {
                            deriveCompatibilityAdvice(
                                yourElement = userSign.element,
                                partnerElement = partnerSign.element,
                                rating10 = rating10
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdviceCard(
                                eyebrow = "STRENGTH",
                                emoji = "⭐",
                                accent = SaturnGold,
                                body = advice.strength
                            )
                            AdviceCard(
                                eyebrow = "FRICTION",
                                emoji = "⚡",
                                accent = PurpleAccent,
                                body = advice.friction
                            )
                            AdviceCard(
                                eyebrow = "TRY THIS",
                                emoji = "✨",
                                accent = PrimaryNeon,
                                body = advice.ritual
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detail-screen fallback when seed data has no description. Element-aware
 * paragraph form (vs. the one-liner used in the list).
 */
private fun elementPairFallback(yourElement: String?, partnerElement: String?): String {
    if (yourElement == null || partnerElement == null) {
        return "Your signs sit on different cosmic orbits. Connection takes effort, but the contrast can be magnetic when you both lean in."
    }
    val pair = setOf(yourElement, partnerElement)
    return when {
        pair == setOf("Fire") -> "Two fires create heat — and sometimes too much of it. The pace can be exhilarating when you're moving in the same direction, exhausting when you're not. Decide together who's leading each day."
        pair == setOf("Earth") -> "Twin earth signs build something solid. Calendars sync, plans stick, and the future feels designed rather than improvised. Watch for inertia — schedule a deliberate disruption now and then."
        pair == setOf("Air") -> "An idea factory. Conversations spiral into unexpected discoveries. The challenge is finishing what you start — pick one shared project and see it through together."
        pair == setOf("Water") -> "Deep emotional resonance. You read each other before words arrive, which is beautiful and occasionally claustrophobic. Name what you feel out loud rather than expecting them to infer."
        pair == setOf("Fire", "Air") -> "Air feeds fire. Their ideas spark your action, your action validates their thinking. Together you generate momentum others find contagious."
        pair == setOf("Earth", "Water") -> "Water nourishes earth. They soften your structure; you give shape to their feelings. A pairing built for the long game."
        pair == setOf("Fire", "Earth") -> "Earth grounds fire. Their stability turns your ambition into something durable. Don't let their pace frustrate you — it's where the work actually lands."
        pair == setOf("Air", "Water") -> "Air over water creates atmosphere. Your conversations carry weather, your silences carry warmth. Beautiful when you let it move."
        pair == setOf("Fire", "Water") -> "Steam dynamics. When you're in tune the heat is electric. When you're not, one of you boils and the other goes quiet. Slow the pace when emotions run hot."
        pair == setOf("Earth", "Air") -> "Air lifts earth out of routine. They pull you into ideas; you keep them rooted. Schedule one shared ritual a week so the orbits stay close."
        else -> "Your signs sit on different cosmic orbits. Connection takes effort, but the contrast can be magnetic when you both lean in."
    }
}

private data class CompatAdvice(
    val strength: String,
    val friction: String,
    val ritual: String
)

private fun deriveCompatibilityAdvice(
    yourElement: String,
    partnerElement: String,
    rating10: Int
): CompatAdvice {
    val pair = setOf(yourElement, partnerElement)
    val isHighFit = rating10 >= 7
    val strength = when {
        pair == setOf("Fire") -> "Two fires double the spark — collective momentum, big plans, contagious energy."
        pair == setOf("Earth") -> "Twin pragmatism. Calendars sync, plans stick, the future feels solid."
        pair == setOf("Air") -> "An idea factory. Conversations spiral into shared discoveries."
        pair == setOf("Water") -> "Emotional resonance. You read each other before words arrive."
        pair == setOf("Fire", "Air") -> "Fire stays fed by air. Their ideas ignite your action."
        pair == setOf("Earth", "Water") -> "Water nourishes earth. They soften your structure; you give their feelings shape."
        pair == setOf("Fire", "Earth") -> "Earth grounds fire. They turn your ambition into something durable."
        pair == setOf("Air", "Water") -> "Air over water creates weather. Together you generate atmosphere."
        pair == setOf("Fire", "Water") -> "Steam dynamics. When you're in tune, the heat is electric."
        pair == setOf("Earth", "Air") -> "Air over earth shapes the landscape. They lift you out of routine."
        else -> "A meeting of distinct worlds. The contrast is the point."
    }
    val friction = when {
        pair == setOf("Fire", "Water") ->
            "Heat boils over the feeling. Slow the pace when emotions run high."
        pair == setOf("Earth", "Air") ->
            "Earth wants concrete; air wants free. Schedule one shared ritual a week."
        pair == setOf("Fire") ->
            "Two engines, one steering wheel. Decide ahead of time who drives this trip."
        pair == setOf("Earth") ->
            "Stability can become inertia. Plan a deliberate disruption monthly."
        pair == setOf("Air") ->
            "Ideas without follow-through. Pick one shared project to finish."
        pair == setOf("Water") ->
            "Mirror-reading turns into mood loops. Name feelings out loud, don't infer."
        else -> if (isHighFit)
            "Even great pairings have edges. The friction is information."
        else
            "Different orbits. Honor the gap rather than forcing alignment."
    }
    val ritual = when (yourElement) {
        "Fire" -> "Plan a movement-first date — hike, dance class, anything kinetic."
        "Earth" -> "Cook a meal together. The shared rhythm builds trust faster than talking."
        "Air" -> "Pick a topic neither knows much about. Learn it together for an hour."
        "Water" -> "Sit by water. Let the silence carry its own conversation."
        else -> "Take a walk with no destination. Talk only when something arrives naturally."
    }
    return CompatAdvice(strength, friction, ritual)
}

@Composable
private fun AdviceCard(
    eyebrow: String,
    emoji: String,
    accent: Color,
    body: String
) {
    GlassCardWithGlow(
        modifier = Modifier.fillMaxWidth(),
        glowColor = accent,
        glowAlpha = 0.14f,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ZodiacOrb(symbol: String, name: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = symbol, fontSize = 44.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CompatChip(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.height(80.dp), shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                letterSpacing = 1.5.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
        }
    }
}
