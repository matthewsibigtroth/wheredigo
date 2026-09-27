package com.wheredigo.hikingtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wheredigo.hikingtracker.ui.theme.CrispWhiteSurface
import com.wheredigo.hikingtracker.ui.theme.DeepForestText
import com.wheredigo.hikingtracker.ui.theme.MutedForestText
import com.wheredigo.hikingtracker.ui.theme.PastelSpringGreen
import com.wheredigo.hikingtracker.ui.theme.SoftMintContainer
import com.wheredigo.hikingtracker.ui.theme.SoftPeachAccent
import com.wheredigo.hikingtracker.ui.theme.SurfaceCardBorder
import com.wheredigo.hikingtracker.ui.theme.SurfaceGlassDark
import com.wheredigo.hikingtracker.ui.theme.VibrantYellowButton
import java.util.Locale

/**
 * Dialog allowing the user to configure their body weight in pounds (lbs).
 * Styled with the pastel green, sage, mint, peach & yellow palette.
 */
@Composable
fun WeightSettingsDialog(
    currentWeightLbs: Double,
    onDismiss: () -> Unit,
    onSaveWeight: (Double) -> Unit
) {
    var weightInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentWeightLbs)) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PastelSpringGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null,
                            tint = DeepForestText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Edit Weight (lbs)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter your weight in pounds (lbs) to personalize your hike calorie calculation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedForestText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Weight Input Field in Pounds (lbs)
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { input ->
                        weightInput = input
                        val parsed = input.toDoubleOrNull()
                        isError = parsed == null || parsed <= 30.0 || parsed >= 700.0
                    },
                    label = { Text("Weight in Pounds (lbs)") },
                    suffix = { Text("lbs", color = MutedForestText, fontWeight = FontWeight.SemiBold) },
                    singleLine = true,
                    isError = isError,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepForestText,
                        unfocusedTextColor = DeepForestText,
                        focusedContainerColor = CrispWhiteSurface,
                        unfocusedContainerColor = CrispWhiteSurface,
                        focusedBorderColor = DeepForestText,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedLabelColor = DeepForestText,
                        unfocusedLabelColor = MutedForestText,
                        cursorColor = DeepForestText
                    ),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Increment/Decrement Buttons in Pounds (lbs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickWeightButton(label = "-5 lbs") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.0f", (current - 5.0).coerceAtLeast(40.0))
                        isError = false
                    }
                    QuickWeightButton(label = "-1 lb") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.0f", (current - 1.0).coerceAtLeast(40.0))
                        isError = false
                    }
                    QuickWeightButton(label = "+1 lb") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.0f", (current + 1.0).coerceAtMost(600.0))
                        isError = false
                    }
                    QuickWeightButton(label = "+5 lbs") {
                        val current = weightInput.toDoubleOrNull() ?: currentWeightLbs
                        weightInput = String.format(Locale.US, "%.0f", (current + 5.0).coerceAtMost(600.0))
                        isError = false
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Cancel in Peach / Save in Vibrant Yellow)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftPeachAccent,
                            contentColor = DeepForestText
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold, color = DeepForestText)
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VibrantYellowButton,
                            contentColor = DeepForestText
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Save (lbs)", fontWeight = FontWeight.Bold, color = DeepForestText)
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
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftMintContainer,
            contentColor = DeepForestText
        ),
        modifier = Modifier.height(38.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Text(text = label, color = DeepForestText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
