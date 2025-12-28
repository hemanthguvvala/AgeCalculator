package com.hkgroups.agecalculator.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hkgroups.agecalculator.R
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// Spacing Constants
object AppSpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Huge = 32.dp
}

// Color Palette - Matching HTML mockup exactly
private val CosmicBackground = Color(0xFF05070A) // #05070a - Deep Midnight Blue
private val CosmicSurface = Color(0x0DFFFFFF) // rgba(255,255,255,0.05) - Glass surface
private val CosmicBorder = Color(0x1AFFFFFF) // rgba(255,255,255,0.1) - Glass border
private val CosmicAccent = Color(0xFF4D96FF) // #4D96FF - Electric Blue
private val MarsAccent = Color(0xFFFF6B6B)
private val JupiterAccent = Color(0xFFE0C097)
private val NeptuneAccent = Color(0xFF4D96FF)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CosmicDashboardImproved(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Error state management
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var canRetry by remember { mutableStateOf(false) }
    
    // Network status
    var isOffline by remember { mutableStateOf(false) }
    var lastSyncTime by remember { mutableStateOf<Long?>(null) }
    
    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Custom haptic pattern for refresh
            scope.launch {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(50)
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            viewModel.refreshData()
            scope.launch {
                try {
                    delay(1500)
                    lastSyncTime = System.currentTimeMillis()
                    isOffline = false
                } catch (e: Exception) {
                    isOffline = true
                    showError = true
                    errorMessage = "Sync failed. Check your connection."
                    canRetry = true
                } finally {
                    isRefreshing = false
                }
            }
        }
    )
    
    // Calculate actual age from uiState with performance optimization
    val ageYears by remember(uiState.selectedDate) {
        derivedStateOf {
            uiState.selectedDate?.let { birthDate ->
                try {
                    java.time.Period.between(birthDate, java.time.LocalDate.now()).years
                } catch (e: Exception) {
                    showError = true
                    errorMessage = "Error calculating age"
                    0
                }
            } ?: 0
        }
    }
    
    // Track screen view (analytics)
    LaunchedEffect(Unit) {
        // Log screen view for analytics
        android.util.Log.d("Analytics", "CosmicDashboard: Screen viewed")
    }

    // Error snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error snackbar with retry
    LaunchedEffect(showError) {
        if (showError) {
            val result = snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = if (canRetry) "Retry" else "Dismiss",
                duration = if (canRetry) SnackbarDuration.Indefinite else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && canRetry) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.refreshData()
                canRetry = false
            }
            showError = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CosmicBackground,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = CosmicSurface,
                        contentColor = Color.White,
                        actionColor = CosmicAccent
                    )
                }
            },
            topBar = {
                CosmicTopBar(
                    onMenuClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        android.util.Log.d("Analytics", "CosmicDashboard: Menu clicked")
                        // Navigate to settings
                    },
                    onRefreshClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        android.util.Log.d("Analytics", "CosmicDashboard: Refresh clicked")
                        viewModel.refreshData()
                    },
                    onProfileClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        android.util.Log.d("Analytics", "CosmicDashboard: Profile clicked")
                        // Navigate to profile
                    }
                )
            },
            bottomBar = { }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                // Show empty state if no birth date selected
                if (uiState.selectedDate == null && !uiState.isLoading) {
                    EmptyStateView(
                        onSetBirthDate = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            android.util.Log.d("Analytics", "CosmicDashboard: Set birth date clicked")
                            // Navigate to date picker
                        }
                    )
                } else {
                    // Parallax scroll state
                    val scrollState = rememberLazyListState()
                    val firstVisibleItemScrollOffset by remember {
                        derivedStateOf<Int> { scrollState.firstVisibleItemScrollOffset }
                    }
                    
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
                        contentPadding = PaddingValues(bottom = 90.dp, top = 0.dp)
                    ) {
                        // Network status banner
                        if (isOffline) {
                            item {
                                OfflineIndicator(
                                    onRetry = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.refreshData()
                                    }
                                )
                            }
                        }
                        
                        // Last sync indicator
                        if (lastSyncTime != null && !isOffline) {
                            item {
                                LastSyncIndicator(timestamp = lastSyncTime!!)
                            }
                        }
                        
                        // Hero Section - Cosmic Age with real data
                        item {
                            CosmicAgeHeroSection(
                                ageYears = ageYears,
                                isLoading = uiState.isLoading,
                                parallaxOffset = firstVisibleItemScrollOffset
                            )
                        }

                        // Planetary Relativity Section
                        item {
                            PlanetarySectionHeader(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                android.util.Log.d("Analytics", "CosmicDashboard: View all planets clicked")
                            })
                        }

                        item {
                            PlanetaryCardsRow(
                                planetaryAges = uiState.planetaryAges,
                                onClick = { 
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    android.util.Log.d("Analytics", "CosmicDashboard: Planet card clicked")
                                }
                            )
                        }

                        // Cosmic Avatar Card
                        item {
                            CosmicAvatarCard(
                                chineseZodiac = uiState.chineseZodiac,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    android.util.Log.d("Analytics", "CosmicDashboard: Zodiac card clicked")
                                }
                            )
                        }

                        // Did You Know Card
                        item {
                            DidYouKnowCard(trivia = uiState.birthYearTrivia)
                        }

                        // Share Button
                        item {
                            ShareCosmicAgeButton(
                                onShareClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    android.util.Log.d("Analytics", "CosmicDashboard: Share clicked")
                                    shareCosmicAge(context, ageYears, uiState.chineseZodiac ?: "")
                                }
                            )
                        }
                    }
                }
                
                // Pull refresh indicator
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = CosmicSurface,
                    contentColor = CosmicAccent
                )
            }
        }

        // Floating Bottom Navigation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            CosmicBottomNavigation(
                selectedItem = selectedNavItem,
                onItemSelected = { index ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    android.util.Log.d("Analytics", "CosmicDashboard: Nav item $index selected")
                    selectedNavItem = index
                }
            )
        }
    }
}

