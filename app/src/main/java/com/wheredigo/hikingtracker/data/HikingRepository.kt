package com.wheredigo.hikingtracker.data

import android.location.Location
import com.mapbox.geojson.Point
import com.wheredigo.hikingtracker.utils.CalorieCalculator
import com.wheredigo.hikingtracker.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton repository managing active hiking session state.
 * Shared seamlessly between [com.wheredigo.hikingtracker.service.HikingService] and [com.wheredigo.hikingtracker.ui.HikingViewModel].
 */
object HikingRepository {

    private val _sessionState = MutableStateFlow(HikingSessionState())
    val sessionState: StateFlow<HikingSessionState> = _sessionState.asStateFlow()

    private var previousLocation: Location? = null
    private var lastRecordedAltitude: Double? = null

    /**
     * Updates the user's weight in pounds and adjusts live calorie calculation weight.
     */
    fun setUserWeightLbs(weightLbs: Double) {
        _sessionState.update { current ->
            current.copy(userWeightLbs = weightLbs)
        }
    }

    /**
     * Initializes a new hiking tracking session.
     */
    fun startSession(initialWeightLbs: Double? = null) {
        previousLocation = null
        lastRecordedAltitude = null
        _sessionState.update { current ->
            HikingSessionState(
                isTracking = true,
                pathPoints = emptyList(),
                metrics = HikingMetrics(),
                latestPoint = null,
                userWeightLbs = initialWeightLbs ?: current.userWeightLbs
            )
        }
    }

    /**
     * Stops the active hiking tracking session.
     */
    fun stopSession() {
        _sessionState.update { current ->
            current.copy(isTracking = false)
        }
        previousLocation = null
        lastRecordedAltitude = null
    }

    /**
     * Increments the duration timer by one second and applies accurate gradient- and speed-aware calorie expenditure.
     */
    fun tickTimer() {
        _sessionState.update { current ->
            if (!current.isTracking) return@update current

            val updatedDuration = current.metrics.durationSeconds + 1L
            // Compute real-time calorie burn for 1 second slice based on current speed and terrain grade
            val instantSpeed = current.metrics.currentSpeedKmh
            val instantGrade = current.metrics.currentGradePercentage

            val met = CalorieCalculator.calculateMET(
                speedKmh = instantSpeed,
                gradePercentage = instantGrade
            )
            val weightKg = CalorieCalculator.lbsToKg(current.userWeightLbs)
            val addedCalories = CalorieCalculator.calculateCaloriesForInterval(met, 1L, weightKg)

            val updatedMetrics = current.metrics.copy(
                durationSeconds = updatedDuration,
                caloriesBurned = current.metrics.caloriesBurned + addedCalories
            )

            current.copy(metrics = updatedMetrics)
        }
    }

    /**
     * Processes a new GPS location update from FusedLocationProviderClient.
     */
    fun onLocationUpdate(location: Location) {
        if (!LocationUtils.isValidLocation(location)) {
            return
        }

        val point = LocationUtils.toMapboxPoint(location)
        val currentAltitude = if (location.hasAltitude()) location.altitude else 0.0

        _sessionState.update { current ->
            if (!current.isTracking) {
                // If not tracking, just update latest location for camera centering
                return@update current.copy(latestPoint = point)
            }

            var deltaDistance = 0.0
            var deltaElevationGain = 0.0
            var deltaElevationChange = 0.0
            var gradePercentage = 0.0
            val prevLoc = previousLocation

            if (prevLoc != null && LocationUtils.isSignificantMove(prevLoc, location)) {
                deltaDistance = LocationUtils.calculateDistance(prevLoc, location)
                if (lastRecordedAltitude != null) {
                    deltaElevationGain = LocationUtils.calculateElevationGain(
                        lastRecordedAltitude!!,
                        currentAltitude
                    )
                    deltaElevationChange = LocationUtils.calculateElevationChange(
                        lastRecordedAltitude!!,
                        currentAltitude
                    )
                    gradePercentage = LocationUtils.calculateGradePercentage(
                        deltaElevationChange,
                        deltaDistance
                    )
                }
            }

            // Speed in km/h (location.speed is in m/s)
            val speedKmh = if (location.hasSpeed() && location.speed > 0f) {
                location.speed * 3.6
            } else if (prevLoc != null && deltaDistance > 0.0) {
                val timeDiffSec = (location.time - prevLoc.time) / 1000.0
                if (timeDiffSec > 0) (deltaDistance / timeDiffSec) * 3.6 else 0.0
            } else {
                0.0
            }

            // Update path points
            val updatedPoints = if (prevLoc == null || deltaDistance > 0.0) {
                current.pathPoints + point
            } else {
                current.pathPoints
            }

            val updatedMetrics = current.metrics.copy(
                distanceMeters = current.metrics.distanceMeters + deltaDistance,
                currentAltitudeMeters = currentAltitude,
                elevationGainMeters = current.metrics.elevationGainMeters + deltaElevationGain,
                currentSpeedKmh = speedKmh,
                currentGradePercentage = if (deltaDistance > 0.0) gradePercentage else 0.0
            )

            previousLocation = location
            lastRecordedAltitude = currentAltitude

            current.copy(
                pathPoints = updatedPoints,
                metrics = updatedMetrics,
                latestPoint = point
            )
        }
    }
}
