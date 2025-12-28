// Font.kt - Updated with fallback configuration
// Use this if you encounter font loading issues

package com.hkgroups.agecalculator.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hkgroups.agecalculator.R

// ===== OPTION 1: Single Font Files =====
// If you downloaded single .ttf files from Google Fonts
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val SplineSans = FontFamily(
    Font(R.font.spline_sans, FontWeight.Normal),
    Font(R.font.spline_sans, FontWeight.Medium),
    Font(R.font.spline_sans, FontWeight.SemiBold)
)

// ===== OPTION 2: Multiple Font Files by Weight =====
// If you downloaded separate files for each weight
/*
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.space_grotesk_bold, FontWeight.Bold)
)

val SplineSans = FontFamily(
    Font(R.font.spline_sans_regular, FontWeight.Normal),
    Font(R.font.spline_sans_medium, FontWeight.Medium),
    Font(R.font.spline_sans_semibold, FontWeight.SemiBold)
)
*/

// ===== OPTION 3: System Font Fallback =====
// Temporary solution if you don't have custom fonts yet
/*
import androidx.compose.ui.text.font.FontFamily

val SpaceGrotesk = FontFamily.SansSerif  // Fallback to system sans-serif
val SplineSans = FontFamily.SansSerif    // Fallback to system sans-serif
*/

// ===== OPTION 4: Use Existing Fonts Temporarily =====
// If you want to keep using your existing fonts
/*
val SpaceGrotesk = PlayfairDisplay  // Use existing font temporarily
val SplineSans = WorkSans            // Use existing font temporarily
*/

// Legacy fonts for compatibility
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

val WorkSans = FontFamily(
    Font(R.font.work_sans_regular, FontWeight.Normal),
    Font(R.font.work_sans_medium, FontWeight.Medium),
    Font(R.font.work_sans_semibold, FontWeight.SemiBold)
)

// ===== Font Download Instructions =====
/*
STEP 1: Download Fonts from Google Fonts

Space Grotesk:
https://fonts.google.com/specimen/Space+Grotesk
- Click "Get font" or "Download family"
- Extract the .ttf files

Spline Sans:
https://fonts.google.com/specimen/Spline+Sans
- Click "Get font" or "Download family"
- Extract the .ttf files

STEP 2: Add to Your Project

Create folder if it doesn't exist:
app/src/main/res/font/

Add files:
- space_grotesk.ttf (or space_grotesk_regular.ttf)
- spline_sans.ttf (or spline_sans_regular.ttf)

STEP 3: Verify File Names

Make sure the file names match your Font() declarations.
Android requires lowercase with underscores, no hyphens:
✓ space_grotesk.ttf
✓ space_grotesk_bold.ttf
✗ SpaceGrotesk.ttf
✗ space-grotesk.ttf

STEP 4: Build Project

./gradlew clean build

If you see "resource not found" errors, double-check:
1. File is in res/font/ folder
2. File name matches R.font.xxx reference
3. File name is lowercase with underscores
4. No spaces in filename

STEP 5: Test

Run the app and verify:
- Headers use Space Grotesk (slightly wider, geometric)
- Body text uses Spline Sans (more humanist, rounded)
*/

// ===== Alternative Free Fonts =====
/*
If you can't find Space Grotesk or Spline Sans, try these alternatives:

Instead of Space Grotesk:
- Inter (https://fonts.google.com/specimen/Inter)
- DM Sans (https://fonts.google.com/specimen/DM+Sans)
- Outfit (https://fonts.google.com/specimen/Outfit)

Instead of Spline Sans:
- Inter (https://fonts.google.com/specimen/Inter)
- Manrope (https://fonts.google.com/specimen/Manrope)
- Plus Jakarta Sans (https://fonts.google.com/specimen/Plus+Jakarta+Sans)
*/
