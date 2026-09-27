package com.wheredigo.hikingtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheredigo.hikingtracker.R
import com.wheredigo.hikingtracker.ui.theme.CrimsonStopButton
import com.wheredigo.hikingtracker.ui.theme.EmeraldStartButton
import com.wheredigo.hikingtracker.ui.theme.SurfaceGlassDark

/**
 * Bottom controls featuring the prominent Start/Stop Pill Button and the Map Recenter FAB.
 */
@Composable
fun TrackingControls(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    onRecenterMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor by animateColorAsState(
        targetValue = if (isTracking) CrimsonStopButton else EmeraldStartButton,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ButtonColorAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Massive Prominent Center Action Pill Button
        Button(
            onClick = onToggleTracking,
            modifier = Modifier
                .align(Alignment.Center)
                .height(64.dp)
                .fillMaxWidth(0.72f),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
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
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (isTracking) stringResource(R.string.stop_hike) else stringResource(R.string.start_hike),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Recenter My Location FAB on the right
        FloatingActionButton(
            onClick = onRecenterMap,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(52.dp),
            shape = CircleShape,
            containerColor = SurfaceGlassDark,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "Recenter Map",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
