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
import com.wheredigo.hikingtracker.ui.theme.CrispWhiteSurface
import com.wheredigo.hikingtracker.ui.theme.DeepForestText
import com.wheredigo.hikingtracker.ui.theme.MutedForestText
import com.wheredigo.hikingtracker.ui.theme.PastelSpringGreen
import com.wheredigo.hikingtracker.ui.theme.SoftSageBackground
import com.wheredigo.hikingtracker.ui.theme.VibrantYellowButton
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

                    // DeepForestText bezel ring around the camera cutout
                    drawCircle(
                        color = DeepForestText.copy(alpha = 0.92f),
                        radius = ringCenterRadius,
                        center = center,
                        style = Stroke(width = ringThickness)
                    )

                    // PastelSpringGreen outer border
                    drawCircle(
                        color = PastelSpringGreen,
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
                            color = if (isCardinal) SoftSageBackground else MutedForestText,
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

                    // VibrantYellowButton North Needle (top of ring, outside the camera hole)
                    val needleHalfWidth = 4.8.dp.toPx()
                    val northPath = Path().apply {
                        moveTo(center.x, center.y - outerRadius + 0.75.dp.toPx()) // Tip at outer top edge
                        lineTo(center.x - needleHalfWidth, center.y - innerRadius - 0.75.dp.toPx())
                        lineTo(center.x + needleHalfWidth, center.y - innerRadius - 0.75.dp.toPx())
                        close()
                    }
                    drawPath(path = northPath, color = VibrantYellowButton)

                    // CrispWhiteSurface South Needle (bottom of ring, outside the camera hole)
                    val southPath = Path().apply {
                        moveTo(center.x, center.y + outerRadius - 0.75.dp.toPx()) // Tip at outer bottom edge
                        lineTo(center.x - needleHalfWidth, center.y + innerRadius + 0.75.dp.toPx())
                        lineTo(center.x + needleHalfWidth, center.y + innerRadius + 0.75.dp.toPx())
                        close()
                    }
                    drawPath(path = southPath, color = CrispWhiteSurface)
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

            // Apply 3D Terrain and UI Palette when style loads
            mapView.mapboxMap.getStyle { style ->
                applyUiPaletteToMapStyle(style)

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
                        .withLineColor("#F8E602")
                        .withLineWidth(6.0)
                        .withLineBorderWidth(2.0)
                        .withLineBorderColor("#171D16")
                )
            )
        }
    }
}

/**
 * Restyles Mapbox Outdoors layers to match the app's UI palette from Color.kt:
 * - SoftSageBackground (#ECF2E6): Base land & label halos
 * - PastelSpringGreen (#AAF5A3): Parks, forests, woods & vegetation
 * - SoftMintContainer (#C2ECD4): Lakes, rivers, oceans & waterways
 * - SoftPeachAccent (#FFDCB9): Roads, highways & buildings
 * - MutedForestText (#4A5848): Contours, hillshade shadows & borders
 * - DeepForestText (#171D16): Trails, paths & typography labels
 * - CrispWhiteSurface (#FFFFFF): Hillshade highlights
 */
private fun applyUiPaletteToMapStyle(style: Style) {
    val softSageBackground = "#ECF2E6"
    val pastelSpringGreen = "#AAF5A3"
    val softMintContainer = "#C2ECD4"
    val softPeachAccent = "#FFDCB9"
    val vibrantYellow = "#F8E602"
    val crispWhite = "#FFFFFF"
    val deepForestText = "#171D16"
    val mutedForestText = "#4A5848"

    for (layer in style.styleLayers) {
        val id = layer.id.lowercase()
        when (layer.type) {
            "background" -> {
                style.setStyleLayerProperty(
                    layer.id,
                    "background-color",
                    com.mapbox.bindgen.Value.valueOf(softSageBackground)
                )
            }
            "fill" -> {
                val fillColor = when {
                    id.contains("water") || id.contains("wetland") || id.contains("glacier") -> softMintContainer
                    id.contains("park") || id.contains("national") || id.contains("wood") ||
                        id.contains("forest") || id.contains("landcover") || id.contains("landuse") ||
                        id.contains("grass") || id.contains("scrub") || id.contains("vegetation") -> pastelSpringGreen
                    id.contains("building") -> softPeachAccent
                    else -> softSageBackground
                }
                style.setStyleLayerProperty(
                    layer.id,
                    "fill-color",
                    com.mapbox.bindgen.Value.valueOf(fillColor)
                )
                if (id.contains("building")) {
                    style.setStyleLayerProperty(
                        layer.id,
                        "fill-outline-color",
                        com.mapbox.bindgen.Value.valueOf(mutedForestText)
                    )
                }
            }
            "hillshade" -> {
                style.setStyleLayerProperty(
                    layer.id,
                    "hillshade-shadow-color",
                    com.mapbox.bindgen.Value.valueOf(mutedForestText)
                )
                style.setStyleLayerProperty(
                    layer.id,
                    "hillshade-highlight-color",
                    com.mapbox.bindgen.Value.valueOf(crispWhite)
                )
                style.setStyleLayerProperty(
                    layer.id,
                    "hillshade-accent-color",
                    com.mapbox.bindgen.Value.valueOf(pastelSpringGreen)
                )
            }
            "line" -> {
                val lineColor = when {
                    id.contains("water") || id.contains("river") || id.contains("stream") ||
                        id.contains("canal") || id.contains("drain") -> softMintContainer
                    id.contains("contour") -> mutedForestText
                    id.contains("path") || id.contains("trail") || id.contains("track") ||
                        id.contains("pedestrian") || id.contains("footway") || id.contains("steps") ||
                        id.contains("cycleway") -> deepForestText
                    id.contains("case") || id.contains("casing") -> mutedForestText
                    id.contains("road") || id.contains("street") || id.contains("highway") ||
                        id.contains("motorway") || id.contains("trunk") || id.contains("primary") ||
                        id.contains("secondary") || id.contains("tertiary") || id.contains("bridge") ||
                        id.contains("tunnel") -> softPeachAccent
                    id.contains("admin") || id.contains("boundary") || id.contains("border") -> mutedForestText
                    else -> softMintContainer
                }
                style.setStyleLayerProperty(
                    layer.id,
                    "line-color",
                    com.mapbox.bindgen.Value.valueOf(lineColor)
                )
            }
            "symbol" -> {
                style.setStyleLayerProperty(
                    layer.id,
                    "text-color",
                    com.mapbox.bindgen.Value.valueOf(deepForestText)
                )
                style.setStyleLayerProperty(
                    layer.id,
                    "text-halo-color",
                    com.mapbox.bindgen.Value.valueOf(softSageBackground)
                )
                style.setStyleLayerProperty(
                    layer.id,
                    "icon-color",
                    com.mapbox.bindgen.Value.valueOf(deepForestText)
                )
                style.setStyleLayerProperty(
                    layer.id,
                    "icon-halo-color",
                    com.mapbox.bindgen.Value.valueOf(softSageBackground)
                )
            }
            "sky" -> {
                style.setStyleLayerProperty(
                    layer.id,
                    "sky-atmosphere-color",
                    com.mapbox.bindgen.Value.valueOf(softSageBackground)
                )
                style.setStyleLayerProperty(
                    layer.id,
                    "sky-atmosphere-sun-color",
                    com.mapbox.bindgen.Value.valueOf(vibrantYellow)
                )
            }
        }
    }
}
