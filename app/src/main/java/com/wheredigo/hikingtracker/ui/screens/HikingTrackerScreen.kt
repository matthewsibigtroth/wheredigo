package com.wheredigo.hikingtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wheredigo.hikingtracker.ui.HikingViewModel
import com.wheredigo.hikingtracker.ui.components.MapboxHikingMap
import com.wheredigo.hikingtracker.ui.components.MetricsOverlay
import com.wheredigo.hikingtracker.ui.components.PermissionHandler
import com.wheredigo.hikingtracker.ui.components.TrackingControls
import com.wheredigo.hikingtracker.ui.components.WeightSettingsDialog

/**
 * Main Single-Screen UI for WhereDiGo Hiking Tracker.
 * Integrates Fullscreen 3D Mapbox Map, Top Frosted Metrics Card, and Bottom Pill Action Controls.
 */
@Composable
fun HikingTrackerScreen(
    viewModel: HikingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initializePreferences(context)
        viewModel.fetchCurrentLocation(context)
    }

    PermissionHandler(
        onPermissionsGranted = {
            viewModel.fetchCurrentLocation(context)
        }
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Fullscreen 3D Topographic Mapbox Map
            MapboxHikingMap(
                pathPoints = uiState.pathPoints,
                latestPoint = uiState.latestPoint,
                isTracking = uiState.isTracking,
                recenterEvents = viewModel.recenterCameraEvents,
                modifier = Modifier.fillMaxSize()
            )

            // Top Overlay: Live Metrics Card (Status bar safe)
            MetricsOverlay(
                uiState = uiState,
                onOpenWeightSettings = {
                    showWeightDialog = true
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            )

            // Bottom Controls: Start/Stop Pill Action Button + Recenter FAB
            TrackingControls(
                isTracking = uiState.isTracking,
                onToggleTracking = {
                    viewModel.toggleTracking(context)
                },
                onRecenterMap = {
                    viewModel.fetchCurrentLocation(context)
                    viewModel.requestRecenter()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
            )

            // Weight Settings Dialog
            if (showWeightDialog) {
                WeightSettingsDialog(
                    currentWeightLbs = uiState.userWeightLbs,
                    onDismiss = { showWeightDialog = false },
                    onSaveWeight = { newWeightLbs ->
                        viewModel.updateUserWeight(newWeightLbs, context)
                    }
                )
            }
        }
    }
}
