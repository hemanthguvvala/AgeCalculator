package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.util.LocalBillingController

/**
 * PremiumUpsellSheet — modal bottom sheet shown when a free user taps a
 * premium-only feature. Drives the same one-time `premium_lifetime` SKU as the
 * Settings → Premium card. Auto-dismisses on successful purchase via the
 * `isPremium` flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumUpsellSheet(
    title: String = "Unlock the full cosmos",
    subtitle: String = "Premium opens every feature, removes all ads, lifetime access.",
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val billingController = LocalBillingController.current
    val activity = androidx.activity.compose.LocalActivity.current

    val isPremium by (billingController?.isPremium
        ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
    val productDetails by (billingController?.productDetails
        ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    val priceLabel = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice

    // If the user purchases while the sheet is open, dismiss automatically.
    androidx.compose.runtime.LaunchedEffect(isPremium) {
        if (isPremium) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF15122B),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SaturnGold.copy(alpha = 0.55f),
                                PrimaryNeon.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", color = SaturnGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "PREMIUM",
                style = MaterialTheme.typography.labelSmall,
                color = SaturnGold,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Benefit("Ad-free across the entire app")
                Benefit("7-day cosmic forecast")
                Benefit("Detailed compatibility narratives")
                Benefit("Birth chart with rising + moon")
                Benefit("Lifetime — one purchase, no subscription")
            }
            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SaturnGold)
                    .clickable(enabled = activity != null && billingController != null) {
                        if (activity != null) billingController?.launchPurchase(activity)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priceLabel?.let { "Unlock · $it" } ?: "Unlock Premium",
                    color = Color(0xFF0B0E1F),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Maybe later",
                    color = Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun Benefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(SaturnGold.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = SaturnGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.88f)
        )
    }
}

/**
 * WeeklyForecastSection — premium-gated 7-day forecast card. Shows a teaser
 * with the first day visible + the rest blurred-out for free users; full list
 * for premium users.
 */
@Composable
fun WeeklyForecastSection(
    forecast: List<String>,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    onUpgradeTap: () -> Unit
) {
    if (forecast.isEmpty()) return
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "7-day forecast",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                if (!isPremium) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SaturnGold.copy(alpha = 0.2f))
                            .border(1.dp, SaturnGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaturnGold,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            forecast.forEachIndexed { index, line ->
                val locked = !isPremium && index > 0
                ForecastRow(line = line, locked = locked)
                if (index < forecast.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (!isPremium) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaturnGold)
                        .clickable(onClick = onUpgradeTap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unlock the rest of the week",
                        color = Color(0xFF0B0E1F),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastRow(line: String, locked: Boolean) {
    val parts = line.split(":", limit = 2)
    val day = parts.getOrNull(0)?.trim() ?: ""
    val body = parts.getOrNull(1)?.trim() ?: line

    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.width(48.dp)
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                color = if (locked) Color.White.copy(alpha = 0.3f) else SaturnGold,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.width(12.dp))
        if (locked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔒  Hidden — premium",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        } else {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
