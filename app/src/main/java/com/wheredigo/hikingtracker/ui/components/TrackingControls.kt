package com.wheredigo.hikingtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Bottom controls featuring the prominent Start/Stop Action Button (#F8E602 / #FFDCB9),
 * sized to match the top MetricsOverlay card when idle and collapsing to a mini-card height when active.
 */
@Composable
fun TrackingControls(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    modifier: Modifier = Modifier,
    expandedHeight: Dp = 224.dp,
    collapsedHeight: Dp = 62.dp
) {
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Massive Prominent Center Action Pill Button matching the Metrics Card dimensions
        Button(
            onClick = onToggleTracking,
            modifier = Modifier
                .align(Alignment.Center)
                .height(animatedHeight)
                .fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = DeepForestText
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Row(
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