@Composable
fun CosmicTopBar(
    onMenuClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Small)
            .semantics { heading() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Icon
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicSurface)
                .semantics { contentDescription = "Menu button" }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = stringResource(R.string.menu_button),
                tint = Color.White
            )
        }

        // LIVE DATA Chip
        Surface(
            shape = CircleShape,
            color = CosmicSurface,
            modifier = Modifier.clickable { onRefreshClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pulsing dot animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CosmicAccent.copy(alpha = alpha))
                )
                Text(
                    text = stringResource(R.string.live_data),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Profile Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0A080))
                .clickable { onProfileClick() }
                .semantics { contentDescription = "Profile button" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.profile_button),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CosmicAgeHeroSection(
    ageYears: Int, 
    isLoading: Boolean = false,
    parallaxOffset: Int = 0
) {
    // Apply parallax effect
    val parallaxAlpha = (1f - (parallaxOffset / 500f).coerceIn(0f, 0.7f))
    val parallaxScale = (1f - (parallaxOffset / 2000f).coerceIn(0f, 0.15f))
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Large)
            .graphicsLayer {
                alpha = parallaxAlpha
                scaleX = parallaxScale
                scaleY = parallaxScale
                translationY = -parallaxOffset * 0.3f
            }
            .semantics { heading() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.cosmic_age_title),
            style = MaterialTheme.typography.labelSmall,
            color = CosmicAccent.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SkeletonLoader(
                modifier = Modifier
                    .width(200.dp)
                    .height(72.dp)
            )
        }
        
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                // Animated counter
                val animatedAge by animateIntAsState(
                    targetValue = ageYears,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing),
                    label = "ageAnimation"
                )
                
                Text(
                    text = "$animatedAge",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 72.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = stringResource(R.string.years_short),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.earth_standard_time),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun PlanetarySectionHeader(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.planetary_relativity),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        TextButton(onClick = onClick) {
            Text(
                text = stringResource(R.string.view_all),
                style = MaterialTheme.typography.labelMedium,
                color = CosmicAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PlanetaryCardsRow(
    planetaryAges: List<Pair<String, String>> = emptyList(),
    onClick: () -> Unit = {}
) {
    // Memoize default planets for performance
    val defaultPlanets = remember {
        listOf(
            Triple("Mars", "13.2", MarsAccent),
            Triple("Jupiter", "2.1", JupiterAccent),
            Triple("Neptune", "0.15", NeptuneAccent)
        )
    }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
        contentPadding = PaddingValues(horizontal = AppSpacing.Large, vertical = AppSpacing.ExtraSmall)
    ) {
        items(defaultPlanets.size) { index ->
            val planet = defaultPlanets[index]
            // Try to find actual age from uiState, fallback to default
            val actualAge = planetaryAges.find { it.first.contains(planet.first, ignoreCase = true) }?.second
                ?: planet.second
            
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "cardScale"
            )
            
            PlanetCard(
                planetName = planet.first,
                planetAge = actualAge,
                accentColor = planet.third,
                planetIcon = Icons.Default.Star,
                modifier = Modifier.scale(scale),
                onClick = onClick
            )
        }
    }
}

