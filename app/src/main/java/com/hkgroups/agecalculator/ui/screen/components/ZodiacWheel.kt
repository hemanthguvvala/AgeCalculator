package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.data.model.ZodiacSign
import kotlin.math.absoluteValue

/**
 * ZodiacWheel — 3D coverflow-style horizontal pager.
 *
 * Replaces the 2-column grid in the Explorer with a single-card-at-a-time
 * carousel where adjacent cards rotate, scale, and fade based on their
 * distance from the center. The user swipes through 12 signs, the focused
 * card sits front-and-center at full scale, and side cards angle away in 3D.
 *
 * Far more cinematic than a flat grid — and the focal card becomes the hero
 * of the screen.
 */
@Composable
fun ZodiacWheel(
    signs: List<ZodiacSign>,
    onSignClick: (ZodiacSign) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { signs.size }
    )
    val focused = signs.getOrNull(pagerState.currentPage)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 90.dp),
            pageSpacing = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) { page ->
            val sign = signs[page]
            // Distance from center, signed: -1 (full left) .. 0 (center) .. +1 (full right)
            val pageOffset = (
                (pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction
                ).coerceIn(-2f, 2f)
            val abs = pageOffset.absoluteValue.coerceAtMost(1.6f)

            ZodiacWheelCard(
                sign = sign,
                pageOffset = pageOffset,
                onClick = { onSignClick(sign) },
                modifier = Modifier
                    .graphicsLayer {
                        // Off-axis rotation gives the 3D coverflow tilt.
                        rotationY = pageOffset * 38f
                        // Scale collapses as the card moves off-center.
                        val sc = 1f - 0.20f * abs
                        scaleX = sc
                        scaleY = sc
                        // Fade adjacent cards.
                        alpha = 1f - 0.55f * abs.coerceAtMost(1f)
                        // Pivot from the front-facing edge so 3D swing feels right.
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (pageOffset > 0f) 1f else 0f,
                            pivotFractionY = 0.5f
                        )
                        cameraDistance = 12f * density
                    }
            )
        }

        // Focused metadata under the wheel — name + element badge so the
        // user knows what they're looking at without tapping.
        focused?.let { sign ->
            val accent = elementAccent(sign.element)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sign.name,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f))
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${sign.element.uppercase()} · ${sign.dateRange}",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                // Page indicator dots
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    signs.forEachIndexed { i, _ ->
                        val isActive = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) accent else Color.White.copy(alpha = 0.18f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZodiacWheelCard(
    sign: ZodiacSign,
    pageOffset: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = elementAccent(sign.element)
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
            .clickable(enabled = pageOffset.absoluteValue < 0.3f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 320.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.22f),
                            Color(0xFF161A2E),
                            Color(0xFF0B0E1B)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.40f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ZodiacGlyph(
                        sign = sign.name,
                        strokeColor = Color.White,
                        accentColor = accent,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = sign.symbol,
                        fontSize = 22.sp,
                        color = accent
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = sign.element.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun elementAccent(element: String): Color = when (element.lowercase()) {
    "fire" -> Color(0xFFFF6B6B)
    "earth" -> Color(0xFFE0C097)
    "air" -> Color(0xFF4ECDC4)
    "water" -> Color(0xFF4D96FF)
    else -> Color(0xFF9B59B6)
}
