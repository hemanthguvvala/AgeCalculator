package com.hkgroups.agecalculator.ui.screen

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.R
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.components.*
import com.hkgroups.agecalculator.ui.theme.*
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel
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
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        StarryBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmicGradient)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ " + stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Discover your cosmic journey",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { showDatePicker(context, onDateSelected) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNeon
                    )
                ) {
                    Text(
                        text = stringResource(R.string.welcome_button_text),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CosmicDashboardScreen(viewModel: MainViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    var liveAge by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedNavIndex by remember { mutableStateOf(0) }

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
    
    Box(modifier = Modifier.fillMaxSize()) {
        StarryBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp) // Space for floating nav bar
            ) {
                // Header with title and avatar
                CosmicHeader(navController = navController, viewModel = viewModel)
                
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
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryNeon
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
                
                // Age Ticker - System Stats
                AgeTickerSection(liveAge = liveAge, planetCount = uiState.planetaryAges.size)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Planetary System Section
                if (uiState.planetaryAges.isNotEmpty()) {
                    PlanetarySystemSection(
                        planetaryAges = uiState.planetaryAges.associate { it.first to it.second },
                        navController = navController
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
                DidYouKnowSection(birthYearTrivia = uiState.birthYearTrivia)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Floating Navigation Bar
        FloatingNavBar(
            items = listOf(
                NavItem(Icons.Default.Home, "Home"),
                NavItem(Icons.Default.Search, "Explore"),
                NavItem(Icons.Default.Star, "Zodiac"),
                NavItem(Icons.Default.Person, "Profile")
            ),
            selectedIndex = selectedNavIndex,
            onItemSelected = { index ->
                selectedNavIndex = index
                when (index) {
                    0 -> { /* Already on home */ }
                    1 -> navController.navigate(Screen.History.route)
                    2 -> navController.navigate(Screen.ZodiacExplorer.route)
                    3 -> navController.navigate(Screen.Settings.route)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
        
        // Snackbar for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun CosmicHeader(navController: NavController, viewModel: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Grid Menu Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGlass)
                .clickable { navController.navigate(Screen.Settings.route) },
            contentAlignment = Alignment.Center
        ) {
            // Grid icon using Text
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(8.dp).background(Color.White, RoundedCornerShape(2.dp)))
                    Box(Modifier.size(8.dp).background(Color.White, RoundedCornerShape(2.dp)))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(8.dp).background(Color.White, RoundedCornerShape(2.dp)))
                    Box(Modifier.size(8.dp).background(Color.White, RoundedCornerShape(2.dp)))
                }
            }
        }
        
        // LIVE DATA Pill Button
        GlassCard(
            modifier = Modifier
                .clickable { /* Refresh data */ viewModel.refreshData() },
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryNeon)
                )
                Text(
                    text = "LIVE DATA",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
        
        // User Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0A080))
                .clickable { navController.navigate(Screen.Settings.route) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun AgeTickerSection(liveAge: List<Pair<String, String>>, planetCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "YOUR COSMIC AGE" Title
        Text(
            text = "•YOUR COSMIC AGE•",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryNeon,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Large centered age display
        if (liveAge.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = liveAge[0].second.replace(" years", ""),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "yr",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // "Earth Standard Time" subtitle
        Text(
            text = "Earth Standard Time",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun PlanetarySystemSection(
    planetaryAges: Map<String, String>,
    navController: NavController
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Planetary Relativity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "VIEW ALL",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryNeon,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.clickable { navController.navigate(Screen.History.route) }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mars Card
            planetaryAges["Mars"]?.let { age ->
                PlanetCard(
                    planetName = "MARS",
                    planetAge = age,
                    planetColor = MarsRed,
                    planetImage = {
                        Text(
                            text = "🔴",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                )
            }
            
            // Jupiter Card
            planetaryAges["Jupiter"]?.let { age ->
                PlanetCard(
                    planetName = "JUPITER",
                    planetAge = age,
                    planetColor = JupiterBeige,
                    planetImage = {
                        Text(
                            text = "🪐",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                )
            }
            
            // Earth Card
            planetaryAges["Earth"]?.let { age ->
                PlanetCard(
                    planetName = "EARTH",
                    planetAge = age,
                    planetColor = EarthBlue,
                    planetImage = {
                        Text(
                            text = "🌍",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                )
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
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Large Chinese Zodiac Avatar Card (like the reference)
        chineseZodiac?.let { czName ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left side: Text content
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // "COSMIC AVATAR" pill
                            GlassCard(
                                modifier = Modifier.width(160.dp),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "COSMIC AVATAR",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            
                            // "Year of the Dragon" title
                            Text(
                                text = "Year of the\n$czName",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 36.sp
                            )
                            
                            // Description
                            zodiacSign?.let {
                                Text(
                                    text = "Power, luck, and\nstrength aligned with\nstars.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            
                            // "View Profile" link
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { navController.navigate(Screen.Settings.route) }
                            ) {
                                Text(
                                    text = "View Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrimaryNeon,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "→",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrimaryNeon
                                )
                            }
                        }
                        
                        // Right side: Large zodiac emoji/avatar
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4CAF50).copy(alpha = 0.6f),
                                            Color(0xFF2E7D32).copy(alpha = 0.3f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.hkgroups.agecalculator.util.CosmicUtils.getChineseZodiacEmoji(czName),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp)
                            )
                        }
                    }
                }
            }
        }
    }
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
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Time Capsule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            selectedDate?.let {
                                Text(
                                    text = "Year ${it.year}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = trivia,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Birthday & Milestone Info
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Birthday Countdown
                daysUntilBirthday?.let { days ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Next Birthday",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = if (days == 0) "Today! 🎉" else "in $days days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "🎂",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Milestone
                milestoneData?.let { milestone ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upcoming Milestone",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "${"%,d".format(milestone.dayCount)} days on ${milestone.date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "🎯",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { navController.navigate(Screen.History.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "View Events in Your Lifetime",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
        
        Button(
            onClick = { navController.navigate(Screen.ZodiacExplorer.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryNeon
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Explore The Zodiac",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Button(
            onClick = { navController.navigate(Screen.BirthdayEvents.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Events On Your Birthday",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DidYouKnowSection(birthYearTrivia: String?) {
    if (birthYearTrivia != null) {
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
                            text = birthYearTrivia,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

private fun showDatePicker(context: Context, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(year, month, day)
            onDateSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = Date().time
        show()
    }
}