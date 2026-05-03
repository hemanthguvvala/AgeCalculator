package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.hkgroups.agecalculator.BuildConfig
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.GlassCard
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.Space
import com.hkgroups.agecalculator.ui.theme.SurfaceGlass
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.settingsRepository.isDarkMode
        .collectAsState(initial = true)
    val notificationsEnabled by viewModel.settingsRepository.notificationsEnabled
        .collectAsState(initial = true)
    val hapticsEnabled by viewModel.settingsRepository.hapticsEnabled
        .collectAsState(initial = true)
    val chimesEnabled by viewModel.settingsRepository.chimesEnabled
        .collectAsState(initial = false)
    val cosmicEventsEnabled by viewModel.settingsRepository.cosmicEventNotificationsEnabled
        .collectAsState(initial = true)
    val uiState by viewModel.uiState.collectAsState()

    // Billing — re-query ownership every time Settings opens so purchases
    // made outside the app (or pending purchases that just resolved) are
    // reflected in the UI without a relaunch.
    val billingController = com.hkgroups.agecalculator.util.LocalBillingController.current
    val isPremium by (billingController?.isPremium
        ?: kotlinx.coroutines.flow.MutableStateFlow(false))
        .collectAsState()
    val productDetails by (billingController?.productDetails
        ?: kotlinx.coroutines.flow.MutableStateFlow(null))
        .collectAsState()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        billingController?.queryOwnership()
    }
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showBirthEditor by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
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
                    title = "Settings",
                    subtitle = "Tune your cosmic experience",
                    onBack = { navController.popBackStack() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Premium upsell — first thing in settings so the value
                    // prop is visible without scrolling. Replaced by a "You're
                    // a member" badge once the user owns premium_lifetime.
                    PremiumCard(
                        isPremium = isPremium,
                        priceLabel = productDetails
                            ?.oneTimePurchaseOfferDetails?.formattedPrice,
                        onUpgrade = {
                            if (activity != null) {
                                billingController?.launchPurchase(activity)
                            }
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    SettingsSectionTitle("APPEARANCE")
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        SettingsRow(
                            emoji = "🌑",
                            accent = PrimaryNeon,
                            title = "Dark Mode",
                            subtitle = "Cosmic deep-space aesthetic",
                            trailing = {
                                com.hkgroups.agecalculator.ui.screen.components.CosmicSwitch(
                                    checked = isDarkMode,
                                    onCheckedChange = { viewModel.onThemeSelected(it) }
                                )
                            }
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsSectionTitle("NOTIFICATIONS")
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            SettingsRow(
                                emoji = "🔔",
                                accent = SaturnGold,
                                title = "Daily Horoscope",
                                subtitle = "Forecast every morning at 8am",
                                trailing = {
                                    com.hkgroups.agecalculator.ui.screen.components.CosmicSwitch(
                                        checked = notificationsEnabled,
                                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                                    )
                                }
                            )
                            SettingsRow(
                                emoji = "🪐",
                                accent = PurpleAccent,
                                title = "Cosmic Events",
                                subtitle = "Reminder a day before eclipses, retrogrades, equinoxes",
                                trailing = {
                                    com.hkgroups.agecalculator.ui.screen.components.CosmicSwitch(
                                        checked = cosmicEventsEnabled,
                                        onCheckedChange = {
                                            viewModel.setCosmicEventNotificationsEnabled(it)
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsSectionTitle("FEEDBACK")
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            SettingsRow(
                                emoji = "✨",
                                accent = PrimaryNeon,
                                title = "Haptics",
                                subtitle = "Subtle vibration on taps and milestones",
                                trailing = {
                                    com.hkgroups.agecalculator.ui.screen.components.CosmicSwitch(
                                        checked = hapticsEnabled,
                                        onCheckedChange = { viewModel.setHapticsEnabled(it) }
                                    )
                                }
                            )
                            SettingsRow(
                                emoji = "🎵",
                                accent = SaturnGold,
                                title = "Chimes",
                                subtitle = "Soft tone on streak milestones (off by default)",
                                trailing = {
                                    com.hkgroups.agecalculator.ui.screen.components.CosmicSwitch(
                                        checked = chimesEnabled,
                                        onCheckedChange = { viewModel.setChimesEnabled(it) }
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsSectionTitle("BIRTH DATE")
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PurpleAccent.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🎂", fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(Space.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    val birthLabel = uiState.selectedDate
                                        ?.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                        ?: "Not set"
                                    Text(
                                        text = "Your birth date",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = birthLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showBirthEditor = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Change birth date", color = PrimaryNeon)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsSectionTitle("DATA")
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SaturnGold.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🧹", fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(Space.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Reset app data",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Clear birth date and return to welcome.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearData()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Data reset successfully")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset", color = SaturnGold)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SettingsSectionTitle("ABOUT")
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedButton(
                                onClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Privacy Policy", color = Color.White)
                            }
                        }
                    }

                    // App version footer — small, calm, signs off the screen.
                    Spacer(Modifier.height(28.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ZODAIC",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                    }
                    Spacer(Modifier.height(64.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showBirthEditor) {
        val initialMillis = uiState.selectedDate?.let {
            it.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        CosmicBirthDatePicker(
            initialMillis = initialMillis,
            onDismiss = { showBirthEditor = false },
            onConfirm = { millis ->
                showBirthEditor = false
                viewModel.onDateSelected(millis)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Birth date updated")
                }
            }
        )
    }
}

@Composable
private fun PremiumCard(
    isPremium: Boolean,
    priceLabel: String?,
    onUpgrade: () -> Unit
) {
    val palette = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    SaturnGold.copy(alpha = 0.55f),
                                    palette.primary.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isPremium) "★" else "✦",
                         color = SaturnGold,
                         fontWeight = FontWeight.Bold,
                         fontSize = 22.sp)
                }
                Spacer(Modifier.width(Space.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPremium) "PREMIUM MEMBER" else "GO PREMIUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = SaturnGold,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (isPremium) "Thank you for supporting Zodaic"
                               else "Unlock the full cosmos",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!isPremium) {
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PremiumBenefit(text = "Ad-free experience across the app")
                    PremiumBenefit(text = "Detailed birth chart with rising + moon")
                    PremiumBenefit(text = "Unlimited compatibility readings")
                    PremiumBenefit(text = "Future premium features included")
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SaturnGold)
                        .clickable(onClick = onUpgrade)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priceLabel?.let { "Upgrade · $it" }
                            ?: "Upgrade",
                        color = Color(0xFF0B0E1F),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumBenefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "✓", color = SaturnGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    val palette = com.hkgroups.agecalculator.ui.theme.LocalSignPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 10.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 2.dp)
                .clip(CircleShape)
                .background(palette.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = palette.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    emoji: String? = null,
    accent: Color = PrimaryNeon,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (emoji != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.width(Space.sm))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
        trailing()
    }
}
