package com.wheredigo.hikingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wheredigo.hikingtracker.ui.theme.EmeraldStartButton
import com.wheredigo.hikingtracker.ui.theme.SurfaceCardBorder
import com.wheredigo.hikingtracker.ui.theme.SurfaceGlassDark
import com.wheredigo.hikingtracker.ui.theme.TextPrimaryLight
import com.wheredigo.hikingtracker.ui.theme.TextSecondaryLight
import com.wheredigo.hikingtracker.utils.CalorieCalculator
import java.util.Locale

/**
 * Dialog allowing the user to configure their body weight in pounds (lbs).
 * Used for precise, personalized MET calorie burn calculations.
 */
@Composable
fun WeightSettingsDialog(
    currentWeightLbs: Double,
    onDismiss: () -> Unit,
    onSaveWeight: (Double) -> Unit
) {
    var weightInput by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentWeightLbs)) }
    var isError by remember { mutableStateOf(false) }

    val currentInputDouble = weightInput.toDoubleOrNull()
    val equivalentKg = currentInputDouble?.let { CalorieCalculator.lbsToKg(it) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceGlassDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = EmeraldStartButton,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "User Weight",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your weight in pounds (lbs) to accurately calculate calories burned based on hiking intensity and slope.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Weight Input Field
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { input ->
                        weightInput = input
                        val parsed = input.toDoubleOrNull()
                        isError = parsed == null || parsed <= 30.0 || parsed >= 700.0
                    },
                    label = { Text("Weight (lbs)") },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryLight,
                        unfocusedTextColor = TextPrimaryLight,
                        focusedBorderColor = EmeraldStartButton,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = EmeraldStartButton,
                        unfocusedLabelColor = TextSecondaryLight,
                        cursorColor = EmeraldStartButton
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                // Live kg Conversion hint
                if (equivalentKg != null && !isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "≈ %.1f kg", equivalentKg),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldStartButton
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Increment/Decrement Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickWeightButton(label = "-5") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.1f", (current - 5.0).coerceAtLeast(40.0))
                        isError = false
                    }
                    QuickWeightButton(label = "-1") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.1f", (current - 1.0).coerceAtLeast(40.0))
                        isError = false
                    }
                    QuickWeightButton(label = "+1") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.1f", (current + 1.0).coerceAtMost(600.0))
                        isError = false
                    }
                    QuickWeightButton(label = "+5") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.1f", (current + 5.0).coerceAtMost(600.0))
                        isError = false
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondaryLight)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val parsed = weightInput.toDoubleOrNull()
                            if (parsed != null && parsed in 30.0..700.0) {
                                onSaveWeight(parsed)
                                onDismiss()
                            } else {
                                isError = true
                            }
                        },
                        enabled = !isError && weightInput.toDoubleOrNull() != null,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldStartButton),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Weight", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickWeightButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = Modifier.height(36.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = label, color = TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
