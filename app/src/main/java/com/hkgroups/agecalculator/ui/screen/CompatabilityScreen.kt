package com.hkgroups.agecalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityDetailScreen(
    navController: NavController,
    userSignName: String?,
    partnerSignName: String?,
    viewModel: MainViewModel = hiltViewModel()
) {
    // Safely load the data using the names passed as arguments
    val userSign = userSignName?.let { viewModel.getSignByName(it) }
    val partnerSign = partnerSignName?.let { viewModel.getSignByName(it) }

    // Find the specific compatibility info
    val compatibilityInfo = userSign?.compatibilities?.find { it.signName == partnerSign?.name }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${userSign?.name ?: "..."} & ${partnerSign?.name ?: "..."}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Check if data is available before trying to display it
        if (userSign != null && partnerSign != null && compatibilityInfo != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = userSign.symbol, style = MaterialTheme.typography.displayLarge)
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Love",
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .size(48.dp),
                        tint = Color(0xFFE91E63)
                    )
                    Text(text = partnerSign.symbol, style = MaterialTheme.typography.displayLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Rating: ${"★".repeat(compatibilityInfo.rating)}${"☆".repeat(5 - compatibilityInfo.rating)}",
                    fontSize = 24.sp,
                    color = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = compatibilityInfo.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Show a loading or error state if the data isn't ready
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}