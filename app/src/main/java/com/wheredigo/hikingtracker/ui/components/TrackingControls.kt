package com.wheredigo.hikingtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheredigo.hikingtracker.R
import com.wheredigo.hikingtracker.data.SavedHike
import com.wheredigo.hikingtracker.ui.theme.CrimsonStopButton
import com.wheredigo.hikingtracker.ui.theme.DeepForestText
import com.wheredigo.hikingtracker.ui.theme.EmeraldStartButton
import com.wheredigo.hikingtracker.utils.Formatters

/**
 * Bottom controls featuring a split Pill Button (#F8E602 / #FFDCB9),
 * sized to match the top MetricsOverlay card when idle and collapsing to a mini-card height when active.
 */
@Composable
fun TrackingControls(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    modifier: Modifier = Modifier,
    savedHikes: List<SavedHike> = emptyList(),
    onSelectSavedHike: (SavedHike) -> Unit = {},
    onSecondaryAction: () -> Unit = {},
    expandedHeight: Dp = 224.dp,
    collapsedHeight: Dp = 62.dp
) {
    val density = LocalDensity.current
    var pillWidthPx by remember { mutableIntStateOf(0) }
    var startContentWidthPx by remember { mutableIntStateOf(0) }
    var isHistoryMode by remember { mutableStateOf(false) }
    var selectedHikeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isTracking) {
        if (isTracking) {
            isHistoryMode = false
        }
    }

    val buttonColor by animateColorAsState(
        targetValue = if (isTracking) CrimsonStopButton else EmeraldStartButton,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ButtonColorAnimation"
    )

    val animatedHeight by animateDpAsState(
        targetValue = if (isTracking) collapsedHeight else expandedHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ButtonHeightAnimation"
    )

    val historyIconAlpha by animateFloatAsState(
        targetValue = when {
            isTracking -> 0f
            isHistoryMode -> 1f
            else -> 0.14f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "HistoryIconAlphaAnimation"
    )

    val startStopAlpha by animateFloatAsState(
        targetValue = if (isHistoryMode && !isTracking) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "StartStopAlphaAnimation"
    )

    val historyListAlpha by animateFloatAsState(
        targetValue = if (isHistoryMode && !isTracking) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "HistoryListAlphaAnimation"
    )

    val pillShape = RoundedCornerShape(percent = 50)

    // Distance from the 25% line to the left edge of the Play icon
    val gapFromDashedLineDp = with(density) {
        if (pillWidthPx > 0 && startContentWidthPx > 0) {
            val dashedLineX = pillWidthPx * 0.25f
            val playIconLeftX = (pillWidthPx - startContentWidthPx) / 2f
            (playIconLeftX - dashedLineX).coerceAtLeast(0f).toDp()
        } else {
            24.dp
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .height(animatedHeight)
                .fillMaxWidth()
                .onSizeChanged { pillWidthPx = it.width }
                .clip(pillShape),
            shape = pillShape,
            color = buttonColor,
            contentColor = DeepForestText,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left 25% History Button
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = !isTracking,
                                onClick = {
                                    isHistoryMode = !isHistoryMode
                                    onSecondaryAction()
                                }
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Hike History",
                            tint = DeepForestText,
                            modifier = Modifier
                                .padding(end = gapFromDashedLineDp)
                                .size(32.dp)
                                .alpha(historyIconAlpha)
                        )
                    }

                    // Right 75% Start/Stop Hike Clickable Area or Saved Hikes Scrollable List
                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .fillMaxHeight()
                            .then(
                                if (!isHistoryMode) {
                                    Modifier.clickable(onClick = onToggleTracking)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (historyListAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(historyListAlpha)
                            ) {
                                if (savedHikes.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(end = 48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No saved hikes yet",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = DeepForestText.copy(alpha = 0.55f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        userScrollEnabled = isHistoryMode,
                                        contentPadding = PaddingValues(
                                            top = 18.dp,
                                            bottom = 18.dp,
                                            start = 4.dp,
                                            end = 52.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(savedHikes, key = { it.id }) { hike ->
                                            val isSelected = hike.id == selectedHikeId
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        if (isSelected) {
                                                            DeepForestText.copy(alpha = 0.12f)
                                                        } else {
                                                            Color.Transparent
                                                        }
                                                    )
                                                    .clickable(enabled = isHistoryMode) {
                                                        selectedHikeId = hike.id
                                                        onSelectSavedHike(hike)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = Formatters.formatTimestamp(hike.timestampMillis),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = DeepForestText,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${Formatters.formatDistance(hike.metrics.distanceMeters)}  •  ${Formatters.formatDuration(hike.metrics.durationSeconds)}  •  ${Formatters.formatElevationGain(hike.metrics.elevationGainMeters)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = DeepForestText.copy(alpha = 0.72f),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Start/Stop Hike Icon & Text centered across the entire pill (same position as before)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(startStopAlpha)
                        .onSizeChanged {
                            if (!isTracking || startContentWidthPx == 0) {
                                startContentWidthPx = it.width
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = if (isTracking) stringResource(R.string.stop_hike) else stringResource(R.string.start_hike),
                        tint = DeepForestText,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isTracking) stringResource(R.string.stop_hike) else stringResource(R.string.start_hike),
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepForestText,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
