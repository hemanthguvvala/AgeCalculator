package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.screen.components.*
import com.hkgroups.agecalculator.ui.theme.*
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

/**
 * Cosmic Profile Screen - Combines Settings and Zodiac detail into a unified profile view
 * Matches the "Deep Space Glassmorphism" aesthetic with progress bars and stats grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmicProfileScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        StarryBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Header with Avatar and Name
                ProfileHeader(viewModel = viewModel, uiState = uiState)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Age Progress Section
                CosmicAgeProgress(uiState = uiState)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Mission Progress
                MissionProgressCard()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Cosmic Stats Grid
                CosmicStatsSection(uiState = uiState)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Personal Data Section
                PersonalDataSection(uiState = uiState)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Settings Section
                SettingsSection()
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    viewModel: MainViewModel,
    uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Avatar with Glow
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .blur(32.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryNeon.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(3.dp, PrimaryNeon, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1a2332),
                                Color(0xFF0f1419)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder cosmic avatar - could be replaced with actual image
                Text(
                    text = "🌌",
                    style = MaterialTheme.typography.displayLarge
                )
                
                // Online indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .background(GreenAccent, CircleShape)
                        .border(3.dp, BackgroundDark, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User Name
        Text(
            text = "Alex Andromeda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // User Title/Level
        GlassCard(
            modifier = Modifier,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "🚀 Cosmic Explorer • Level 4",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryNeon,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Earth Age Display
        uiState.selectedDate?.let { birthDate ->
            val period = java.time.Period.between(birthDate, java.time.LocalDate.now())
            GlassCard(
                modifier = Modifier,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${period.years} EARTH YEARS / ${period.years.toDouble() / 11.86} SATURN YEARS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmicAgeProgress(uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "COSMIC AGE",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        uiState.selectedDate?.let { birthDate ->
            val period = java.time.Period.between(birthDate, java.time.LocalDate.now())
            Text(
                text = "${period.years} yr",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Earth Standard Time",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MissionProgressCard() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mission to Mars",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Next Major Milestone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Text(
                        text = "🔴",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "65%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Cosmic Progress Bar
                CosmicProgressBar(
                    progress = 0.65f,
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryNeon, PurpleAccent)
                    )
                )
            }
        }
    }
}

@Composable
private fun CosmicStatsSection(uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "COSMIC STATS",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StatsGrid(
            items = listOf(
                StatsItem(
                    icon = uiState.zodiacSign?.symbol ?: "♈",
                    label = "Zodiac Sign",
                    value = uiState.zodiacSign?.name ?: "Unknown"
                ),
                StatsItem(
                    icon = "🌙",
                    label = "Moon Phase",
                    value = "Waning"
                )
            )
        )
    }
}

@Composable
private fun PersonalDataSection(uiState: com.hkgroups.agecalculator.ui.viewmodel.UiState) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "PERSONAL DATA",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                PersonalDataItem(
                    icon = "📧",
                    label = "Email",
                    value = "alex@cosmos.io"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PersonalDataItem(
                    icon = "🎂",
                    label = "Date of Birth",
                    value = uiState.selectedDate?.format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                    ) ?: "Not set"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PersonalDataItem(
                    icon = "📍",
                    label = "Home Base",
                    value = "New York, USA"
                )
            }
        }
    }
}

@Composable
private fun PersonalDataItem(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SettingsSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SettingsItem(
                    icon = "🔔",
                    label = "Notifications",
                    hasToggle = true,
                    isToggled = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItem(
                    icon = "📏",
                    label = "Units",
                    value = "Lightyears"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItem(
                    icon = "🌙",
                    label = "Theme",
                    value = "Deep Space 🔒"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Log Out Button
        Button(
            onClick = { /* Handle logout */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color(0xFFFF6B6B)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFF6B6B)
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: String,
    label: String,
    value: String? = null,
    hasToggle: Boolean = false,
    isToggled: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
        
        if (hasToggle) {
            Switch(
                checked = isToggled,
                onCheckedChange = { /* Handle toggle */ },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryNeon,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        } else if (value != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
