package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SurfaceDark
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

/**
 * CosmicDatePicker — three-wheel date selector (Month · Day · Year) styled
 * to match the cosmic theme.
 *
 * Each wheel is a vertical scroller showing 5 visible items at a time with
 * the center selection highlighted by a sign-tinted pill. Far more on-brand
 * than the Material3 calendar grid.
 *
 * @param initialDate starting date for the wheels
 * @param minYear / maxYear inclusive year range
 * @param onConfirm called with the picked LocalDate when the user taps OK
 * @param onDismiss called when the user taps Cancel or outside the dialog
 */
@Composable
fun CosmicDatePicker(
    initialDate: LocalDate,
    minYear: Int,
    maxYear: Int,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalSignPalette.current
    var year by remember { mutableIntStateOf(initialDate.year) }
    var month by remember { mutableIntStateOf(initialDate.monthValue) }
    var day by remember { mutableIntStateOf(initialDate.dayOfMonth) }

    // Clamp day if month/year change reduces month length.
    LaunchedEffect(year, month) {
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        if (day > maxDay) day = maxDay
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceDark)
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PICK YOUR BIRTH DATE",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.primary,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Scroll each wheel to set the date",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    // Selection band — center pill the wheels scroll behind.
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.primary.copy(alpha = 0.16f))
                            .border(
                                1.dp,
                                palette.primary.copy(alpha = 0.45f),
                                RoundedCornerShape(14.dp)
                            )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Month
                        Wheel(
                            modifier = Modifier.weight(1.4f),
                            items = (1..12).map { Months[it - 1] },
                            initialIndex = month - 1,
                            onIndexChanged = { month = it + 1 }
                        )
                        // Day — recomputed length when month/year change
                        val maxDay = remember(year, month) {
                            YearMonth.of(year, month).lengthOfMonth()
                        }
                        Wheel(
                            modifier = Modifier.weight(1f),
                            items = (1..maxDay).map { it.toString().padStart(2, '0') },
                            initialIndex = (day - 1).coerceAtMost(maxDay - 1),
                            onIndexChanged = { day = it + 1 }
                        )
                        // Year
                        Wheel(
                            modifier = Modifier.weight(1.2f),
                            items = (minYear..maxYear).map { it.toString() },
                            initialIndex = year - minYear,
                            onIndexChanged = { year = minYear + it }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        onClick = {
                            val picked = LocalDate.of(year, month, day)
                            onConfirm(picked)
                        }
                    ) {
                        Text(
                            text = "OK",
                            color = palette.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Wheel(
    items: List<String>,
    initialIndex: Int,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5
) {
    val itemHeight = 44.dp
    val itemHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { itemHeight.toPx() }
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex.coerceAtLeast(0)
    )
    val scope = rememberCoroutineScope()

    // Selected index = the item whose center is closest to the viewport center.
    val selectedIndex by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            info.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2f) - viewportCenter)
            }?.index ?: state.firstVisibleItemIndex
        }
    }
    LaunchedEffect(selectedIndex) {
        onIndexChanged(selectedIndex.coerceIn(0, items.lastIndex))
    }

    // Snap to selected when the user lifts their finger.
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            scope.launch {
                state.animateScrollToItem(selectedIndex.coerceIn(0, items.lastIndex))
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleCount)
            .fillMaxWidth()
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                vertical = itemHeight * (visibleCount / 2)
            )
        ) {
            items(items) { label ->
                val i = items.indexOf(label)
                val distance = abs(i - selectedIndex).coerceAtMost(2)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.55f
                    else -> 0.25f
                }
                val style = if (distance == 0)
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.titleSmall
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = style,
                        color = Color.White.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        // Top + bottom fade so off-pill items dissolve into the surface.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * (visibleCount / 2))
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceDark, Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * (visibleCount / 2))
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, SurfaceDark)
                    )
                )
        )
    }
}

private val Months = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)
