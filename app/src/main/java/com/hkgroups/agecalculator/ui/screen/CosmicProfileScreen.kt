package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.CurvedGauge
import com.hkgroups.agecalculator.ui.screen.components.GlassCard
import com.hkgroups.agecalculator.ui.screen.components.GlassCardWithGlow
import com.hkgroups.agecalculator.ui.screen.components.GradientBorderRing
import com.hkgroups.agecalculator.ui.screen.components.SectionHeader
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.screen.components.pressableScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.BorderGlass
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.Radius
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.ShapeLg
import com.hkgroups.agecalculator.ui.theme.ShapeMd
import com.hkgroups.agecalculator.ui.theme.Space
import com.hkgroups.agecalculator.ui.theme.SurfaceGlass
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import com.hkgroups.agecalculator.util.CosmicUtils
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

/**
 * Cosmic Profile — entirely data-driven now (no Alex Andromeda placeholder).
 * Shows the user's actual birth-date-derived identity:
 *  - Avatar from Chinese zodiac emoji
 *  - Sun sign + element + Chinese-year + decade
 *  - Lifetime progress (vs ~80yr expectancy)
 *  - Quick links to Compatibility, Birthday Events, Settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmicProfileScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val risingSign by viewModel.settingsRepository.risingSign.collectAsState(initial = null)
    val moonSign by viewModel.settingsRepository.moonSign.collectAsState(initial = null)
    var pickerOpenFor by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<TrinityPick?>(null)
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
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 160.dp)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val sign = uiState.zodiacSign
                val cz = uiState.chineseZodiac
                val dob = uiState.selectedDate
                val palette = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current
                val shareScope = androidx.compose.runtime.rememberCoroutineScope()
                CosmicTopBar(
                    title = "Your cosmic profile",
                    subtitle = "Identity, lifetime, and quick links",
                    onBack = { navController.popBackStack() },
                    trailing = {
                        com.hkgroups.agecalculator.ui.screen.components.IconChip(
                            icon = Icons.Filled.Share,
                            contentDescription = "Share cosmic identity",
                            onClick = {
                                shareScope.launch {
                                    val bmp = com.hkgroups.agecalculator.ui.screen.components
                                        .renderCosmicShareCard(
                                            sunSign = sign?.name,
                                            sunSignSymbol = sign?.symbol,
                                            chineseZodiac = cz,
                                            earthAge = dob?.let {
                                                "${java.time.Period.between(it, java.time.LocalDate.now()).years}"
                                            },
                                            palette = palette
                                        )
                                    com.hkgroups.agecalculator.ui.screen.components
                                        .shareCosmicCardImage(context, bmp)
                                }
                            }
                        )
                    }
                )

                Spacer(Modifier.height(Space.lg))

                ProfileHero(uiState = uiState)

                Spacer(Modifier.height(Space.xl))

                // Cosmic trinity — Sun (read-only) + Rising + Moon (editable).
                // Rising/Moon are manually entered since accurate calculation
                // requires birth time and location.
                CosmicTrinitySection(
                    sunSignName = uiState.zodiacSign?.name,
                    risingSignName = risingSign,
                    moonSignName = moonSign,
                    onTapRising = { pickerOpenFor = TrinityPick.Rising },
                    onTapMoon = { pickerOpenFor = TrinityPick.Moon }
                )

                Spacer(Modifier.height(Space.xl))

                IdentityFactsSection(uiState = uiState)

                Spacer(Modifier.height(Space.xl))

                // Birth chart wheel — shows where the user's sun sign sits
                // among the full zodiac wheel, with all 12 glyphs around the
                // perimeter and the user's segment highlighted.
                Column(
                    modifier = Modifier.padding(horizontal = Space.md)
                ) {
                    SectionHeader(eyebrow = "YOUR PLACE", title = "Birth chart")
                    Spacer(Modifier.height(Space.sm))
                    com.hkgroups.agecalculator.ui.screen.components.BirthChartWheel(
                        sunSignName = uiState.zodiacSign?.name
                    )
                }

                Spacer(Modifier.height(Space.xl))

                // Achievements — derives badges from current/longest streak,
                // birth date set, etc. Locked badges show as 🔒 placeholders.
                val streakLongest by viewModel.longestStreakDays
                    .collectAsState(initial = 0)
                val streakCurrent by viewModel.streakDays.collectAsState()
                Column(modifier = Modifier.padding(horizontal = 0.dp)) {
                    Box(modifier = Modifier.padding(horizontal = Space.md)) {
                        SectionHeader(eyebrow = "MILESTONES", title = "Achievements")
                    }
                    Spacer(Modifier.height(Space.sm))
                    com.hkgroups.agecalculator.ui.screen.components.AchievementsRow(
                        achievements = com.hkgroups.agecalculator.ui.screen.components
                            .deriveAchievements(
                                currentStreak = streakCurrent,
                                longestStreak = streakLongest,
                                hasBirthDate = uiState.selectedDate != null,
                                visitedSignsCount = 0, // Future: track in repository
                                completedCompatibilityChecks = 0
                            )
                    )
                }

                Spacer(Modifier.height(Space.xl))

                LifetimeProgressSection(uiState = uiState)

                Spacer(Modifier.height(Space.xl))

                ProfileActionsSection(navController = navController)

                Spacer(Modifier.height(Space.xl))
            }
        }
    }

    pickerOpenFor?.let { which ->
        val initial = if (which == TrinityPick.Rising) risingSign else moonSign
        com.hkgroups.agecalculator.ui.screen.components.SignPickerSheet(
            title = if (which == TrinityPick.Rising) "Rising sign" else "Moon sign",
            subtitle = if (which == TrinityPick.Rising)
                "How others see you when you walk into a room."
            else "Your inner emotional weather.",
            initialSelection = initial,
            allowClear = true,
            onDismiss = { pickerOpenFor = null },
            onSelect = { sign ->
                if (which == TrinityPick.Rising) viewModel.setRisingSign(sign)
                else viewModel.setMoonSign(sign)
                pickerOpenFor = null
            }
        )
    }
}

private enum class TrinityPick { Rising, Moon }

@Composable
private fun CosmicTrinitySection(
    sunSignName: String?,
    risingSignName: String?,
    moonSignName: String?,
    onTapRising: () -> Unit,
    onTapMoon: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Space.md)) {
        SectionHeader(eyebrow = "BIG THREE", title = "Cosmic trinity")
        Spacer(Modifier.height(Space.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            TrinityCell(
                eyebrow = "SUN",
                signName = sunSignName,
                placeholder = "—",
                editable = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            TrinityCell(
                eyebrow = "RISING",
                signName = risingSignName,
                placeholder = "Tap to set",
                editable = true,
                onClick = onTapRising,
                modifier = Modifier.weight(1f)
            )
            TrinityCell(
                eyebrow = "MOON",
                signName = moonSignName,
                placeholder = "Tap to set",
                editable = true,
                onClick = onTapMoon,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TrinityCell(
    eyebrow: String,
    signName: String?,
    placeholder: String,
    editable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signPalette = com.hkgroups.agecalculator.ui.theme.rememberSignPalette(signName)
    val accent = if (signName != null) signPalette.primary else PurpleAccent
    val interactionSource = remember { MutableInteractionSource() }

    GlassCardWithGlow(
        modifier = modifier
            .let { if (editable) it.clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accent),
                onClick = onClick
            ).pressableScale(interactionSource) else it },
        glowColor = accent,
        glowAlpha = 0.18f,
        shape = ShapeLg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.sm, vertical = Space.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(Space.xs))
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (signName != null) {
                    com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                        sign = signName,
                        strokeColor = Color.White,
                        accentColor = accent,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(
                        text = if (editable) "+" else "—",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Light
                    )
                }
            }
            Spacer(Modifier.height(Space.xs))
            Text(
                text = signName ?: placeholder,
                style = MaterialTheme.typography.labelMedium,
                color = if (signName != null) Color.White
                        else Color.White.copy(alpha = 0.45f),
                fontWeight = if (signName != null) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ProfileHero(uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState) {
    val cz = uiState.chineseZodiac
    val sign = uiState.zodiacSign
    val nameLabel = sign?.name ?: "Cosmic explorer"
    val subLabel = when {
        sign != null && cz != null -> "${sign.element} · Year of the $cz"
        sign != null -> sign.element
        cz != null -> "Year of the $cz"
        else -> "Stars aligned"
    }
    val palette = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero — bigger sign-tinted halo + rotating ring + custom-drawn glyph.
        // Palette colors mean a Taurus profile feels like grounded green-gold,
        // an Aries profile feels like fire red, etc.
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                palette.primary.copy(alpha = 0.45f),
                                palette.secondary.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            GradientBorderRing(
                size = 168.dp,
                strokeWidth = 2.dp,
                colors = listOf(palette.primary, palette.secondary, palette.primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF15192C),
                                    Color(0xFF06080F)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                        sign = sign?.name ?: "",
                        strokeColor = Color.White,
                        accentColor = palette.primary,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(Space.lg))

        // Name — boost from headlineMedium (28sp) to a true display heading.
        Text(
            text = nameLabel,
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        Spacer(Modifier.height(Space.xxs))
        Text(
            text = subLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        uiState.selectedDate?.let { dob ->
            Spacer(Modifier.height(Space.sm))
            val period = remember(dob) { Period.between(dob, LocalDate.now()) }
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceGlass)
                    .border(1.dp, BorderGlass, CircleShape)
                    .padding(horizontal = Space.md, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎂", fontSize = 14.sp)
                Spacer(Modifier.width(Space.xxs))
                Text(
                    text = dob.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) +
                        " · " + period.years + " yr",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun IdentityFactsSection(uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState) {
    val sign = uiState.zodiacSign
    val cz = uiState.chineseZodiac
    val dob = uiState.selectedDate

    Column(
        modifier = Modifier.padding(horizontal = Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        ProfileSectionTitle("You are")

        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            FactCard(
                eyebrow = "SUN SIGN",
                primary = sign?.name ?: "—",
                secondary = sign?.let { "${it.symbol}  ${it.element}" } ?: "Set your birth date",
                accent = PrimaryNeon,
                modifier = Modifier.weight(1f)
            )
            FactCard(
                eyebrow = "CHINESE",
                primary = cz ?: "—",
                secondary = cz?.let { "${CosmicUtils.getChineseZodiacEmoji(it)}  Year of the $it" } ?: "",
                accent = SaturnGold,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            FactCard(
                eyebrow = "BIRTH YEAR",
                primary = dob?.year?.toString() ?: "—",
                secondary = dob?.let {
                    "${decadeName(it.year)} · ${dayOfWeekName(it)}"
                } ?: "",
                accent = PurpleAccent,
                modifier = Modifier.weight(1f)
            )
            FactCard(
                eyebrow = "DAYS LIVED",
                primary = dob?.let {
                    "%,d".format(java.time.temporal.ChronoUnit.DAYS.between(it, LocalDate.now()))
                } ?: "—",
                secondary = dob?.let { "On planet Earth" } ?: "",
                accent = NeptuneAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private val NeptuneAccent: Color get() = Color(0xFF4ECDC4)

@Composable
private fun FactCard(
    eyebrow: String,
    primary: String,
    secondary: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassCardWithGlow(
        modifier = modifier,
        glowColor = accent,
        glowAlpha = 0.15f,
        shape = ShapeLg
    ) {
        // Pack content tightly at the top — fixed-height cards left a "hole"
        // because the SpaceBetween layout pushed the body to the bottom edge,
        // exposing the empty middle of the glass surface.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = Space.sm + Space.xxs),
            verticalArrangement = Arrangement.spacedBy(Space.xxs)
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Allow two lines so phrases like "Year of the Snake" don't clip on
            // narrow half-width cards.
            Text(
                text = primary,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            if (secondary.isNotBlank()) {
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun LifetimeProgressSection(uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState) {
    val dob = uiState.selectedDate ?: return
    val today = remember { LocalDate.now() }
    val period = remember(dob) { Period.between(dob, today) }
    val daysLived = remember(dob) {
        java.time.temporal.ChronoUnit.DAYS.between(dob, today).coerceAtLeast(0)
    }
    // Rough WHO-ish global average — the bar is a vibes indicator, not a forecast.
    val expectancyDays = 80.0 * 365.25
    val progress = (daysLived.toFloat() / expectancyDays.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.padding(horizontal = Space.md)) {
        SectionHeader(eyebrow = "WHERE YOU ARE", title = "Lifetime journey")
        Spacer(Modifier.height(Space.sm))

        GlassCard(modifier = Modifier.fillMaxWidth(), shape = ShapeLg) {
            Column(modifier = Modifier.padding(Space.md)) {
                // Curved gauge replaces the flat bar — feels more like a "dial"
                // and gives the percent something to live inside.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CurvedGauge(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    Column(
                        modifier = Modifier.padding(bottom = Space.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "%.0f%%".format(progress * 100),
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "of an average lifetime",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(Modifier.height(Space.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${period.years} yr lived",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${"%,d".format(daysLived)} sunrises",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileActionsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        ProfileSectionTitle("Quick actions")

        ActionRow(
            emoji = "💞",
            title = "Compatibility",
            subtitle = "See how you match with other signs",
            accent = PurpleAccent,
            onClick = { navController.navigate(Screen.CompatibilityList.route) }
        )
        ActionRow(
            emoji = "📜",
            title = "Birthday history",
            subtitle = "Events that share your day",
            accent = SaturnGold,
            onClick = { navController.navigate(Screen.BirthdayEvents.route) }
        )
        ActionRow(
            emoji = "⚙️",
            title = "Settings",
            subtitle = "Edit birth date, theme, notifications",
            accent = PrimaryNeon,
            onClick = { navController.navigate(Screen.Settings.route) }
        )
    }
}

@Composable
private fun ActionRow(
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accent),
                onClick = onClick
            )
            .pressableScale(interactionSource),
        shape = ShapeMd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(Space.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ProfileSectionTitle(text: String) {
    val eyebrow = when (text) {
        "You are" -> "IDENTITY"
        "Lifetime journey" -> "WHERE YOU ARE"
        "Quick actions" -> "DO MORE"
        else -> "PROFILE"
    }
    SectionHeader(eyebrow = eyebrow, title = text)
}

private fun decadeName(year: Int): String {
    val d = (year / 10) * 10
    return "${d}s"
}

private fun dayOfWeekName(date: LocalDate): String =
    date.dayOfWeek.getDisplayName(
        java.time.format.TextStyle.FULL,
        java.util.Locale.US
    )