@Composable
fun PlanetCard(
    planetName: String,
    planetAge: String,
    accentColor: Color,
    planetIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var showQuickActions by remember { mutableStateOf(false) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardSwipe"
    )
    
    Box(
        modifier = modifier
            .width(140.dp)
            .height(160.dp)
            .graphicsLayer {
                translationX = animatedOffsetX
                rotationZ = animatedOffsetX / 50f
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showQuickActions = !showQuickActions
                    }
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(
                width = if (showQuickActions) 2.dp else 1.dp,
                color = if (showQuickActions) accentColor else CosmicBorder,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Glow effect in top-right
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 30.dp, y = (-20).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Planet icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.dp, accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = planetIcon,
                    contentDescription = planetName,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Planet info
            Column {
                Text(
                    text = planetName.uppercase(),
                    fontSize = 10.sp,
                    color = accentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = planetAge,
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Text(
                    text = "Years",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CosmicAvatarCard(
    chineseZodiac: String?,
    onClick: () -> Unit = {}
) {
    val displayZodiac = chineseZodiac ?: "Dragon"
    val zodiacEmoji = when (displayZodiac.lowercase()) {
        "rat" -> "🐭"
        "ox" -> "🐂"
        "tiger" -> "🐯"
        "rabbit" -> "🐰"
        "dragon" -> "🐲"
        "snake" -> "🐍"
        "horse" -> "🐴"
        "goat" -> "🐐"
        "monkey" -> "🐵"
        "rooster" -> "🐔"
        "dog" -> "🐕"
        "pig" -> "🐖"
        else -> "🐲"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large)
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CosmicAccent.copy(alpha = 0.1f),
                            Color(0xFF9B59B6).copy(alpha = 0.1f),
                            Color(0xFFEC4899).copy(alpha = 0.1f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side content
            Column(
                modifier = Modifier.weight(0.55f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = stringResource(R.string.cosmic_avatar),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Year of the\n$displayZodiac",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                Text(
                    text = "Power, luck, and\nstrength aligned with\nstars.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* View Profile */ }
                ) {
                    Text(
                        text = "View Profile",
                        fontSize = 14.sp,
                        color = CosmicAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = CosmicAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Right side - Zodiac emoji
            Text(
                text = zodiacEmoji,
                fontSize = 100.sp,
                modifier = Modifier
                    .offset(x = 8.dp)
            )
        }
    }
}

@Composable
fun DidYouKnowCard(trivia: String? = null) {
    val displayTrivia = trivia ?: stringResource(R.string.venus_fact)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large)
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .clickable { /* Card action */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CosmicAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CosmicAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.did_you_know),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                Text(
                    text = displayTrivia,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ShareCosmicAgeButton(onShareClick: () -> Unit) {
    Button(
        onClick = onShareClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CosmicAccent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.Small))
        Text(
            text = stringResource(R.string.share_cosmic_age),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// Share functionality
private fun shareCosmicAge(context: Context, age: Int, zodiac: String) {
    val shareText = buildString {
        appendLine("🌟 My Cosmic Age 🌟")
        appendLine()
        appendLine("I'm $age years old on Earth!")
        if (zodiac.isNotEmpty()) {
            appendLine("Chinese Zodiac: Year of the $zodiac")
        }
        appendLine()
        appendLine("Discover your cosmic age too!")
        appendLine("#CosmicAge #ZodiacCalculator")
    }
    
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    
    val chooser = Intent.createChooser(intent, context.getString(R.string.share_via))
    context.startActivity(chooser)
}

@Composable
fun CosmicBottomNavigation(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val navItems = listOf(
        Triple(Icons.Default.Home, stringResource(R.string.nav_dashboard), "Dashboard"),
        Triple(Icons.Default.Star, stringResource(R.string.nav_rocket), "Rocket"),
        Triple(Icons.Default.Search, stringResource(R.string.nav_explore), "Explore"),
        Triple(Icons.Default.Settings, stringResource(R.string.nav_settings), "Settings")
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.Large, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                navItems.forEachIndexed { index, (icon, label, contentDesc) ->
                    val isSelected = selectedItem == index
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "navScale$index"
                    )
                    
                    IconButton(
                        onClick = { onItemSelected(index) },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) CosmicAccent
                                else Color.Transparent
                            )
                            .semantics { contentDescription = contentDesc }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// Empty State when no birth date selected
@Composable
fun EmptyStateView(onSetBirthDate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Large)
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "emptyState")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(CosmicSurface)
                    .border(2.dp, CosmicBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = CosmicAccent.copy(alpha = alpha),
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Text(
                text = "Set Your Birth Date",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Discover your cosmic age across\nthe universe and unlock personalized insights",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.Large))
            
            Button(
                onClick = onSetBirthDate,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicAccent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.Small))
                Text(
                    text = "Choose Birth Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Skeleton Loader for loading states
@Composable
fun SkeletonLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicSurface.copy(alpha = alpha))
    )
}

// Offline Indicator Banner
@Composable
fun OfflineIndicator(onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
        border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Offline Mode",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Showing cached data",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            TextButton(onClick = onRetry) {
                Text(
                    text = "Retry",
                    color = CosmicAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Last Sync Time Indicator
@Composable
fun LastSyncIndicator(timestamp: Long) {
    val timeAgo = remember(timestamp) {
        val diff = System.currentTimeMillis() - timestamp
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.ExtraSmall),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4ADE80),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Synced $timeAgo",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun CosmicDashboardPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = CosmicBackground,
            surface = CosmicSurface,
            primary = CosmicAccent
        )
    ) {
        CosmicDashboardImproved()
    }
}
