package com.wheredigo.hikingtracker.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.terrain.generated.Terrain
import com.mapbox.maps.extension.style.terrain.generated.setTerrain
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Jetpack Compose wrapper for Mapbox Maps SDK v11.
 * Features:
 * - Outdoors Topographic Style ("mapbox://styles/mapbox/outdoors-v12")
 * - 3D Terrain DEM with elevation exaggeration for realistic mountain relief
 * - 60-degree camera pitch angle for 3D perspective
 * - Continuous Polyline path drawing for active hike
 * - Smooth camera following user movements and recenter event handling
 */
@Composable
fun MapboxHikingMap(
    pathPoints: List<Point>,
    latestPoint: Point?,
    isTracking: Boolean,
    recenterEvents: SharedFlow<Point>,
    modifier: Modifier = Modifier
) {
    // Initial camera: high pitch (60 deg) for 3D terrain emphasis
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(15.5)
            pitch(60.0)
            bearing(0.0)
        }
    }

    // Handle manual recenter camera requests
    LaunchedEffect(recenterEvents) {
        recenterEvents.collectLatest { targetPoint ->
            mapViewportState.easeTo(
                cameraOptions = cameraOptions {
                    center(targetPoint)
                    pitch(60.0)
                    zoom(16.5)
                },
                animationOptions = MapAnimationOptions.mapAnimationOptions {
                    duration(1200)
                }
            )
        }
    }

    // Smoothly follow latest location during active tracking
    LaunchedEffect(latestPoint, isTracking) {
        if (latestPoint != null) {
            mapViewportState.easeTo(
                cameraOptions = cameraOptions {
                    center(latestPoint)
                    pitch(60.0)
                    if (isTracking) zoom(16.5)
                },
                animationOptions = MapAnimationOptions.mapAnimationOptions {
                    duration(1000)
                }
            )
        }
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        style = {
            MapStyle(style = Style.OUTDOORS)
        }
    ) {
        // Configure 3D Terrain DEM and Location Puck via MapEffect
        MapEffect(Unit) { mapView ->
            // Enable 2D/3D Location Puck with pulsing effect
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }

            // Apply 3D Terrain when style loads
            mapView.mapboxMap.getStyle { style ->
                if (!mapView.mapboxMap.styleSourceExists("mapbox-dem")) {
                    val demSource = rasterDemSource("mapbox-dem") {
                        url("mapbox://mapbox.mapbox-terrain-dem-v1")
                        tileSize(514L)
                        maxzoom(14L)
                    }
                    style.addSource(demSource)
                }
                style.setTerrain(Terrain("mapbox-dem").apply {
                    exaggeration(1.5)
                })
            }
        }

        // Draw continuous Polyline of the tracked hike path
        if (pathPoints.size >= 2) {
            PolylineAnnotationGroup(
                annotations = listOf(
                    PolylineAnnotationOptions()
                        .withPoints(pathPoints)
                        .withLineColor("#00E676")
                        .withLineWidth(6.0)
                        .withLineBorderWidth(2.0)
                        .withLineBorderColor("#004D40")
                )
            )
        }
    }
}
