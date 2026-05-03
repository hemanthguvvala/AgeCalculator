package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkgroups.agecalculator.ui.theme.LocalSignPalette
import com.hkgroups.agecalculator.ui.theme.SignPalette
import com.hkgroups.agecalculator.ui.theme.rememberSignPalette
import com.hkgroups.agecalculator.util.LocalCosmicFeedback
import com.hkgroups.agecalculator.util.CosmicFeedback

/** Canonical sign order for pickers. */
val ZODIAC_SIGN_NAMES = listOf(
    "Aries", "Taurus", "Gemini", "Cancer",
    "Leo", "Virgo", "Libra", "Scorpio",
    "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

/**
 * SignPickerSheet — modal bottom sheet with a 12-sign grid. Each cell uses
 * the sign's own palette so the picker doubles as a palette preview.
 *
 * Saves only when the user taps a chip (with optional Clear button if
 * [allowClear] is set, useful for unsetting rising/moon).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SignPickerSheet(
    title: String,
    subtitle: String,
    initialSelection: String?,
    allowClear: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = LocalSignPalette.current
    val feedback = LocalCosmicFeedback.current
    var picked by rememberSaveable(initialSelection) { mutableStateOf(initialSelection) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14182B),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.primary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ZODIAC_SIGN_NAMES) { sign ->
                    SignChip(
                        sign = sign,
                        isSelected = picked == sign,
                        onClick = {
                            picked = sign
                            feedback?.fire(CosmicFeedback.Cue.Select)
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (allowClear && initialSelection != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .clickable { onSelect(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Clear",
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                val canSave = picked != null
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (canSave) palette.primary else Color.White.copy(alpha = 0.10f))
                        .clickable(enabled = canSave) {
                            picked?.let(onSelect)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = if (canSave) Color(0xFF0B0E1F) else Color.White.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SignChip(
    sign: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val signPalette = rememberSignPalette(sign)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSelected)
                        listOf(
                            signPalette.primary.copy(alpha = 0.45f),
                            signPalette.secondary.copy(alpha = 0.20f)
                        )
                    else listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                )
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) signPalette.primary
                        else Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            ZodiacGlyph(
                sign = sign,
                strokeColor = Color.White,
                accentColor = signPalette.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = sign,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}
