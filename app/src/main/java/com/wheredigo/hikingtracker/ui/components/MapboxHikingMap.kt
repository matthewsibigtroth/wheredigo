package com.wheredigo.hikingtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.terrain.generated.Terrain
import com.mapbox.maps.extension.style.terrain.generated.setTerrain
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.location
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Jetpack Compose wrapper for Mapbox Maps SDK v11.
 * Features:
 * - Outdoors Topographic Style ("mapbox://styles/mapbox/outdoors-v12")
 * - 3D Terrain DEM with elevation exaggeration for realistic mountain relief
 * - 60-degree camera pitch angle for 3D perspective
 * - Continuous Polyline path drawing for active hike
 * - Smooth camera following user movements
 */
@Composable
fun MapboxHikingMap(
    pathPoints: List<Point>,
    latestPoint: Point?,
    isTracking: Boolean,
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

    // Smoothly follow latest location
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

    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val cutoutRect = insets?.displayCutout?.boundingRects?.firstOrNull()

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        compass = {
            Compass(
                alignment = Alignment.TopCenter,
                contentPadding = PaddingValues(0.dp),
                fadeWhenFacingNorth = false,
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val cutoutCenterY = cutoutRect?.exactCenterY()
                            ?: (24f * view.resources.displayMetrics.density)
                        val offsetY = (cutoutCenterY - placeable.height / 2f).roundToInt()
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, offsetY)
                        }
                    }
            ) {
                // 42dp * 1.2 = ~50.dp compass bezel hugging the front camera
                Canvas(modifier = Modifier.size(50.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f
                    val cutoutRadiusPx = cutoutRect?.let {
                        maxOf(it.width(), it.height()) / 2f
                    } ?: 13.dp.toPx()

                    // Ring band between camera cutout edge and outer compass edge
                    val innerRadius = cutoutRadiusPx.coerceAtMost(outerRadius * 0.56f)
                    val ringCenterRadius = (innerRadius + outerRadius) / 2f
                    val ringThickness = outerRadius - innerRadius

                    // Dark bezel ring around the camera cutout
                    drawCircle(
                        color = Color(0xD9171D16),
                        radius = ringCenterRadius,
                        center = center,
                        style = Stroke(width = ringThickness)
                    )

                    // Crisp outer border
                    drawCircle(
                        color = Color(0x80FFFFFF),
                        radius = outerRadius - 0.85.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // Degree tick marks around the bezel (skip North & South where pointers sit)
                    for (angleDeg in 0 until 360 step 30) {
                        if (angleDeg == 0 || angleDeg == 180) continue
                        val rad = Math.toRadians((angleDeg - 90).toDouble())
                        val isCardinal = angleDeg % 90 == 0
                        val tickStart = if (isCardinal) innerRadius + 1.8.dp.toPx() else innerRadius + 3.5.dp.toPx()
                        val tickEnd = outerRadius - 1.8.dp.toPx()
                        drawLine(
                            color = if (isCardinal) Color.White else Color(0x99FFFFFF),
                            start = Offset(
                                center.x + (tickStart * cos(rad)).toFloat(),
                                center.y + (tickStart * sin(rad)).toFloat()
                            ),
                            end = Offset(
                                center.x + (tickEnd * cos(rad)).toFloat(),
                                center.y + (tickEnd * sin(rad)).toFloat()
                            ),
                            strokeWidth = if (isCardinal) 1.8.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Bold Red North Needle (top of ring, outside the camera hole)
                    val needleHalfWidth = 4.8.dp.toPx()
                    val northPath = Path().apply {
                        moveTo(center.x, center.y - outerRadius + 0.75.dp.toPx()) // Tip at outer top edge
                        lineTo(center.x - needleHalfWidth, center.y - innerRadius - 0.75.dp.toPx())
                        lineTo(center.x + needleHalfWidth, center.y - innerRadius - 0.75.dp.toPx())
                        close()
                    }
                    drawPath(path = northPath, color = Color(0xFFFF3B30))

                    // Crisp White South Needle (bottom of ring, outside the camera hole)
                    val southPath = Path().apply {
                        moveTo(center.x, center.y + outerRadius - 0.75.dp.toPx()) // Tip at outer bottom edge
                        lineTo(center.x - needleHalfWidth, center.y + innerRadius + 0.75.dp.toPx())
                        lineTo(center.x + needleHalfWidth, center.y + innerRadius + 0.75.dp.toPx())
                        close()
                    }
                    drawPath(path = southPath, color = Color.White)
                }
            }
        },
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
                        .withLineColor("#AAF5A3")
                        .withLineWidth(6.0)
                        .withLineBorderWidth(2.0)
                        .withLineBorderColor("#171D16")
                )
            )
        }
    }
}
