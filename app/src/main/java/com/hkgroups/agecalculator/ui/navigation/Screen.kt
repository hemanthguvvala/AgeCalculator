package com.hkgroups.agecalculator.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")

    object Compatibility : Screen("compatibility_screen/{userSignName}/{partnerSignName}") {
        fun createRoute(userSignName: String, partnerSignName: String) =
            "compatibility_screen/$userSignName/$partnerSignName"
    }

    object History : Screen("history_screen")

    // --- NEW ROUTES FOR THE EXPLORER FEATURE ---
    object ZodiacExplorer : Screen("zodiac_explorer_screen")
    object ZodiacDetail : Screen("zodiac_detail_screen/{signName}") {
        fun createRoute(signName: String) = "zodiac_detail_screen/$signName"
    }

    object CompatibilityList : Screen("compatibility_list_screen")

    object BirthdayEvents : Screen("birthday_events_screen")

    object Settings : Screen("settings_screen")
    
    object PrivacyPolicy : Screen("privacy_policy_screen")
}