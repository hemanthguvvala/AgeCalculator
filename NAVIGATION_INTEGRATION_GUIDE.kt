/* 
 * Navigation Integration Guide for Cosmic Profile Screen
 * Add this to your Screen sealed class and navigation graph
 */

// ===== Step 1: Update Screen.kt =====
// Add to your Screen sealed class:
object CosmicProfile : Screen("cosmic_profile")

// ===== Step 2: Update NavigationGraph.kt =====
// Add this composable to your NavHost:

composable(Screen.CosmicProfile.route) {
    CosmicProfileScreen(
        navController = navController,
        viewModel = hiltViewModel()
    )
}

// ===== Step 3: Navigate to Profile =====
// From MainScreen or anywhere else:
navController.navigate(Screen.CosmicProfile.route)

// Or update the FloatingNavBar in MainScreen to use the new profile:
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
            3 -> navController.navigate(Screen.CosmicProfile.route) // ← Updated
        }
    },
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
)

// ===== Optional: Replace Settings Icon =====
// If you want to replace the settings icon in CosmicHeader with profile navigation:

Icon(
    imageVector = Icons.Default.Person,
    contentDescription = "Profile",
    tint = PrimaryNeon,
    modifier = Modifier
        .size(28.dp)
        .clickable { navController.navigate(Screen.CosmicProfile.route) }
)
