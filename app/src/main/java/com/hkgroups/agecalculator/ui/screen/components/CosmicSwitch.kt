package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette

/**
 * CosmicSwitch — custom toggle that matches the cosmic theme.
 *
 * Replaces Material3's stock Switch with:
 *  - Pill track tinted with the user's sign palette when on
 *  - Glass-like dark track when off, with a subtle inner border
 *  - Thumb that springs across with a colored shadow when activated
 *
 * Same API as Material's `Switch` — caller provides `checked` + `onCheckedChange`.
 */
@Composable
fun CosmicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalSignPalette.current
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 22.dp
    val thumbInset = 4.dp
    val travel = trackWidth - thumbSize - (thumbInset * 2)

    val trackColor by animateColorAsState(
        targetValue = if (checked)
            palette.primary.copy(alpha = 0.55f)
        else
            Color.White.copy(alpha = 0.06f),
        animationSpec = tween(220),
        label = "trackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked)
            palette.primary.copy(alpha = 0.7f)
        else
            Color.White.copy(alpha = 0.16f),
        animationSpec = tween(220),
        label = "trackBorder"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) travel else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbOffset"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color(0xFFD8DBE4),
        animationSpec = tween(220),
        label = "thumbColor"
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(thumbInset)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = palette.primary,
                    spotColor = palette.primary
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, thumbColor)
                    )
                )
        )
    }
}
