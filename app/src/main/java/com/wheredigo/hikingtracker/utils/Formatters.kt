package com.wheredigo.hikingtracker.utils

import java.util.Locale

/**
 * Utility functions for clean, user-friendly formatting of hiking metrics.
 */
object Formatters {

    /**
     * Formats duration in seconds to standard HH:MM:SS format.
     */
    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Formats distance in meters to miles with 2 decimal places.
     */
    fun formatDistance(meters: Double): String {
        val miles = meters / 1609.344
        return String.format(Locale.US, "%.2f mi", miles)
    }

    /**
     * Formats altitude in feet.
     * e.g., "4,659 ft"
     */
    fun formatAltitude(meters: Double): String {
        val feet = meters * 3.28084
        return String.format(Locale.US, "%,d ft", feet.toInt())
    }

    /**
     * Compact altitude string for cards / notifications in feet.
     */
    fun formatAltitudeCompact(meters: Double): String {
        val feet = meters * 3.28084
        return String.format(Locale.US, "%d ft", feet.toInt())
    }

    /**
     * Formats elevation gain in meters to feet.
     */
    fun formatElevationGain(meters: Double): String {
        val feet = meters * 3.28084
        return String.format(Locale.US, "+%d ft", feet.toInt())
    }

    /**
     * Formats calories burned into rounded integer cal.
     */
    fun formatCalories(kcal: Double): String {
        return String.format(Locale.US, "%d cal", kcal.toInt())
    }

    /**
     * Formats current speed in mph (converted from km/h).
     */
    fun formatSpeed(kmh: Double): String {
        val mph = kmh * 0.621371
        return String.format(Locale.US, "%.1f mph", mph)
    }
}
