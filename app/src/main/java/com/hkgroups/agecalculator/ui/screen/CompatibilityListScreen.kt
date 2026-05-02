package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.ui.screen.components.GlassCardWithGlow
import com.hkgroups.agecalculator.ui.screen.components.StarryBackground
import com.hkgroups.agecalculator.ui.screen.components.pressableScale
import com.hkgroups.agecalculator.ui.theme.BackgroundDark
import com.hkgroups.agecalculator.ui.theme.PrimaryNeon
import com.hkgroups.agecalculator.ui.theme.PurpleAccent
import com.hkgroups.agecalculator.ui.theme.SaturnGold
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityListScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allSigns by viewModel.zodiacSignsState.collectAsState()
    val userSign = uiState.zodiacSign

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (userSign != null)
                            "${userSign.name} Compatibility"
                        else "Compatibility",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                when {
                    userSign == null -> EmptyCompatState(
                        title = "No sign selected yet",
                        message = "Set your birthday on the dashboard to see your cosmic matches."
                    )
                    allSigns.isEmpty() -> EmptyCompatState(
                        title = "Aligning the cosmos...",
                        message = "Loading zodiac data."
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val partners = allSigns.filter { it.name != userSign.name }
                        items(partners) { partner ->
                            CompatibilityRowCard(userSign = userSign, partnerSign = partner)
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCompatState(title: String, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CompatibilityRowCard(userSign: ZodiacSign, partnerSign: ZodiacSign) {
    val info = userSign.compatibilities.find { it.signName == partnerSign.name }
    val rating = info?.rating ?: 1
    val accent = when {
        rating >= 4 -> SaturnGold
        rating >= 3 -> PrimaryNeon
        else -> PurpleAccent
    }
    val interactionSource = remember { MutableInteractionSource() }

    GlassCardWithGlow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accent),
                onClick = {}
            )
            .pressableScale(interactionSource),
        glowColor = accent,
        glowAlpha = 0.4f,
        elevation = 16.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZodiacBadge(symbol = userSign.symbol, name = userSign.name, accent = accent)
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "★".repeat(rating) + "☆".repeat((5 - rating).coerceAtLeast(0)),
                    fontSize = 22.sp,
                    color = SaturnGold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = info?.description?.take(80) ?: "Different orbits",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(Modifier.width(12.dp))
            ZodiacBadge(symbol = partnerSign.symbol, name = partnerSign.name, accent = accent)
        }
    }
}

@Composable
private fun ZodiacBadge(symbol: String, name: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = symbol, fontSize = 26.sp, color = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
