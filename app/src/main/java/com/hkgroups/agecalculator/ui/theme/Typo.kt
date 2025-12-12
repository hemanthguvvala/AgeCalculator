package com.hkgroups.agecalculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // For large, important titles
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    // For screen titles
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    ),
    // For card titles
    titleMedium = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    // For body text
    bodyLarge = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)