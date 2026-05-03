package com.hkgroups.agecalculator.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.R
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.components.*
import com.hkgroups.agecalculator.ui.screen.components.staggeredEntrance
import com.hkgroups.agecalculator.ui.screen.components.tiltable3D
import com.hkgroups.agecalculator.ui.theme.*
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.util.*

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.selectedDate,
        transitionSpec = {
            fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
        },
        label = "Content Flow"
    ) { targetDate ->
        if (targetDate == null) {
            WelcomeScreen { timeInMillis ->
                viewModel.onDateSelected(timeInMillis)
            }
        } else {
            CosmicDashboardScreen(viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
fun WelcomeScreen(onDateSelected: (Long) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    // Stage gates: each unlocks the next layer of the entrance sequence.
    // Starts at 0 (orb only) and steps through 5 over ~1.5s.
    var stage by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120); stage = 1   // brand mark
        kotlinx.coroutines.delay(260); stage = 2   // headline
        kotlinx.coroutines.delay(180); stage = 3   // subtitle
        kotlinx.coroutines.delay(220); stage = 4   // feature peeks
        kotlinx.coroutines.delay(360); stage = 5   // CTA + disclaimer
    }

    val transition = rememberInfiniteTransition(label = "welcomeGlow")
    val glowScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcomeGlowScale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcomeGlowAlpha"
    )

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
                    .padding(horizontal = Space.lg),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Space.xl))
                StageReveal(visible = stage >= 1) {
                    Text(
                        text = "ZODAIC",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.55f),
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    // Hero orb is visible from frame 1 — anchor of the sequence.
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .scale(glowScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryNeon.copy(alpha = glowAlpha),
                                        PurpleAccent.copy(alpha = glowAlpha * 0.6f),
                                        Color.Transparent
                                    ),
                                    radius = 360f
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 72.sp)
                    }

                    Spacer(modifier = Modifier.height(Space.xl))

                    StageReveal(visible = stage >= 2) {
                        Text(
                            text = stringResource(R.string.welcome_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(Space.sm))

                    StageReveal(visible = stage >= 3) {
                        Text(
                            text = "Your age across the planets, your stars,\nyour year, your cosmic identity.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.65f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(Space.xl))

                    StageReveal(visible = stage >= 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            FeaturePeek(emoji = "🪐", label = "Planets", modifier = Modifier.weight(1f))
                            FeaturePeek(emoji = "♉", label = "Zodiac", modifier = Modifier.weight(1f))
                            FeaturePeek(emoji = "🔮", label = "Daily", modifier = Modifier.weight(1f))
                        }
                    }
                }

                StageReveal(visible = stage >= 5) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Space.xl)
                    ) {
                        Button(
                            onClick = { showPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                            shape = ShapeMd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.welcome_button_text),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(Space.sm))
                        Text(
                            text = "We never send your birthday anywhere. It lives only on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showPicker) {
        CosmicBirthDatePicker(
            onDismiss = { showPicker = false },
            onConfirm = { millis ->
                showPicker = false
                onDateSelected(millis)
            }
        )
    }
}

/**
 * StageReveal — slides + fades content in when [visible] flips to true.
 * Used to choreograph the welcome screen entrance.
 */
@Composable
private fun StageReveal(visible: Boolean, content: @Composable () -> Unit) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(
            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
        ) + androidx.compose.animation.slideInVertically(
            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}

