package com.hkgroups.agecalculator.ui.navigation

import androidx.navigation.NavBackStackEntry

/**
 * Type-safe navigation argument helper
 * Provides compile-time safe access to navigation arguments
 */
sealed class NavigationArgs {
    
    /**
     * Arguments for Compatibility screen
     */
    data class CompatibilityArgs(
        val userSignName: String,
        val partnerSignName: String
    ) {
        companion object {
            const val USER_SIGN_KEY = "userSignName"
            const val PARTNER_SIGN_KEY = "partnerSignName"
            
            /**
             * Extract arguments from NavBackStackEntry
             */
            fun from(backStackEntry: NavBackStackEntry): CompatibilityArgs? {
                val userSign = backStackEntry.arguments?.getString(USER_SIGN_KEY) ?: return null
                val partnerSign = backStackEntry.arguments?.getString(PARTNER_SIGN_KEY) ?: return null
                return CompatibilityArgs(userSign, partnerSign)
            }
        }
    }
    
    /**
     * Arguments for ZodiacDetail screen
     */
    data class ZodiacDetailArgs(
        val signName: String
    ) {
        companion object {
            const val SIGN_NAME_KEY = "signName"
            
            /**
             * Extract arguments from NavBackStackEntry
             */
            fun from(backStackEntry: NavBackStackEntry): ZodiacDetailArgs? {
                val signName = backStackEntry.arguments?.getString(SIGN_NAME_KEY) ?: return null
                return ZodiacDetailArgs(signName)
            }
        }
    }
}

/**
 * Extension functions for type-safe navigation
 */

/**
 * Navigate to Compatibility screen with type-safe arguments
 */
fun androidx.navigation.NavController.navigateToCompatibility(
    userSignName: String,
    partnerSignName: String
) {
    navigate("compatibility/$userSignName/$partnerSignName")
}

/**
 * Navigate to ZodiacDetail screen with type-safe arguments
 */
fun androidx.navigation.NavController.navigateToZodiacDetail(signName: String) {
    navigate("zodiac_detail/$signName")
}
