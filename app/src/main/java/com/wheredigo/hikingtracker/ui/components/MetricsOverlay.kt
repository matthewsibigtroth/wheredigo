package com.wheredigo.hikingtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheredigo.hikingtracker.ui.HikingUiState
import com.wheredigo.hikingtracker.ui.theme.AccentCyan
import com.wheredigo.hikingtracker.ui.theme.AccentOrange
import com.wheredigo.hikingtracker.ui.theme.AccentYellow
import com.wheredigo.hikingtracker.ui.theme.EmeraldStartButton
import com.wheredigo.hikingtracker.ui.theme.SurfaceCardBorder
import com.wheredigo.hikingtracker.ui.theme.SurfaceGlassDark
import com.wheredigo.hikingtracker.ui.theme.TextPrimaryLight
import com.wheredigo.hikingtracker.ui.theme.TextSecondaryLight

/**
 * Top semi-transparent frosted card displaying live hiking metrics in a 2x2 grid.
 */
@Composable
fun MetricsOverlay(
    uiState: HikingUiState,
    onOpenWeightSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = SurfaceGlassDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SurfaceCardBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row with App branding, Weight Chip & Live Tracking status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WHERED I GO • HIKE METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    letterSpacing = 1.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clickable Weight Chip
                    Surface(
                        onClick = onOpenWeightSettings,
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.0f lbs", uiState.userWeightLbs),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimaryLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (uiState.isTracking) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EmeraldStartButton, CircleShape)
                            )
                            Text(
                                text = "REC",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldStartButton,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid of Primary Metrics
            // Row 1: Time Elapsed & Distance Covered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricGridItem(
                    title = "TIME ELAPSED",
                    value = uiState.formattedTime,
                    icon = Icons.Rounded.Schedule,
                    iconTint = AccentYellow,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                MetricGridItem(
                    title = "DISTANCE",
                    value = uiState.formattedDistance,
                    icon = Icons.Rounded.DirectionsWalk,
                    iconTint = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Altitude & Calories Burned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricGridItem(
                    title = "ALTITUDE",
                    value = uiState.formattedAltitude,
                    icon = Icons.Rounded.Terrain,
                    iconTint = EmeraldStartButton,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                MetricGridItem(
                    title = "CALORIES",
                    value = uiState.formattedCalories,
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            // Secondary Stats Bar: Elevation Gain & Speed
            if (uiState.isTracking) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gain: ${uiState.formattedElevationGain}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "•",
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "Speed: ${uiState.formattedSpeed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricGridItem(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 22.sp
            )
        }
    }
}
