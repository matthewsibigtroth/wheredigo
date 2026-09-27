package com.wheredigo.hikingtracker.data

import android.content.Context
import android.content.SharedPreferences
import com.wheredigo.hikingtracker.utils.CalorieCalculator

/**
 * Manages persistent user preferences such as body weight.
 */
object HikingPreferences {

    private const val PREFS_NAME = "wheredigo_hiking_prefs"
    private const val KEY_USER_WEIGHT_LBS = "user_weight_lbs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Gets the saved user weight in pounds (lbs), defaulting to 154.0 lbs.
     */
    fun getUserWeightLbs(context: Context): Double {
        val prefs = getPrefs(context)
        return prefs.getFloat(KEY_USER_WEIGHT_LBS, CalorieCalculator.DEFAULT_WEIGHT_LBS.toFloat()).toDouble()
    }

    /**
     * Saves user weight in pounds (lbs).
     */
    fun setUserWeightLbs(context: Context, weightLbs: Double) {
        val prefs = getPrefs(context)
        prefs.edit().putFloat(KEY_USER_WEIGHT_LBS, weightLbs.toFloat()).apply()
    }
}
