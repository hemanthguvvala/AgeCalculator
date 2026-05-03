package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.screen.components.CosmicTopBar
import com.hkgroups.agecalculator.ui.screen.components.GlassCard
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.theme.ShapeLg
import com.hkgroups.agecalculator.ui.theme.Space

/**
 * Privacy policy — bundled inline so it works offline and stays in the cosmic theme.
 * The previous version loaded a light-mode WebView from GitHub Pages which broke the
 * dark UI and required network.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
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
                    title = "Privacy",
                    subtitle = "Your birthday stays on your device",
                    onBack = { navController.popBackStack() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Space.md, vertical = Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    HeroPledge()

                    PolicySection(
                        title = "What we collect",
                        body = "Your birth date — when you choose to enter it. Optional mood log entries " +
                            "(date, mood, an optional one-line note). Optional rising and moon sign overrides. " +
                            "Streak counters and achievement progress. That's it for personal data."
                    )

                    PolicySection(
                        title = "Where it lives",
                        body = "On your device, in app-private storage (Android DataStore + Room). " +
                            "We do not sync, upload, mirror, or transmit it anywhere. There is no server, " +
                            "no account system, and no analytics SDK that would receive it."
                    )

                    PolicySection(
                        title = "Advertising",
                        body = "The free tier shows ads through Meta Audience Network (FAN). FAN may collect " +
                            "your device's advertising identifier to deliver and measure ads. It does not " +
                            "receive your birth date, mood entries, or streak data — none of those are sent " +
                            "off the device.\n\n" +
                            "To stop sharing your advertising ID with FAN: open Android Settings → Privacy → " +
                            "Ads, then delete or reset your advertising ID. The premium tier removes ads " +
                            "entirely."
                    )

                    PolicySection(
                        title = "Purchases",
                        body = "Premium upgrades go through Google Play Billing. Google receives the purchase " +
                            "details (product, price, payment method) directly — we never see your card " +
                            "number or address. We only learn whether you own the premium product so the " +
                            "app can flip you to ad-free."
                    )

                    PolicySection(
                        title = "Permissions",
                        body = "INTERNET, ACCESS_NETWORK_STATE — for ads, premium-tier purchases, and any " +
                            "future remote content.\n" +
                            "POST_NOTIFICATIONS — to deliver the daily horoscope, evening mood reminder, " +
                            "and cosmic-event alerts you opted into.\n" +
                            "VIBRATE — for haptic feedback on taps and milestones (toggleable in Settings).\n" +
                            "AD_ID — required by Android 13+ for the advertising network to serve relevant ads."
                    )

                    PolicySection(
                        title = "Removing your data",
                        body = "Open Settings → Reset App Data, or simply uninstall the app. Either action " +
                            "permanently removes your stored birth date, mood entries, rising/moon overrides, " +
                            "streak history, and any other locally-stored data."
                    )

                    PolicySection(
                        title = "Children",
                        body = "The app is suitable for general audiences and stores nothing identifying " +
                            "beyond a birth date the user enters. There is no account or login. Advertising " +
                            "is provided by Meta Audience Network, which has its own age-targeting controls."
                    )

                    PolicySection(
                        title = "Changes",
                        body = "If we ever change how data is handled, this policy will be updated and the " +
                            "version label below will change."
                    )

                    Spacer(Modifier.height(Space.md))
                    Text(
                        text = "Version 1.1 — Last updated May 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(Space.xl))
                }
            }
        }
    }
}

@Composable
private fun HeroPledge() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeLg
    ) {
        Column(modifier = Modifier.padding(Space.md)) {
            Text(
                text = "🛡️",
                fontSize = 28.sp
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                text = "Your birthday stays on this device",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Space.xxs))
            Text(
                text = "No accounts, no servers, no analytics. The only data this app keeps is the birth date you enter, and it never leaves your phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = SaturnGold,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, top = Space.sm, bottom = Space.xxs)
        )
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = ShapeLg) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 22.sp,
                modifier = Modifier.padding(Space.md)
            )
        }
    }
}
