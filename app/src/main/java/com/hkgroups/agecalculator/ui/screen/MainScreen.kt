package com.hkgroups.agecalculator.ui.screen

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hkgroups.agecalculator.R
import com.hkgroups.agecalculator.ui.navigation.Screen
import com.hkgroups.agecalculator.ui.screen.components.HoroscopeCard
import com.hkgroups.agecalculator.ui.screen.components.LifestyleInfoRow
import com.hkgroups.agecalculator.ui.screen.components.LifestyleZodiacCard
import com.hkgroups.agecalculator.ui.viewmodel.MainViewModel
import java.util.*

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.selectedDate,
        transitionSpec = {
            fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
        },
        label = "Content Flow"
    ) { targetDate ->
        if (targetDate == null) {
            WelcomeScreen { timeInMillis ->
                viewModel.onDateSelected(timeInMillis)
            }
        } else {
            CosmicClockScreen(viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
fun WelcomeScreen(onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            showDatePicker(context, onDateSelected)
        }) {
            Text(stringResource(R.string.welcome_button_text))
        }
    }
}

@Composable
fun CosmicClockScreen(viewModel: MainViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    var liveAge by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(uiState.selectedDate) {
        uiState.selectedDate?.let {
            viewModel.ageTicker(it).collect { newAgeMap ->
                liveAge = newAgeMap
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (liveAge.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    liveAge.forEach { (unit, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))

            HoroscopeCard(horoscopeProvider = { uiState.dailyTip })
            Spacer(modifier = Modifier.height(16.dp))

            uiState.zodiacSign?.let { LifestyleZodiacCard(it, viewModel, navController) }

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // --- THIS IS THE CORRECTED CALL ---
                    LifestyleInfoRow(
                        title = "Next Birthday",
                        contentProvider = { uiState.birthdayCountdown }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    // --- AND THIS IS THE OTHER CORRECTED CALL ---
                    LifestyleInfoRow(
                        title = "Upcoming Milestone",
                        contentProvider = { uiState.milestone }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { navController.navigate(Screen.History.route) }) {
                Text("View Events in Your Lifetime")
            }

            Button(onClick = { navController.navigate(Screen.ZodiacExplorer.route) }) {
                Text("Explore The Zodiac")
            }

            OutlinedButton(onClick = { navController.navigate(Screen.BirthdayEvents.route) }) {
                Text("Events On Your Birthday")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        IconButton(
            onClick = { navController.navigate(Screen.Settings.route) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

private fun showDatePicker(context: Context, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            calendar.set(year, month, day)
            onDateSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = Date().time
        show()
    }
}