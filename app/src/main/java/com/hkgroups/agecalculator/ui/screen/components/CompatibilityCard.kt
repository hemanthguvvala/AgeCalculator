package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hkgroups.agecalculator.data.model.ZodiacSign

@Composable
fun CompatibilityCard(
    sign: ZodiacSign,
    allSigns: List<ZodiacSign>,
    onSignClick: (ZodiacSign) -> Unit
) {
    val compatibleSigns = sign.compatibilities.mapNotNull { compatibility ->
        allSigns.find { it.name == compatibility.signName }
    }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Most Compatible With",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            compatibleSigns.forEachIndexed { index, compatibleSign ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSignClick(compatibleSign) }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    // --- THE ONLY CHANGE IS HERE ---
                    // Replace the old static Column with the new AnimatedIcon
                    AnimatedIcon(sign = compatibleSign, index = index)
                }
            }
        }
    }
}