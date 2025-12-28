package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZodiacDetailScreen(
    navController: NavController,
    signName: String?,
    viewModel: MainViewModel = hiltViewModel()
) {
    val sign = signName?.let { viewModel.getSignByName(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = sign?.name ?: "Zodiac Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (sign != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()), // Make the column scrollable
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = sign.symbol, style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sign.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = sign.dateRange, style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = sign.personality,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                // --- NEW UI FOR DETAILED TRAITS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(title = "Ruling Planet", value = sign.rulingPlanet)
                        DetailRow(title = "Element", value = sign.element)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TraitSection(title = "Strengths", traits = sign.strengths)

                Spacer(modifier = Modifier.height(16.dp))

                TraitSection(title = "Weaknesses", traits = sign.weaknesses)
            }
        }
    }
}

@Composable
fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TraitSection(title: String, traits: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        traits.forEach { trait ->
            Text(
                text = "• $trait",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}