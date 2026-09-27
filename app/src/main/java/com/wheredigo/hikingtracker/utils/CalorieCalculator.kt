package com.wheredigo.hikingtracker.utils

import kotlin.math.max
import kotlin.math.min

/**
 * Utility for calculating realistic caloric expenditure during hiking sessions.
 *
 * Uses Metabolic Equivalent of Task (MET) principles adapted from the Compendium of
 * Physical Activities and the Minetti/Pandolf equations for uphill trail walking.
 */
object CalorieCalculator {

    /** Default user weight in pounds (154 lbs ≈ 70 kg) */
    const val DEFAULT_WEIGHT_LBS = 154.0

    /** Default user weight in kilograms (70 kg ≈ 154 lbs) */
    const val DEFAULT_WEIGHT_KG = 70.0

    /** Conversion factor: 1 lb ≈ 0.45359237 kg */
    const val LBS_TO_KG_FACTOR = 0.45359237

    /**
     * Converts weight in pounds (lbs) to kilograms (kg).
     */
    fun lbsToKg(weightLbs: Double): Double = weightLbs * LBS_TO_KG_FACTOR

    /**
     * Converts weight in kilograms (kg) to pounds (lbs).
     */
    fun kgToLbs(weightKg: Double): Double = weightKg / LBS_TO_KG_FACTOR

    /**
     * Calculates the effective MET (Metabolic Equivalent of Task) for hiking based on
     * speed and terrain grade (slope percentage).
     *
     * @param speedKmh Current hiking speed in km/h.
     * @param gradePercentage Terrain slope in percent ((vertical change / horizontal distance) * 100).
     *                        Positive for uphill, negative for downhill.
     * @return Effective MET value (ranging from ~1.3 for resting to ~14.0 for steep mountain ascent).
     */
    fun calculateMET(
        speedKmh: Double,
        gradePercentage: Double = 0.0
    ): Double {
        // If stationary or moving very slowly (< 0.5 km/h), user is resting/standing
        if (speedKmh < 0.5) {
            return 1.3
        }

        // Base walking MET at 4.0 km/h (2.5 mph) on level ground is 3.5 MET
        // (Compendium of Physical Activities standard: 3.5 MET for level moderate walking)
        var met = 3.5

        // Speed adjustment relative to 4.0 km/h baseline
        if (speedKmh > 4.0) {
            val speedDiff = speedKmh - 4.0
            met += speedDiff * 0.8
        } else {
            val speedDiff = 4.0 - speedKmh
            met -= speedDiff * 0.4
        }

        // Slope / Incline / Decline adjustment
        if (gradePercentage > 0.0) {
            // Uphill: Each +1% uphill grade adds ~0.25 MET (e.g. +10% grade adds +2.5 MET)
            val uphillBonus = min(gradePercentage * 0.25, 8.0)
            met += uphillBonus
        } else if (gradePercentage < 0.0) {
            // Downhill: Moderate downhill (-5% to -15%) reduces metabolic cost by 20–35%
            // Steep downhill requires braking/stabilization, bottoming out around 2.2–2.5 MET
            val downhillReduction = max(gradePercentage * 0.12, -1.3)
            met += downhillReduction // gradePercentage is negative, so this subtracts
        }

        // Clamp MET between resting baseline (1.3) and intense mountain climbing (14.0)
        return met.coerceIn(1.3, 14.0)
    }

    /**
     * Calculates calories (kcal) burned over an elapsed time slice.
     *
     * Formula: Calories = MET * Weight(kg) * Time(hours)
     *
     * @param met The calculated MET value.
     * @param durationSeconds Time interval in seconds.
     * @param weightKg User body weight in kg (defaults to 70.0 kg).
     * @return Calories burned in kcal during the interval.
     */
    fun calculateCaloriesForInterval(
        met: Double,
        durationSeconds: Long,
        weightKg: Double = DEFAULT_WEIGHT_KG
    ): Double {
        if (durationSeconds <= 0) return 0.0
        val hours = durationSeconds / 3600.0
        return met * weightKg * hours
    }
}
