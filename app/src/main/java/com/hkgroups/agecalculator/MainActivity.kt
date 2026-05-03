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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.hkgroups.agecalculator.ui.screen.components.FloatingNavBar
import com.hkgroups.agecalculator.ui.screen.components.NavItem
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.ZodiacAgeTheme
import com.hkgroups.agecalculator.ui.theme.rememberSignPalette
import com.hkgroups.agecalculator.util.CosmicFeedback
import com.hkgroups.agecalculator.util.LocalAdController
import com.hkgroups.agecalculator.util.LocalBillingController
import com.hkgroups.agecalculator.util.LocalConsentManager
import com.hkgroups.agecalculator.util.LocalCosmicFeedback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op — user choice persists in system settings */ }

    /** Asks for POST_NOTIFICATIONS *only after* the user has signaled intent
     *  (i.e. set a birth date). Pre-prompting on app start tanks opt-in rates. */
    fun maybeAskNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        // Drive UMP consent before FAN initializes. Idempotent — UMP fast-paths
        // for users in non-regulated regions or who have already consented.
        (application as ZodiacAgeApp).requestConsentAndInitAds(this)

        // Record today's check-in at the activity level so the streak fires
        // regardless of which screen the user lands on.
        lifecycleScope.launch {
            settingsRepository.recordCheckIn()
        }

        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = true)
            val savedBirthDate by settingsRepository.savedBirthDate.collectAsStateWithLifecycle(initialValue = null)
            val signName = remember(savedBirthDate) {
                savedBirthDate?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    zodiacSignNameFromDate(date)
                }
            }
            val signPalette = rememberSignPalette(signName)

            var splashShown by remember { mutableStateOf(true) }

            // Once user has a birth date, ask for notification permission (soft path).
            LaunchedEffect(savedBirthDate) {
                if (savedBirthDate != null) maybeAskNotificationPermission()
            }

            val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsStateWithLifecycle(initialValue = true)
            val chimesEnabled by settingsRepository.chimesEnabled.collectAsStateWithLifecycle(initialValue = false)
            val feedbackScope = androidx.compose.runtime.rememberCoroutineScope()
            val cosmicFeedback = remember {
                CosmicFeedback(
                    context = applicationContext,
                    scope = feedbackScope,
                    hapticsEnabled = { hapticsEnabled },
                    chimesEnabled = { chimesEnabled }
                )
            }

            val app = remember { application as ZodiacAgeApp }

            ZodiacAgeTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(
                    LocalSignPalette provides signPalette,
                    LocalCosmicFeedback provides cosmicFeedback,
                    LocalAdController provides app.adsController,
                    LocalBillingController provides app.billingController,
                    LocalConsentManager provides app.consentManager
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

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
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Main.route,
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
                                composable(
                                    route = Screen.Compatibility.route,
                                    arguments = listOf(
                                        navArgument(NavigationArgs.CompatibilityArgs.USER_SIGN_KEY) { type = NavType.StringType },
                                        navArgument(NavigationArgs.CompatibilityArgs.PARTNER_SIGN_KEY) { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val args = NavigationArgs.CompatibilityArgs.from(backStackEntry)
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

                        if (splashShown) {
                            com.hkgroups.agecalculator.ui.screen.components.CosmicSplash(
                                signName = signName,
                                onComplete = { splashShown = false }
                            )
                        }

                        val onboardingDone by settingsRepository.onboardingCompleted
                            .collectAsStateWithLifecycle(initialValue = true)
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

/** Sun-sign lookup from calendar date (no DB needed). */
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
