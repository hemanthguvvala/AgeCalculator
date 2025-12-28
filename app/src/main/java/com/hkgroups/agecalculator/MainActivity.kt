package com.hkgroups.agecalculator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.BirthdayEventsScreen
import com.hkgroups.agecalculator.ui.screen.CompatibilityDetailScreen
import com.hkgroups.agecalculator.ui.screen.CompatibilityListScreen
import com.hkgroups.agecalculator.ui.screen.HistoricalEventsScreen
import com.hkgroups.agecalculator.ui.screen.MainScreen
import com.hkgroups.agecalculator.ui.screen.SettingsScreen
import com.hkgroups.agecalculator.ui.screen.ZodiacDetailScreen
import com.hkgroups.agecalculator.ui.screen.ZodiacExplorerScreen
import com.hkgroups.agecalculator.ui.theme.ZodiacAgeTheme
import dagger.hilt.android.AndroidEntryPoint
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
            val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = false)
            ZodiacAgeTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route
                    ) {
                        composable(route = Screen.Main.route) {
                            MainScreen(navController = navController)
                        }

                        // Define the arguments for the compatibility route
                        composable(
                            route = Screen.Compatibility.route,
                            arguments = listOf(
                                navArgument("userSignName") { type = NavType.StringType },
                                navArgument("partnerSignName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            // Extract the arguments
                            val userSignName = backStackEntry.arguments?.getString("userSignName")
                            val partnerSignName =
                                backStackEntry.arguments?.getString("partnerSignName")

                            // Pass the arguments to the screen
                            CompatibilityDetailScreen(
                                navController = navController,
                                userSignName = userSignName,
                                partnerSignName = partnerSignName
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
                            arguments = listOf(navArgument("signName") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            ZodiacDetailScreen(
                                navController = navController,
                                signName = backStackEntry.arguments?.getString("signName")
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
                    }
                }
            }
        }
    }
}