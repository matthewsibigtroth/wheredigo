package com.wheredigo.hikingtracker.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.wheredigo.hikingtracker.ui.HikingViewModel
import com.wheredigo.hikingtracker.ui.components.MapboxHikingMap
import com.wheredigo.hikingtracker.ui.components.MetricsOverlay
import com.wheredigo.hikingtracker.ui.components.PermissionHandler
import com.wheredigo.hikingtracker.ui.components.TrackingControls
import com.wheredigo.hikingtracker.ui.components.WeightSettingsDialog
import com.wheredigo.hikingtracker.ui.components.getCameraCutoutCenterYPx

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
    val density = LocalDensity.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    val savedHikes by viewModel.savedHikes.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }
    var isHistoryMode by remember { mutableStateOf(false) }
    var metricsCardHeightPx by remember { mutableIntStateOf(0) }
    var miniCardHeightPx by remember { mutableIntStateOf(0) }

    val showMetricsCard = uiState.isTracking || isHistoryMode
    val metricsCardAlpha by animateFloatAsState(
        targetValue = if (showMetricsCard) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "MetricsCardAlphaAnimation"
    )

    val cutoutCenterYPx = getCameraCutoutCenterYPx(view)
    val compassRadiusPx = with(density) { 25.dp.toPx() } // 50.dp compass diameter / 2

    // Distance from the top of the compass to the top of the screen
    val compassTopGapPx = (cutoutCenterYPx - compassRadiusPx).coerceAtLeast(0f)
    val compassBottomYPx = cutoutCenterYPx + compassRadiusPx

    // Distance from the top of the screen to the top of the metrics card
    val metricsTopPaddingDp = with(density) { (compassBottomYPx + compassTopGapPx).toDp() }

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
                modifier = Modifier.fillMaxSize()
            )

            // Top Overlay: Live Metrics Card shown only during an active hike or in history mode
            if (metricsCardAlpha > 0f || metricsCardHeightPx == 0) {
                MetricsOverlay(
                    uiState = uiState,
                    onOpenWeightSettings = {
                        if (showMetricsCard) {
                            showWeightDialog = true
                        }
                    },
                    onMiniCardHeightMeasured = { heightPx ->
                        miniCardHeightPx = heightPx
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = metricsTopPaddingDp,
                            start = 16.dp,
                            end = 16.dp
                        )
                        .alpha(metricsCardAlpha)
                        .onSizeChanged { size ->
                            if (!uiState.isTracking && size.height > metricsCardHeightPx) {
                                metricsCardHeightPx = size.height
                            }
                        }
                )
            }

            val matchedExpandedHeight = if (metricsCardHeightPx > 0) {
                with(density) { metricsCardHeightPx.toDp() }
            } else {
                224.dp
            }

            val matchedMiniCardHeight = if (miniCardHeightPx > 0) {
                with(density) { miniCardHeightPx.toDp() }
            } else {
                62.dp
            }

            // Bottom Controls: Start/Stop Action Button with bottom padding matching metricsTopPaddingDp
            TrackingControls(
                isTracking = uiState.isTracking,
                onToggleTracking = {
                    viewModel.toggleTracking(context)
                },
                isHistoryMode = isHistoryMode,
                onHistoryModeChange = { historyActive ->
                    isHistoryMode = historyActive
                },
                savedHikes = savedHikes,
                onSelectSavedHike = { hike ->
                    viewModel.loadSavedHike(hike)
                },
                expandedHeight = matchedExpandedHeight,
                collapsedHeight = matchedMiniCardHeight,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = metricsTopPaddingDp)
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
