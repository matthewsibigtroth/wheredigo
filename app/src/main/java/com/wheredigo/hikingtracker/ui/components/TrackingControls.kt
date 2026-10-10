package com.wheredigo.hikingtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheredigo.hikingtracker.R
import com.wheredigo.hikingtracker.ui.theme.CrimsonStopButton
import com.wheredigo.hikingtracker.ui.theme.DeepForestText
import com.wheredigo.hikingtracker.ui.theme.EmeraldStartButton

/**
 * Bottom controls featuring a split Pill Button (#F8E602 / #FFDCB9),
 * sized to match the top MetricsOverlay card when idle and collapsing to a mini-card height when active.
 */
@Composable
fun TrackingControls(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    modifier: Modifier = Modifier,
    onSecondaryAction: () -> Unit = {},
    expandedHeight: Dp = 224.dp,
    collapsedHeight: Dp = 62.dp
) {
    val density = LocalDensity.current
    var pillWidthPx by remember { mutableIntStateOf(0) }
    var startContentWidthPx by remember { mutableIntStateOf(0) }

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
        targetValue = if (isTracking) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "HistoryIconAlphaAnimation"
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
                                onClick = onSecondaryAction
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Hike History",
                            tint = DeepForestText.copy(alpha = 0.14f),
                            modifier = Modifier
                                .padding(end = gapFromDashedLineDp)
                                .size(32.dp)
                                .alpha(historyIconAlpha)
                        )
                    }

                    // Right 75% Start/Stop Hike Clickable Area
                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .fillMaxHeight()
                            .clickable(onClick = onToggleTracking)
                    )
                }

                // Start/Stop Hike Icon & Text centered across the entire pill (same position as before)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
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
