package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@Composable
fun LifestyleZodiacCard(
    sign: ZodiacSign,
    viewModel: MainViewModel,
    navController: NavController
) {
    val allSigns = viewModel.zodiacSigns

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (Symbol, Name, Personality Text)
            Text(text = sign.symbol, style = MaterialTheme.typography.displayMedium)
            Text(
                text = sign.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = sign.personality,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            CompatibilityCard(
                sign = sign,
                allSigns = allSigns,
                onSignClick = { selectedSign ->
                    viewModel.onCompatibilitySignSelected(selectedSign)
                    navController.navigate(
                        Screen.Compatibility.createRoute(
                            userSignName = sign.name,
                            partnerSignName = selectedSign.name
                        )
                    )
                }
            )

            // --- ADD THE NEW BUTTON ---
            Spacer(modifier = Modifier.height(16.dp))
            Button (onClick = { navController.navigate(Screen.CompatibilityList.route) }) {
                Text("View Full Compatibility List")
            }
        }
    }
}