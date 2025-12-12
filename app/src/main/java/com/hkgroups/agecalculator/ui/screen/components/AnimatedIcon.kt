package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.hkgroups.agecalculator.data.model.ZodiacSign
import kotlinx.coroutines.delay

@Composable
fun AnimatedIcon(sign: ZodiacSign, index: Int) {
    // Animation states for scale and alpha
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    // This triggers the animation when the composable first appears
    LaunchedEffect(key1 = sign) {
        // Stagger the animation based on the icon's index
        delay(index * 150L) // Each icon will start 150ms after the previous one

        // Animate scale from 0% to 100%
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        // Animate alpha (fade-in) at the same time
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.value) // Apply the animated scale
            .alpha(alpha.value) // Apply the animated alpha
    ) {
        Text(
            text = sign.symbol,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = sign.name,
            style = MaterialTheme.typography.bodySmall
        )
    }
}