@Composable
private fun FeaturePeek(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(ShapeMd)
            .background(SurfaceGlass)
            .border(1.dp, BorderGlass, ShapeMd)
            .padding(vertical = Space.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(Space.xxs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CosmicDashboardScreen(viewModel: MainViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val streak by viewModel.streakDays.collectAsState()
    val freezes by viewModel.streakFreezes.collectAsState()
    val moodInsight by viewModel.moodInsight.collectAsState()
    val questionOfTheDay by viewModel.questionOfTheDay.collectAsState()
    val hasAnsweredToday by viewModel.hasAnsweredQuestionToday.collectAsState()
    val birthdayWindowMessage by viewModel.birthdayWindowMessage.collectAsState()
    val cosmicSnapshot by viewModel.cosmicSnapshot.collectAsState()
    var liveAge by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Premium gating — drives both the weekly-forecast tease and the
    // upsell-sheet trigger from any premium-only feature on this screen.
    val billingController = com.hkgroups.agecalculator.util.LocalBillingController.current
    val isPremium by (billingController?.isPremium
        ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
    var showPremiumSheet by remember { mutableStateOf(false) }
    val weeklyForecast by viewModel.weeklyForecast.collectAsState()

    // Mood journal state — collected here so the card and the sheet share state.
    val moodEntries by viewModel.settingsRepository.moodEntries
        .collectAsState(initial = emptyList())
    val today = remember { java.time.LocalDate.now() }
    val todayMood = remember(moodEntries) { moodEntries.firstOrNull { it.date == today } }
    var showMoodSheet by remember { mutableStateOf(false) }

    // Record today's visit exactly once per dashboard composition (per app session).
    // The repository is idempotent within a calendar day.
    LaunchedEffect(Unit) {
        viewModel.checkInToday()
    }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true
            )
        }
    }

    // Live age ticker
    LaunchedEffect(uiState.selectedDate) {
        uiState.selectedDate?.let {
            viewModel.ageTicker(it).collect { newAgeMap ->
                liveAge = newAgeMap
            }
        }
    }
    
    // Scroll state hoisted so the hero can react to scroll position with a
    // parallax fade — the cosmic age scales down and dims as you scroll past it.
    val scrollState = rememberScrollState()

    // Streak milestone celebration trigger. Watching the streak value and
    // firing only when the user crosses a celebrated threshold.
    var streakCelebration by remember { mutableStateOf<Int?>(null) }
    var lastSeenStreak by remember { mutableStateOf(streak) }
    LaunchedEffect(streak) {
        if (streak != lastSeenStreak &&
            com.hkgroups.agecalculator.ui.screen.components.isStreakMilestone(streak)
        ) {
            streakCelebration = streak
        }
        lastSeenStreak = streak
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        StarryBackground {
            com.hkgroups.agecalculator.ui.screen.components.CosmicPullToRefresh(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refreshData() }
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(bottom = 160.dp)
            ) {
                // Header with title and avatar.
                // We pass the user's birth date explicitly so the header can
                // derive the sign name even before the Room-backed zodiac
                // signs list finishes loading. This keeps the greeting from
                // saying "Welcome back" for a beat after launch.
                CosmicHeader(
                    navController = navController,
                    viewModel = viewModel,
                    zodiacSign = uiState.zodiacSign,
                    selectedDate = uiState.selectedDate,
                    chineseZodiac = uiState.chineseZodiac
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Loading indicator
                if (uiState.isLoading) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            com.hkgroups.agecalculator.ui.screen.components.CosmicLoading(
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading cosmic data...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Age Ticker — passes scroll value for parallax fade.
                AgeTickerSection(
                    liveAge = liveAge,
                    planetCount = uiState.planetaryAges.size,
                    scrollOffset = scrollState.value
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Birthday window — fires only in the ±7-day solar return window.
                // Highest-engagement annual moment; pinned above the daily loop.
                birthdayWindowMessage?.let { msg ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        com.hkgroups.agecalculator.ui.screen.components.BirthdayBannerCard(
                            message = msg
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Daily engagement loop — streak, reveal, lucky cards, mood
                DailyEngagementSection(
                    horoscope = uiState.horoscope,
                    zodiacSign = uiState.zodiacSign,
                    streakDays = streak,
                    streakFreezes = freezes
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cosmic weather — today's sun-sign / moon / ruling planet
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    com.hkgroups.agecalculator.ui.screen.components.CosmicWeatherCard(
                        snapshot = cosmicSnapshot
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Question of the day — short reflection prompt that doubles as
                // engagement signal: an answered prompt is a stronger streak indicator
                // than a pure app-open.
                if (questionOfTheDay.isNotBlank()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        com.hkgroups.agecalculator.ui.screen.components.QuestionOfDayCard(
                            question = questionOfTheDay,
                            hasAnswered = hasAnsweredToday,
                            onSubmit = viewModel::answerQuestionOfTheDay
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Mood insight — appears once we have ≥ 5 mood entries and
                // detect a day-of-week / planetary correlation.
                moodInsight?.let { insight ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        com.hkgroups.agecalculator.ui.screen.components.MoodInsightCard(
                            insight = insight
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Planetary System Section
                if (uiState.planetaryAges.isNotEmpty()) {
                    PlanetarySystemSection(
                        planetaryAges = uiState.planetaryAges.associate { it.first to it.second }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Cosmic Identity Section
                CosmicIdentitySection(
                    zodiacSign = uiState.zodiacSign,
                    chineseZodiac = uiState.chineseZodiac,
                    dailyTip = uiState.dailyTip,
                    viewModel = viewModel,
                    navController = navController
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Daily mood journal — first action of the day.
                Column(modifier = Modifier.padding(horizontal = Space.md)) {
                    SectionTitle("Daily journal")
                    Spacer(Modifier.height(Space.sm))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeLg
                    ) {
                        com.hkgroups.agecalculator.ui.screen.components.MoodJournalCard(
                            todayEntry = todayMood,
                            recentEntries = moodEntries,
                            signName = uiState.zodiacSign?.name,
                            onLogToday = { showMoodSheet = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Tonight's sky — current moon phase + 7-day lunar strip.
                Column(modifier = Modifier.padding(horizontal = Space.md)) {
                    SectionTitle("Tonight's sky")
                    Spacer(Modifier.height(Space.sm))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeLg
                    ) {
                        Column {
                            com.hkgroups.agecalculator.ui.screen.components.MoonPhaseCard()
                            com.hkgroups.agecalculator.ui.screen.components.MoonPhaseStrip()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Upcoming cosmic events — retrogrades, eclipses, equinoxes
                Column(modifier = Modifier.padding(horizontal = Space.md)) {
                    SectionTitle("Cosmic events")
                    Spacer(Modifier.height(Space.sm))
                    val events = remember {
                        com.hkgroups.agecalculator.ui.screen.components.upcomingCosmicEvents()
                    }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeLg
                    ) {
                        com.hkgroups.agecalculator.ui.screen.components
                            .CosmicEventsSection(events = events)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 7-day forecast — premium-gated. Free users see the first day,
                // the rest hide behind a "Unlock" CTA that opens the upsell sheet.
                if (weeklyForecast.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = Space.md)) {
                        SectionTitle("Week ahead")
                        Spacer(Modifier.height(Space.sm))
                        com.hkgroups.agecalculator.ui.screen.components.WeeklyForecastSection(
                            forecast = weeklyForecast,
                            isPremium = isPremium,
                            onUpgradeTap = { showPremiumSheet = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Sponsored — single in-feed native ad. Placed below the
                // primary daily ritual content (mood, sky, events) so it never
                // interrupts the core flow.
                Column(modifier = Modifier.padding(horizontal = Space.md)) {
                    com.hkgroups.agecalculator.ui.screen.components.NativeAdCard()
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Time Capsule & Milestones
                TimeCapsuleSection(
                    birthYearTrivia = uiState.birthYearTrivia,
                    selectedDate = uiState.selectedDate,
                    daysUntilBirthday = uiState.daysUntilBirthday,
                    milestoneData = uiState.milestoneData
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick Actions
                QuickActionsSection(navController = navController)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Did you know? Section
                DidYouKnowSection(selectedDate = uiState.selectedDate)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            }
        }

        // Snackbar for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Streak milestone celebration — confetti + banner over the whole UI.
        com.hkgroups.agecalculator.ui.screen.components.StreakMilestoneOverlay(
            trigger = streakCelebration
        )
    }

    if (showMoodSheet) {
        com.hkgroups.agecalculator.ui.screen.components.MoodJournalSheet(
            initialMood = todayMood?.mood?.let {
                com.hkgroups.agecalculator.ui.screen.components.MoodOption.fromKey(it)
            },
            initialNote = todayMood?.note ?: "",
            onDismiss = { showMoodSheet = false },
            onSave = { mood, note ->
                viewModel.saveMoodEntry(today, mood.key, note)
                showMoodSheet = false
            }
        )
    }

    if (showPremiumSheet) {
        com.hkgroups.agecalculator.ui.screen.components.PremiumUpsellSheet(
            title = "Unlock the full week",
            subtitle = "Premium reveals the rest of your 7-day forecast plus every other premium feature, lifetime.",
            onDismiss = { showPremiumSheet = false }
        )
    }
}

@Composable
private fun CosmicHeader(
    navController: NavController,
    viewModel: MainViewModel,
    zodiacSign: ZodiacSign?,
    selectedDate: LocalDate?,
    chineseZodiac: String?
) {
    // Sign name fallback: when the Room signs list hasn't finished loading,
    // we still know the user's sign from their birth date alone — no DB
    // lookup needed. Prevents the "Welcome back" flash on launch.
    val effectiveSignName = zodiacSign?.name
        ?: selectedDate?.let {
            com.hkgroups.agecalculator.content.AstronomyEngine.sunSignOfDay(it)
        }
    val today = remember { LocalDate.now() }
    val greeting = remember(today) {
        val h = java.time.LocalTime.now().hour
        when {
            h < 5 -> "Good night"
            h < 12 -> "Good morning"
            h < 17 -> "Good afternoon"
            h < 21 -> "Good evening"
            else -> "Good night"
        }
    }
    val dateLabel = remember(today) {
        today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }
    val palette = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.md, vertical = Space.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = effectiveSignName?.let { "$it, you're glowing today." }
                    ?: "Welcome back",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Spacer(Modifier.width(Space.sm))

        // Settings button — small, secondary
        IconChip(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            onClick = { navController.navigate(Screen.Settings.route) }
        )

        Spacer(Modifier.width(Space.xs))

        // Zodiac avatar — tap bursts sparkles, then navigates to Profile.
        // SparkleBurst wraps the ring with a particle field overlay.
        com.hkgroups.agecalculator.ui.screen.components.SparkleBurst { emitter ->
            GradientBorderRing(
                size = 56.dp,
                strokeWidth = 1.5.dp,
                colors = listOf(palette.primary, palette.secondary, palette.primary),
                modifier = Modifier.clickable {
                    emitter.burst(
                        originXNorm = 0.5f,
                        originYNorm = 0.5f,
                        count = 18,
                        colors = listOf(palette.primary, palette.secondary, Color.White),
                        speed = 0.55f
                    )
                    navController.navigate(Screen.Profile.route)
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    palette.primary.copy(alpha = 0.30f),
                                    palette.secondary.copy(alpha = 0.12f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                        sign = effectiveSignName ?: "",
                        strokeColor = Color.White,
                        accentColor = palette.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AgeTickerSection(
    liveAge: List<Pair<String, String>>,
    planetCount: Int,
    scrollOffset: Int = 0
) {
    val yearsStr = liveAge.getOrNull(0)?.second ?: "0"
    val months = liveAge.getOrNull(1)?.second ?: "0"
    val days = liveAge.getOrNull(2)?.second ?: "0"
    val yearsInt = remember(yearsStr) { yearsStr.toIntOrNull() ?: 0 }
    val palette = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current

    // Parallax progress 0..1 over ~600px of scroll. As the user scrolls past
    // the hero, it shrinks (1.0 → 0.65), fades (1.0 → 0.25), and lifts
    // upward more slowly than the rest of the content, giving real depth.
    val parallaxProgress = (scrollOffset / 600f).coerceIn(0f, 1f)
    val heroScale = 1f - 0.35f * parallaxProgress
    val heroAlpha = 1f - 0.75f * parallaxProgress
    val heroTranslateY = -scrollOffset * 0.35f

    // Slow-breathing halo — single infinite transition driven via graphicsLayer
    // so it doesn't trigger recomposition on every frame.
    val haloTransition = rememberInfiniteTransition(label = "haloPulse")
    val haloScale by haloTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )
    val haloAlpha by haloTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.md, vertical = Space.lg)
            .graphicsLayer {
                scaleX = heroScale
                scaleY = heroScale
                alpha = heroAlpha
                translationY = heroTranslateY
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
            }
    ) {
        // Sign-tinted breathing halo — the palette + pulse make this feel
        // like a living centerpiece instead of a static background tint.
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = haloAlpha
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0.36f),
                            palette.secondary.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        radius = 540f
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR COSMIC AGE",
                style = MaterialTheme.typography.labelMedium,
                color = palette.primary.copy(alpha = 0.85f),
                letterSpacing = 5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Space.lg))

            // Hero number: 192sp — large enough to be the unmistakable
            // centerpiece of the screen, not a polite stat.
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                com.hkgroups.agecalculator.ui.screen.components.TumblingCounter(
                    target = yearsInt,
                    durationMillis = 1400,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 192.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 192.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = "yr",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = palette.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 36.dp)
                )
            }

            Spacer(modifier = Modifier.height(Space.xs))

            // Months · days pill — single chip is calmer than three chips
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceGlass)
                    .border(1.dp, BorderGlass, CircleShape)
                    .padding(horizontal = Space.md, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$months mo",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = Space.xs)
                        .size(3.dp)
                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                )
                Text(
                    text = "$days days",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = Space.xs)
                        .size(3.dp)
                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                )
                Text(
                    text = "on Earth",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun PlanetarySystemSection(
    planetaryAges: Map<String, String>
) {
    // Order + visuals come from a single source so the dashboard stays in sync with
    // CosmicUtils.planets when planets are added/removed.
    data class PlanetUi(val name: String, val color: Color, val emoji: String)
    val planetUi = listOf(
        PlanetUi("Mercury", MercuryGray, "☿️"),
        PlanetUi("Venus", VenusGold, "♀️"),
        PlanetUi("Earth", EarthBlue, "🌍"),
        PlanetUi("Mars", MarsRed, "🔴"),
        PlanetUi("Jupiter", JupiterBeige, "🪐"),
        PlanetUi("Saturn", SaturnGold, "🪐"),
        PlanetUi("Uranus", UranusGreen, "🌐"),
        PlanetUi("Neptune", NeptuneBlue, "🔵")
    )

    Column(modifier = Modifier.padding(horizontal = Space.md)) {
        SectionTitle("Planetary relativity")
        Text(
            text = "Swipe to drift across the solar system",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(Space.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            planetUi.forEach { planet ->
                planetaryAges[planet.name]?.let { age ->
                    PlanetCard(
                        planetName = planet.name.uppercase(),
                        planetAge = age,
                        planetColor = planet.color,
                        planetImage = {
                            Text(
                                text = planet.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmicIdentitySection(
    zodiacSign: com.hkgroups.agecalculator.data.model.ZodiacSign?,
    chineseZodiac: String?,
    dailyTip: String?,
    viewModel: MainViewModel,
    navController: NavController
) {
    if (chineseZodiac == null && zodiacSign == null) return

    Column(
        modifier = Modifier.padding(horizontal = Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        SectionTitle("Your cosmic identity")

        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            // Western zodiac tile — uses the actual sign name to drive both
            // copy and the custom-drawn glyph.
            zodiacSign?.let { z ->
                IdentityTile(
                    eyebrow = "WESTERN",
                    title = z.name,
                    subtitle = z.element ?: "Stars aligned",
                    glyph = z.name,
                    isWestern = true,
                    accent = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current.primary,
                    onClick = {
                        navController.navigate(Screen.ZodiacDetail.createRoute(z.name))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            // Chinese zodiac tile — keeps the emoji animal, but with new glyph slot.
            chineseZodiac?.let { cz ->
                IdentityTile(
                    eyebrow = "CHINESE",
                    title = "Year of the $cz",
                    subtitle = com.hkgroups.agecalculator.util.CosmicUtils
                        .getChineseZodiacDescription(cz)
                        .substringBefore("—")
                        .trim()
                        .removeSuffix(",")
                        .trim()
                        .ifBlank { "Born in the cycle" },
                    glyph = com.hkgroups.agecalculator.util.CosmicUtils.getChineseZodiacEmoji(cz),
                    isWestern = false,
                    accent = SaturnGold,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IdentityTile(
    eyebrow: String,
    title: String,
    subtitle: String,
    glyph: String,
    isWestern: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCardWithGlow(
        modifier = modifier.tiltable3D(maxAngle = 6f, onTap = onClick),
        glowColor = accent,
        glowAlpha = 0.18f,
        shape = ShapeLg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isWestern) {
                    com.hkgroups.agecalculator.ui.screen.components.ZodiacGlyph(
                        sign = glyph,
                        strokeColor = Color.White,
                        accentColor = accent,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    Text(text = glyph, fontSize = 30.sp)
                }
            }
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 3
            )
        }
    }
}

private fun westernZodiacGlyph(name: String): String = when (name) {
    "Aries" -> "♈"; "Taurus" -> "♉"; "Gemini" -> "♊"; "Cancer" -> "♋"
    "Leo" -> "♌"; "Virgo" -> "♍"; "Libra" -> "♎"; "Scorpio" -> "♏"
    "Sagittarius" -> "♐"; "Capricorn" -> "♑"; "Aquarius" -> "♒"; "Pisces" -> "♓"
    else -> "✨"
}

/**
 * Local section title that pairs with the new shared SectionHeader component
 * (eyebrow + title) so every dashboard section reads the same. Maps the
 * single `text` to a sensible eyebrow per known section name; falls back to
 * a generic eyebrow otherwise.
 */
@Composable
private fun SectionTitle(text: String) {
    val eyebrow = when (text) {
        "Planetary relativity" -> "DRIFT"
        "Your cosmic identity" -> "WHO YOU ARE"
        "Explore further" -> "GO DEEPER"
        "Cosmic events" -> "WHAT'S COMING"
        else -> "COSMOS"
    }
    SectionHeader(eyebrow = eyebrow, title = text)
}

@Composable
private fun TimeCapsuleSection(
    birthYearTrivia: String?,
    selectedDate: java.time.LocalDate?,
    daysUntilBirthday: Int?,
    milestoneData: com.hkgroups.agecalculator.ui.viewmodel.MilestoneData?
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Birth Year Trivia
        birthYearTrivia?.let { trivia ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeLg
            ) {
                Column(modifier = Modifier.padding(Space.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SaturnGold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📅", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(Space.sm))
                        Column {
                            Text(
                                text = "Time capsule",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            selectedDate?.let {
                                Text(
                                    text = "Year ${it.year}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SaturnGold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Space.sm))
                    Text(
                        text = trivia,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.sm))
        }

        // Birthday & Milestone — split into two clean rows with a divider
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeLg
        ) {
            Column(modifier = Modifier.padding(Space.md)) {
                daysUntilBirthday?.let { days ->
                    InfoRow(
                        emoji = "🎂",
                        title = "Next birthday",
                        value = if (days == 0) "Today 🎉" else "in $days days"
                    )
                }
                if (daysUntilBirthday != null && milestoneData != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Space.sm)
                            .height(1.dp)
                            .background(BorderGlass)
                    )
                }
                milestoneData?.let { milestone ->
                    InfoRow(
                        emoji = "🎯",
                        title = "Upcoming milestone",
                        value = "${"%,d".format(milestone.dayCount)} days · " +
                            milestone.date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(emoji: String, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceGlass),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(Space.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuickActionsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        SectionTitle("Explore further")

        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            ActionTile(
                emoji = "🌍",
                title = "Lifetime",
                subtitle = "Events since birth",
                accent = NeptuneBlue,
                onClick = { navController.navigate(Screen.History.route) },
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                emoji = "🎂",
                title = "Your day",
                subtitle = "Things that happened",
                accent = MarsRed,
                onClick = { navController.navigate(Screen.BirthdayEvents.route) },
                modifier = Modifier.weight(1f)
            )
        }

        // Primary CTA — full width, accent
        Button(
            onClick = { navController.navigate(Screen.ZodiacExplorer.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
            shape = ShapeMd
        ) {
            Text(
                text = "Explore the zodiac",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionTile(
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.tiltable3D(maxAngle = 6f, onTap = onClick),
        shape = ShapeMd
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DidYouKnowSection(selectedDate: java.time.LocalDate?) {
    // Rotates through cosmic facts (distinct from birth-year trivia) using a seed
    // derived from birth-day-of-year + today's day-of-year, so the fact is stable
    // for a single calendar day but varies across visits.
    val today = remember { LocalDate.now() }
    val seed = remember(selectedDate, today) {
        val birthSeed = selectedDate?.dayOfYear ?: 0
        kotlin.math.abs(birthSeed * 31 + today.dayOfYear * 17 + today.year)
    }
    val fact = remember(seed) { com.hkgroups.agecalculator.util.CosmicUtils.getCosmicDidYouKnow(seed) }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lightbulb icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryNeon.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Did you know?",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fact,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

/**
 * CosmicBirthDatePicker — wraps the custom three-wheel CosmicDatePicker.
 *
 * Maintains the millis-based API that existing callers (Welcome, Settings)
 * depend on, while delegating the actual UI to the on-brand wheel picker.
 */
@Composable
internal fun CosmicBirthDatePicker(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    initialMillis: Long? = null
) {
    val zone = remember { java.time.ZoneId.systemDefault() }
    val initialDate = remember(initialMillis) {
        initialMillis?.let {
            java.time.Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
        } ?: LocalDate.now().minusYears(25)
    }
    val today = remember { LocalDate.now() }

    com.hkgroups.agecalculator.ui.screen.components.CosmicDatePicker(
        initialDate = initialDate,
        minYear = 1900,
        maxYear = today.year,
        onDismiss = onDismiss,
        onConfirm = { picked ->
            val safe = if (picked.isAfter(today)) today else picked
            val millis = safe.atStartOfDay(zone).toInstant().toEpochMilli()
            onConfirm(millis)
        }
    )
}

// ---------- Daily engagement section (streak, reveal, lucky, mood) ----------

@Composable
private fun DailyEngagementSection(
    horoscope: String?,
    zodiacSign: ZodiacSign?,
    streakDays: Int,
    streakFreezes: Int = 0
) {
    val today = remember { LocalDate.now() }
    val signName = zodiacSign?.name ?: "Cosmic"
    val seedKey = "$today-$signName"
    val seed = remember(seedKey) { kotlin.math.abs(seedKey.hashCode()) }

    val luckyNumber = remember(seed) { (seed % 99) + 1 }
    val (luckyColorName, luckyColor) = remember(seed) { dailyLuckyColor(seed) }

    Column(modifier = Modifier.padding(horizontal = 0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            com.hkgroups.agecalculator.ui.screen.components.StreakPillWithFreezes(
                days = streakDays,
                freezes = streakFreezes
            )
        }

        Spacer(Modifier.height(14.dp))

        // Multi-facet forecast — swipeable through Reading, Mood, Love,
        // Energy, Focus. Each facet has its own emoji, accent, and body.
        com.hkgroups.agecalculator.ui.screen.components.DailyForecastPager(
            horoscope = horoscope,
            seed = seed
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LuckyNumberCard(
                luckyNumber = luckyNumber,
                modifier = Modifier.weight(1f)
            )
            LuckyColorCard(
                colorName = luckyColorName,
                color = luckyColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun dailyLuckyColor(seed: Int): Pair<String, Color> {
    val palette = listOf(
        "Cosmic Violet" to Color(0xFF9B59B6),
        "Stardust Pink" to Color(0xFFFF6B9D),
        "Nebula Teal" to Color(0xFF4ECDC4),
        "Solar Gold" to Color(0xFFFFC857),
        "Mercury Silver" to Color(0xFFB8B8B8),
        "Mars Crimson" to Color(0xFFFF6B6B),
        "Neptune Blue" to Color(0xFF4D96FF),
        "Saturn Bronze" to Color(0xFFE0C097)
    )
    return palette[seed % palette.size]
}

private fun dailyMood(seed: Int): Triple<String, String, String> {
    val moods = listOf(
        Triple("🌟", "Radiant", "Confidence flows through you today"),
        Triple("🌙", "Reflective", "A quiet day for inner work"),
        Triple("⚡", "Charged", "Channel your energy into one big move"),
        Triple("🌊", "Flowing", "Let the day unfold without forcing it"),
        Triple("🔥", "Bold", "Speak the thing you've been holding back"),
        Triple("✨", "Magnetic", "People will gravitate toward your light"),
        Triple("🌸", "Tender", "Be gentle with yourself and others")
    )
    return moods[seed % moods.size]
}

private fun dailyHoroscopeFallback(seed: Int, sign: ZodiacSign?): String {
    val signLabel = sign?.name ?: "your sign"
    val templates = listOf(
        "The stars favor $signLabel today. Trust an instinct you've been doubting — it's pointing somewhere worth going. One small choice this morning shapes the rest of the day.",
        "Today asks $signLabel to slow down before deciding. Energy is plentiful but scattered — pick the one thing that matters and let the rest wait.",
        "A quiet conversation reveals more than a loud one for $signLabel today. Listen for what isn't said. The cosmos rewards patience.",
        "$signLabel, your intuition is sharper than usual. Don't second-guess the first answer that surfaces — it's the right one. Make the call.",
        "An old idea returns with new energy for $signLabel today. What you set aside last month is worth picking back up. Trust the timing."
    )
    return templates[seed % templates.size]
}
