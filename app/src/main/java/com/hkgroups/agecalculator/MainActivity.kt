package com.hkgroups.agecalculator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hkgroups.agecalculator.ui.screen.components.FloatingNavBar
import com.hkgroups.agecalculator.ui.screen.components.NavItem
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.ui.navigation.NavigationArgs
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.BirthdayEventsScreen
import com.hkgroups.agecalculator.ui.screen.CompatibilityDetailScreen
import com.hkgroups.agecalculator.ui.screen.CompatibilityListScreen
import com.hkgroups.agecalculator.ui.screen.CosmicProfileScreen
import com.hkgroups.agecalculator.ui.screen.HistoricalEventsScreen
import com.hkgroups.agecalculator.ui.screen.MainScreen
import com.hkgroups.agecalculator.ui.screen.PrivacyPolicyScreen
import com.hkgroups.agecalculator.ui.screen.SettingsScreen
import com.hkgroups.agecalculator.ui.screen.ZodiacDetailScreen
import com.hkgroups.agecalculator.ui.screen.ZodiacExplorerScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SignPalette
import com.hkgroups.agecalculator.ui.theme.ZodiacAgeTheme
import com.hkgroups.agecalculator.ui.theme.rememberSignPalette
import com.hkgroups.agecalculator.util.CosmicFeedback
import com.hkgroups.agecalculator.util.LocalAdController
import com.hkgroups.agecalculator.util.LocalBillingController
import com.hkgroups.agecalculator.util.LocalCosmicFeedback
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // --- NEW: Activity Result Launcher for Notification Permission ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // We can handle the result here if needed, e.g., show a message
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level 33+ (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Install splash screen
        installSplashScreen()
        
        askNotificationPermission()
        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = true)
            // Only show the floating nav once the user has actually set a birth date —
            // on the welcome screen the nav has nothing to show and just covers the CTA.
            val savedBirthDate by settingsRepository.savedBirthDate.collectAsState(initial = null)
            // Derive the user's sun-sign name from their saved birth date so the
            // whole app re-tints itself with their personal palette. Lookup is
            // pure date arithmetic — no DB required, so the palette is ready
            // before signs finish loading from Room.
            val signName = remember(savedBirthDate) {
                savedBirthDate?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    zodiacSignNameFromDate(date)
                }
            }
            val signPalette = rememberSignPalette(signName)

            // In-app splash — runs after the system splash dismisses, fades
            // out automatically. We render the rest of the app underneath
            // immediately so navigation/state initialize during the splash.
            var splashShown by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(true)
            }

            // Wire haptics + chime feedback once per session. Hot-read both flags
            // so the user can disable them mid-flight without restarting.
            val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsState(initial = true)
            val chimesEnabled by settingsRepository.chimesEnabled.collectAsState(initial = false)
            val feedbackScope = androidx.compose.runtime.rememberCoroutineScope()
            val cosmicFeedback = remember {
                CosmicFeedback(
                    context = applicationContext,
                    scope = feedbackScope,
                    hapticsEnabled = { hapticsEnabled },
                    chimesEnabled = { chimesEnabled }
                )
            }

            // Pull the process-scoped FAN controller out of the Application
            // so every composable can request ads via LocalAdController.
            val adsController = remember {
                (application as ZodiacAgeApp).adsController
            }
            val billingController = remember {
                (application as ZodiacAgeApp).billingController
            }

            ZodiacAgeTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(
                    LocalSignPalette provides signPalette,
                    LocalCosmicFeedback provides cosmicFeedback,
                    LocalAdController provides adsController,
                    LocalBillingController provides billingController
                ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Top-level destinations that show the floating nav bar.
                    val topLevelRoutes = listOf(
                        Screen.Main.route,
                        Screen.ZodiacExplorer.route,
                        Screen.CompatibilityList.route,
                        Screen.Profile.route
                    )
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route
                    val showBottomNav = currentRoute in topLevelRoutes && savedBirthDate != null
                    val selectedIndex = topLevelRoutes
                        .indexOf(currentRoute)
                        .coerceAtLeast(0)

                    Box(modifier = Modifier.fillMaxSize()) {
                    // Cohesive screen transitions: slide-in + fade enter, fade
                    // + scale exit. The pop variants reverse the slide so back
                    // gestures feel like a "lift away" rather than a re-mount.
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route,
                        // Snappy 200ms transitions — long enough to feel
                        // intentional, short enough to never feel laggy.
                        enterTransition = {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(180)
                            ) + androidx.compose.animation.scaleIn(
                                initialScale = 0.98f,
                                animationSpec = androidx.compose.animation.core.tween(180)
                            )
                        },
                        exitTransition = {
                            androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(140)
                            )
                        },
                        popEnterTransition = {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(160)
                            )
                        },
                        popExitTransition = {
                            androidx.compose.animation.fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(140)
                            ) + androidx.compose.animation.scaleOut(
                                targetScale = 0.98f,
                                animationSpec = androidx.compose.animation.core.tween(140)
                            )
                        }
                    ) {
                        composable(route = Screen.Main.route) {
                            MainScreen(navController = navController)
                        }

                        // Define the arguments for the compatibility route
                        composable(
                            route = Screen.Compatibility.route,
                            arguments = listOf(
                                navArgument(NavigationArgs.CompatibilityArgs.USER_SIGN_KEY) { type = NavType.StringType },
                                navArgument(NavigationArgs.CompatibilityArgs.PARTNER_SIGN_KEY) { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            // Extract the arguments using type-safe helper
                            val args = NavigationArgs.CompatibilityArgs.from(backStackEntry)

                            // Pass the arguments to the screen
                            CompatibilityDetailScreen(
                                navController = navController,
                                userSignName = args?.userSignName,
                                partnerSignName = args?.partnerSignName
                            )
                        }

                        composable(route = Screen.History.route) {
                            HistoricalEventsScreen(navController = navController)
                        }

                        composable(route = Screen.ZodiacExplorer.route) {
                            ZodiacExplorerScreen(navController = navController)
                        }

                        composable(
                            route = Screen.ZodiacDetail.route,
                            arguments = listOf(navArgument(NavigationArgs.ZodiacDetailArgs.SIGN_NAME_KEY) {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val args = NavigationArgs.ZodiacDetailArgs.from(backStackEntry)
                            ZodiacDetailScreen(
                                navController = navController,
                                signName = args?.signName
                            )
                        }
                        composable(route = Screen.CompatibilityList.route) {
                            CompatibilityListScreen(navController = navController)
                        }

                        composable(route = Screen.BirthdayEvents.route) {
                            BirthdayEventsScreen(navController = navController)
                        }

                        composable(route = Screen.Settings.route) {
                            SettingsScreen(navController = navController)
                        }

                        composable(route = Screen.Profile.route) {
                            CosmicProfileScreen(navController = navController)
                        }

                        composable(route = Screen.PrivacyPolicy.route) {
                            PrivacyPolicyScreen(navController = navController)
                        }
                    }

                    // FAN banner pinned just above the floating nav on
                        // top-level screens. Sub-screens (compatibility
                        // detail, settings) are ad-free for focus.
                        if (showBottomNav) {
                            com.hkgroups.agecalculator.ui.screen.components.BannerAdHost(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 84.dp)
                            )
                        }

                    if (showBottomNav) {
                        FloatingNavBar(
                            items = listOf(
                                // Labels intentionally match the actual destination
                                // (the previous "Explore"/"Zodiac" labels both led to
                                // zodiac-themed screens and confused users).
                                NavItem(Icons.Default.Home, "Today"),
                                NavItem(Icons.Default.Star, "Signs"),
                                NavItem(Icons.Default.Favorite, "Match"),
                                NavItem(Icons.Default.Person, "Profile")
                            ),
                            selectedIndex = selectedIndex,
                            onItemSelected = { idx ->
                                val route = topLevelRoutes[idx]
                                if (route != currentRoute) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        )
                    }
                    }

                    // Animated cosmic splash overlay — fades in/out on top of
                    // the actual UI so navigation state initializes underneath.
                    if (splashShown) {
                        com.hkgroups.agecalculator.ui.screen.components.CosmicSplash(
                            signName = signName,
                            onComplete = { splashShown = false }
                        )
                    }

                    // First-run onboarding — only after splash is gone, only
                    // for users who've already entered a birth date and not yet
                    // seen the tour. (Brand-new users get the welcome screen
                    // first; the tour fires once they land on the dashboard.)
                    val onboardingDone by settingsRepository.onboardingCompleted
                        .collectAsState(initial = true)
                    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                    val shouldShowTour = !splashShown &&
                        savedBirthDate != null &&
                        !onboardingDone
                    if (shouldShowTour) {
                        com.hkgroups.agecalculator.ui.screen.components.OnboardingOverlay(
                            onComplete = {
                                coroutineScope.launch {
                                    settingsRepository.markOnboardingComplete()
                                }
                            }
                        )
                    }
                }
                }
            }
        }
    }
}

/** Sun-sign lookup that depends only on the calendar date (no DB needed). */
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