package com.wheredigo.hikingtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
 * Includes a compact show/hide toggle affordance.
 */
@Composable
fun MetricsOverlay(
    uiState: HikingUiState,
    onOpenWeightSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var maxCardHeightPx by remember { mutableIntStateOf(0) }
    val uniformCardMinHeight: Dp = with(density) { maxCardHeightPx.toDp() }
    var isExpanded by rememberSaveable { mutableStateOf(true) }

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
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Pastel Green Header Banner (#AAF5A3)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = PastelSpringGreen
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHERED I GO • HIKE METRICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepForestText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Clickable Edit Weight Button in Soft Peach (#FFDCB9)
                        Surface(
                            onClick = onOpenWeightSettings,
                            shape = RoundedCornerShape(50),
                            color = SoftPeachAccent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.0f lbs", uiState.userWeightLbs),
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

                        // Small circular Show/Hide Chevron Button
                        Surface(
                            onClick = { isExpanded = !isExpanded },
                            shape = CircleShape,
                            color = SoftMintContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isExpanded) {
                                        Icons.Rounded.KeyboardArrowUp
                                    } else {
                                        Icons.Rounded.KeyboardArrowDown
                                    },
                                    contentDescription = if (isExpanded) "Hide metrics" else "Show metrics",
                                    tint = DeepForestText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Collapsible 2x2 Grid & Secondary Stats Bar
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Time Elapsed & Distance Covered
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricGridItem(
                            value = uiState.formattedTime,
                            icon = Icons.Rounded.Schedule,
                            minHeight = uniformCardMinHeight,
                            onHeightMeasured = { h -> if (h > maxCardHeightPx) maxCardHeightPx = h },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        MetricGridItem(
                            value = uiState.formattedDistance,
                            icon = Icons.Rounded.DirectionsWalk,
                            minHeight = uniformCardMinHeight,
                            onHeightMeasured = { h -> if (h > maxCardHeightPx) maxCardHeightPx = h },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Altitude & Calories Burned
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricGridItem(
                            value = uiState.formattedAltitude,
                            icon = Icons.Rounded.Terrain,
                            minHeight = uniformCardMinHeight,
                            onHeightMeasured = { h -> if (h > maxCardHeightPx) maxCardHeightPx = h },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        MetricGridItem(
                            value = uiState.formattedCalories,
                            icon = Icons.Rounded.LocalFireDepartment,
                            minHeight = uniformCardMinHeight,
                            onHeightMeasured = { h -> if (h > maxCardHeightPx) maxCardHeightPx = h },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
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
    }
}

@Composable
private fun MetricGridItem(
    value: String,
    icon: ImageVector,
    minHeight: Dp,
    onHeightMeasured: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = minHeight)
            .onSizeChanged { onHeightMeasured(it.height) },
        shape = RoundedCornerShape(20.dp),
        color = CrispWhiteSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SoftMintContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepForestText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = DeepForestText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 22.sp
            )
        }
    }
}
