package com.hkgroups.agecalculator.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.LocalSize
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.MainActivity
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.ui.screen.components.upcomingCosmicEvents
import com.hkgroups.agecalculator.util.LunarPhase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * CosmicAgeWidget — adaptive home-screen widget. Three layouts triggered by
 * the host's reported size:
 *   small (≈3×2): cosmic age only
 *   medium (≈4×2): age + sun sign + next event title
 *   large (≈4×3+): all of the above plus moon phase + countdown
 *
 * Sizing is done with Glance's built-in SizeMode.Responsive, which lets the
 * launcher pick the closest layout when the user resizes the widget.
 */
class CosmicAgeWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SettingsAccessor {
        fun settingsRepository(): SettingsRepository
    }

    private val small = DpSize(180.dp, 120.dp)
    private val medium = DpSize(260.dp, 120.dp)
    private val large = DpSize(260.dp, 200.dp)

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(small, medium, large))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = EntryPointAccessors
            .fromApplication(context, SettingsAccessor::class.java)
            .settingsRepository()
        val savedMillis = repo.savedBirthDate.first()
        val today = LocalDate.now()
        val birthDate = savedMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val years = birthDate?.let { Period.between(it, today).years }
        val sign = birthDate?.let { zodiacSignNameFromDate(it) }

        val nextEvent = upcomingCosmicEvents(today = today, count = 1).firstOrNull()
        val daysToNextEvent = nextEvent?.let {
            ChronoUnit.DAYS.between(today, it.date).toInt().coerceAtLeast(0)
        }

        val phaseFraction = LunarPhase.phaseFraction(today)
        val phase = LunarPhase.phase(phaseFraction)
        val illumination = LunarPhase.illuminationPercent(phaseFraction)
        val moonGlyph = LunarPhase.shortGlyph(phase)

        provideContent {
            CosmicAgeWidgetContent(
                context = context,
                years = years,
                sign = sign,
                nextEventTitle = nextEvent?.title,
                daysToNextEvent = daysToNextEvent,
                moonGlyph = moonGlyph,
                moonPhaseTitle = phase.title,
                moonIllumination = illumination
            )
        }
    }

    @Composable
    private fun CosmicAgeWidgetContent(
        context: Context,
        years: Int?,
        sign: String?,
        nextEventTitle: String?,
        daysToNextEvent: Int?,
        moonGlyph: String,
        moonPhaseTitle: String,
        moonIllumination: Int
    ) {
        val size = LocalSize.current
        val openApp = actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(ColorProvider(ComposeColor(0xFF0A0F1F)))
                .cornerRadius(20.dp)
                .clickable(openApp),
            contentAlignment = Alignment.Center
        ) {
            when {
                size.height >= large.height -> LargeLayout(
                    years = years,
                    sign = sign,
                    nextEventTitle = nextEventTitle,
                    daysToNextEvent = daysToNextEvent,
                    moonGlyph = moonGlyph,
                    moonPhaseTitle = moonPhaseTitle,
                    moonIllumination = moonIllumination
                )
                size.width >= medium.width -> MediumLayout(
                    years = years,
                    sign = sign,
                    nextEventTitle = nextEventTitle,
                    daysToNextEvent = daysToNextEvent
                )
                else -> SmallLayout(years = years)
            }
        }
    }

    @Composable
    private fun SmallLayout(years: Int?) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "YOUR COSMIC AGE",
                style = TextStyle(
                    color = ColorProvider(ComposeColor(0xFFE0C097)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(6.dp))
            AgeRow(years)
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "Open Zodiac Age",
                style = TextStyle(
                    color = ColorProvider(ComposeColor(0xFF4D96FF)),
                    fontSize = 11.sp
                )
            )
        }
    }

    @Composable
    private fun MediumLayout(
        years: Int?,
        sign: String?,
        nextEventTitle: String?,
        daysToNextEvent: Int?
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = (sign?.uppercase() ?: "COSMIC AGE"),
                    style = TextStyle(
                        color = ColorProvider(ComposeColor(0xFFE0C097)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                AgeRow(years)
            }
            Spacer(GlanceModifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "NEXT EVENT",
                    style = TextStyle(
                        color = ColorProvider(ComposeColor(0x99FFFFFF.toInt())),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = nextEventTitle ?: "—",
                    style = TextStyle(
                        color = ColorProvider(ComposeColor.White),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (daysToNextEvent != null) {
                    Text(
                        text = "in $daysToNextEvent day${if (daysToNextEvent == 1) "" else "s"}",
                        style = TextStyle(
                            color = ColorProvider(ComposeColor(0xFF4D96FF)),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun LargeLayout(
        years: Int?,
        sign: String?,
        nextEventTitle: String?,
        daysToNextEvent: Int?,
        moonGlyph: String,
        moonPhaseTitle: String,
        moonIllumination: Int
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = (sign?.uppercase() ?: "COSMIC AGE"),
                        style = TextStyle(
                            color = ColorProvider(ComposeColor(0xFFE0C097)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    AgeRow(years)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = moonGlyph,
                        style = TextStyle(
                            color = ColorProvider(ComposeColor.White),
                            fontSize = 30.sp
                        )
                    )
                    Text(
                        text = "$moonIllumination%",
                        style = TextStyle(
                            color = ColorProvider(ComposeColor(0xFFE0C097)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(GlanceModifier.height(10.dp))
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorProvider(ComposeColor(0x33FFFFFF.toInt())))
            ) {}
            Spacer(GlanceModifier.height(8.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "MOON",
                        style = TextStyle(
                            color = ColorProvider(ComposeColor(0x99FFFFFF.toInt())),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = moonPhaseTitle,
                        style = TextStyle(
                            color = ColorProvider(ComposeColor.White),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NEXT",
                        style = TextStyle(
                            color = ColorProvider(ComposeColor(0x99FFFFFF.toInt())),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = nextEventTitle ?: "—",
                        style = TextStyle(
                            color = ColorProvider(ComposeColor.White),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (daysToNextEvent != null) {
                        Text(
                            text = "in $daysToNextEvent day${if (daysToNextEvent == 1) "" else "s"}",
                            style = TextStyle(
                                color = ColorProvider(ComposeColor(0xFF4D96FF)),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AgeRow(years: Int?) {
        Row(verticalAlignment = Alignment.Vertical.Bottom) {
            Text(
                text = years?.toString() ?: "—",
                style = TextStyle(
                    color = ColorProvider(ComposeColor.White),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.width(4.dp))
            Text(
                text = "yr",
                style = TextStyle(
                    color = ColorProvider(ComposeColor(0x99FFFFFF.toInt())),
                    fontSize = 18.sp
                )
            )
        }
    }
}

class CosmicAgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CosmicAgeWidget()
}

/** Same lookup as in MainActivity — duplicated to keep widget self-contained. */
private fun zodiacSignNameFromDate(date: LocalDate): String {
    val m = date.monthValue
    val d = date.dayOfMonth
    return when {
        (m == 3 && d >= 21) || (m == 4 && d <= 19) -> "Aries"
        (m == 4 && d >= 20) || (m == 5 && d <= 20) -> "Taurus"
        (m == 5 && d >= 21) || (m == 6 && d <= 20) -> "Gemini"
        (m == 6 && d >= 21) || (m == 7 && d <= 22) -> "Cancer"
        (m == 7 && d >= 23) || (m == 8 && d <= 22) -> "Leo"
        (m == 8 && d >= 23) || (m == 9 && d <= 22) -> "Virgo"
        (m == 9 && d >= 23) || (m == 10 && d <= 22) -> "Libra"
        (m == 10 && d >= 23) || (m == 11 && d <= 21) -> "Scorpio"
        (m == 11 && d >= 22) || (m == 12 && d <= 21) -> "Sagittarius"
        (m == 12 && d >= 22) || (m == 1 && d <= 19) -> "Capricorn"
        (m == 1 && d >= 20) || (m == 2 && d <= 18) -> "Aquarius"
        else -> "Pisces"
    }
}
