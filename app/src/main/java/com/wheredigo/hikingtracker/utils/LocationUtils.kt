package com.wheredigo.hikingtracker.utils

import android.location.Location
import com.mapbox.geojson.Point

/**
 * Helper methods for GPS distance calculation, noise filtering, and GeoJSON conversions.
 */
object LocationUtils {

    /** Minimum GPS accuracy threshold in meters. Updates worse than this are filtered out */
    private const val ACCURACY_THRESHOLD_METERS = 30.0f

    /** Minimum distance change in meters required to record a new coordinate point */
    private const val MIN_DISTANCE_DELTA_METERS = 1.5

    /** Minimum altitude delta in meters to register elevation gain (filters barometric noise) */
    private const val MIN_ALTITUDE_DELTA_METERS = 1.0

    /**
     * Determines whether a new GPS location is accurate and significant enough to be processed.
     */
    fun isValidLocation(newLocation: Location): Boolean {
        if (!newLocation.hasAccuracy() || newLocation.accuracy > ACCURACY_THRESHOLD_METERS) {
            return false
        }
        return true
    }

    /**
     * Converts an Android [Location] to a Mapbox [Point] (Longitude, Latitude, Altitude).
     */
    fun toMapboxPoint(location: Location): Point {
        return Point.fromLngLat(
            location.longitude,
            location.latitude,
            if (location.hasAltitude()) location.altitude else 0.0
        )
    }

    /**
     * Calculates distance in meters between two [Location] instances.
     */
    fun calculateDistance(loc1: Location, loc2: Location): Double {
        return loc1.distanceTo(loc2).toDouble()
    }

    /**
     * Checks if the distance between two locations is greater than the noise threshold.
     */
    fun isSignificantMove(loc1: Location, loc2: Location): Boolean {
        return loc1.distanceTo(loc2) >= MIN_DISTANCE_DELTA_METERS
    }

    /**
     * Calculates positive elevation gain between previous and current altitude readings.
     */
    fun calculateElevationGain(prevAltitude: Double, currentAltitude: Double): Double {
        val diff = currentAltitude - prevAltitude
        return if (diff >= MIN_ALTITUDE_DELTA_METERS) diff else 0.0
    }

    /**
     * Calculates signed vertical elevation change in meters (positive for ascent, negative for descent).
     */
    fun calculateElevationChange(prevAltitude: Double, currentAltitude: Double): Double {
        return currentAltitude - prevAltitude
    }

    /**
     * Calculates terrain grade percentage based on vertical change and horizontal distance.
     * Clamped between -50.0% and +50.0% to prevent GPS jitter spikes.
     */
    fun calculateGradePercentage(elevationChangeMeters: Double, horizontalDistanceMeters: Double): Double {
        if (horizontalDistanceMeters < 2.0) return 0.0
        val rawGrade = (elevationChangeMeters / horizontalDistanceMeters) * 100.0
        return rawGrade.coerceIn(-50.0, 50.0)
    }
}
