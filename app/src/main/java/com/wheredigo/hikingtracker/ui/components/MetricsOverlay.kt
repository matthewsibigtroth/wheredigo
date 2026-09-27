package com.wheredigo.hikingtracker.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheredigo.hikingtracker.ui.HikingUiState
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
 * Top card displaying live hiking metrics in a 2x2 grid using the pastel green, sage, mint, peach & yellow palette.
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
            .clip(RoundedCornerShape(28.dp)),
        color = SurfaceGlassDark,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, SurfaceCardBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Pastel Green Header Banner (#AAF5A3) inspired by the top display panel in the palette
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = PastelSpringGreen
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHERED I GO • HIKE METRICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepForestText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clickable Edit Weight Button in Soft Peach (#FFDCB9)
                        Surface(
                            onClick = onOpenWeightSettings,
                            shape = RoundedCornerShape(50),
                            color = SoftPeachAccent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "Weight: %.0f lbs ✎", uiState.userWeightLbs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepForestText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (uiState.isTracking) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = VibrantYellowButton
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(DeepForestText, CircleShape)
                                    )
                                    Text(
                                        text = "REC",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepForestText,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Grid of Primary Metrics in Crisp White Tiles (#FFFFFF)
            // Row 1: Time Elapsed & Distance Covered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricGridItem(
                    title = "TIME ELAPSED",
                    value = uiState.formattedTime,
                    icon = Icons.Rounded.Schedule,
                    badgeColor = VibrantYellowButton,
                    modifier = Modifier.weight(1f)
                )

                MetricGridItem(
                    title = "DISTANCE",
                    value = uiState.formattedDistance,
                    icon = Icons.Rounded.DirectionsWalk,
                    badgeColor = SoftMintContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Altitude & Calories Burned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricGridItem(
                    title = "ALTITUDE",
                    value = uiState.formattedAltitude,
                    icon = Icons.Rounded.Terrain,
                    badgeColor = PastelSpringGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricGridItem(
                    title = "CALORIES",
                    value = uiState.formattedCalories,
                    icon = Icons.Rounded.LocalFireDepartment,
                    badgeColor = SoftPeachAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Secondary Stats Bar: Elevation Gain & Speed in Soft Mint (#C2ECD4)
            if (uiState.isTracking) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftMintContainer, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gain: ${uiState.formattedElevationGain}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepForestText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = MutedForestText
                    )
                    Text(
                        text = "Speed: ${uiState.formattedSpeed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepForestText,
                        fontWeight = FontWeight.SemiBold
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
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CrispWhiteSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = DeepForestText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedForestText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepForestText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}
