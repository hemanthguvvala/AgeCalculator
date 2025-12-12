package com.hkgroups.agecalculator.ui.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.data.model.ZodiacSign
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityListScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userSign = uiState.zodiacSign
    val allSigns = viewModel.zodiacSigns

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${userSign?.name ?: ""} Compatibility") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (userSign != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allSigns) { partnerSign ->
                    if (partnerSign.name != userSign.name) {
                        CompatibilityRow(userSign = userSign, partnerSign = partnerSign)
                    }
                }
            }
        }
    }
}

@Composable
fun CompatibilityRow(userSign: ZodiacSign, partnerSign: ZodiacSign) {
    val compatibilityInfo = userSign.compatibilities.find { it.signName == partnerSign.name }
    // Default to a low rating if no specific compatibility is defined
    val rating = compatibilityInfo?.rating ?: 1

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = userSign.symbol, style = MaterialTheme.typography.headlineMedium)
                Text(text = userSign.name, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "★".repeat(rating) + "☆".repeat(5 - rating),
                    fontSize = 24.sp,
                    color = Color(0xFFFFC107),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = partnerSign.symbol, style = MaterialTheme.typography.headlineMedium)
                Text(text = partnerSign.name, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}