package com.wheredigo.hikingtracker.data

import com.mapbox.geojson.Point

/**
 * Real-time hiking metrics calculated during an active tracking session.
 *
 * @property durationSeconds Elapsed time in seconds.
 * @property distanceMeters Cumulative distance walked in meters.
 * @property currentAltitudeMeters Current elevation above sea level in meters.
 * @property elevationGainMeters Cumulative ascent / elevation gain in meters.
 * @property caloriesBurned Cumulative calories burned in kcal based on MET and slope.
 * @property currentSpeedKmh Instantaneous or recent speed in km/h.
 * @property currentGradePercentage Current terrain slope percentage (positive for uphill, negative for downhill).
 */
data class HikingMetrics(
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentAltitudeMeters: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val caloriesBurned: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val currentGradePercentage: Double = 0.0
)

/**
 * State representing an active or idle hiking session.
 *
 * @property isTracking True if the hike is actively being recorded by the Foreground Service.
 * @property pathPoints Complete list of GPS coordinates recorded in this session for Mapbox polyline rendering.
 * @property metrics Current aggregate hike statistics.
 * @property latestPoint Latest recorded point (latitude/longitude), if available.
 */
data class HikingSessionState(
    val isTracking: Boolean = false,
    val pathPoints: List<Point> = emptyList(),
    val metrics: HikingMetrics = HikingMetrics(),
    val latestPoint: Point? = null,
    val userWeightLbs: Double = com.wheredigo.hikingtracker.utils.CalorieCalculator.DEFAULT_WEIGHT_LBS
)
