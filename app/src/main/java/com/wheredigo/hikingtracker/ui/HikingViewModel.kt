package com.wheredigo.hikingtracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.wheredigo.hikingtracker.data.HikingMetrics
import com.wheredigo.hikingtracker.data.HikingRepository
import com.wheredigo.hikingtracker.data.HikingSessionState
import com.wheredigo.hikingtracker.service.HikingService
import com.wheredigo.hikingtracker.utils.Formatters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State exposed to the Jetpack Compose layer.
 */
data class HikingUiState(
    val isTracking: Boolean = false,
    val pathPoints: List<Point> = emptyList(),
    val latestPoint: Point? = null,
    val rawMetrics: HikingMetrics = HikingMetrics(),
    val formattedTime: String = "00:00:00",
    val formattedDistance: String = "0.00 mi",
    val formattedAltitude: String = "0 ft\n(0 m)",
    val formattedCalories: String = "0 kcal",
    val formattedSpeed: String = "0.0 mph",
    val formattedElevationGain: String = "+0 ft",
    val userWeightLbs: Double = com.wheredigo.hikingtracker.utils.CalorieCalculator.DEFAULT_WEIGHT_LBS
)

/**
 * ViewModel managing UI state and user interactions for the hiking tracker screen.
 */
class HikingViewModel : ViewModel() {

    // One-time events for UI (e.g. camera recenter requests)
    private val _recenterCameraEvents = MutableSharedFlow<Point>(extraBufferCapacity = 1)
    val recenterCameraEvents: SharedFlow<Point> = _recenterCameraEvents.asSharedFlow()

    val uiState: StateFlow<HikingUiState> = HikingRepository.sessionState
        .map { session: HikingSessionState ->
            val m = session.metrics
            HikingUiState(
                isTracking = session.isTracking,
                pathPoints = session.pathPoints,
                latestPoint = session.latestPoint,
                rawMetrics = m,
                formattedTime = Formatters.formatDuration(m.durationSeconds),
                formattedDistance = Formatters.formatDistance(m.distanceMeters),
                formattedAltitude = Formatters.formatAltitude(m.currentAltitudeMeters),
                formattedCalories = Formatters.formatCalories(m.caloriesBurned),
                formattedSpeed = Formatters.formatSpeed(m.currentSpeedKmh),
                formattedElevationGain = Formatters.formatElevationGain(m.elevationGainMeters),
                userWeightLbs = session.userWeightLbs
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HikingUiState()
        )

    /**
     * Initializes user preferences like weight on startup.
     */
    fun initializePreferences(context: Context) {
        val savedWeight = com.wheredigo.hikingtracker.data.HikingPreferences.getUserWeightLbs(context)
        HikingRepository.setUserWeightLbs(savedWeight)
    }

    /**
     * Fetches the user's initial GPS location on app launch to center the map on their location.
     */
    @android.annotation.SuppressLint("MissingPermission")
    fun fetchCurrentLocation(context: Context) {
        val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                HikingRepository.onLocationUpdate(location)
                viewModelScope.launch {
                    _recenterCameraEvents.emit(com.wheredigo.hikingtracker.utils.LocationUtils.toMapboxPoint(location))
                }
            } else {
                val tokenSource = com.google.android.gms.tasks.CancellationTokenSource()
                fusedClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    tokenSource.token
                ).addOnSuccessListener { currentLocation ->
                    currentLocation?.let {
                        HikingRepository.onLocationUpdate(it)
                        viewModelScope.launch {
                            _recenterCameraEvents.emit(com.wheredigo.hikingtracker.utils.LocationUtils.toMapboxPoint(it))
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the user's weight in pounds and persists it.
     */
    fun updateUserWeight(weightLbs: Double, context: Context) {
        com.wheredigo.hikingtracker.data.HikingPreferences.setUserWeightLbs(context, weightLbs)
        HikingRepository.setUserWeightLbs(weightLbs)
    }

    /**
     * Starts tracking by launching the HikingService foreground service.
     */
    fun startHike(context: Context) {
        HikingService.startService(context)
    }

    /**
     * Stops the tracking service.
     */
    fun stopHike(context: Context) {
        HikingService.stopService(context)
    }

    /**
     * Toggles between starting and stopping the hike session.
     */
    fun toggleTracking(context: Context) {
        if (uiState.value.isTracking) {
            stopHike(context)
        } else {
            startHike(context)
        }
    }

    /**
     * Requests the map camera to smoothly recenter on the latest recorded user coordinate.
     */
    fun requestRecenter() {
        uiState.value.latestPoint?.let { point ->
            viewModelScope.launch {
                _recenterCameraEvents.emit(point)
            }
        }
    }
}
