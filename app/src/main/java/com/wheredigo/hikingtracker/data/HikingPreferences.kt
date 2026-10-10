package com.wheredigo.hikingtracker.data

import android.content.Context
import android.content.SharedPreferences
import com.mapbox.geojson.Point
import com.wheredigo.hikingtracker.utils.CalorieCalculator
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Manages persistent user preferences such as body weight and saved hikes history.
 */
object HikingPreferences {

    private const val PREFS_NAME = "wheredigo_hiking_prefs"
    private const val KEY_USER_WEIGHT_LBS = "user_weight_lbs"
    private const val SAVED_HIKES_FILE = "saved_hikes.json"

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

    /**
     * Loads all saved hikes from local app storage (newest first).
     */
    fun getSavedHikes(context: Context): List<SavedHike> {
        val file = File(context.filesDir, SAVED_HIKES_FILE)
        if (!file.exists()) return emptyList()

        return try {
            val jsonText = file.readText()
            val array = JSONArray(jsonText)
            val result = mutableListOf<SavedHike>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val metricsObj = obj.optJSONObject("metrics") ?: JSONObject()
                val pointsArray = obj.optJSONArray("pathPoints") ?: JSONArray()
                val points = mutableListOf<Point>()
                for (j in 0 until pointsArray.length()) {
                    val pt = pointsArray.getJSONObject(j)
                    points.add(Point.fromLngLat(pt.getDouble("lng"), pt.getDouble("lat")))
                }
                result.add(
                    SavedHike(
                        id = obj.getString("id"),
                        timestampMillis = obj.getLong("timestampMillis"),
                        metrics = HikingMetrics(
                            durationSeconds = metricsObj.optLong("durationSeconds", 0L),
                            distanceMeters = metricsObj.optDouble("distanceMeters", 0.0),
                            currentAltitudeMeters = metricsObj.optDouble("currentAltitudeMeters", 0.0),
                            elevationGainMeters = metricsObj.optDouble("elevationGainMeters", 0.0),
                            caloriesBurned = metricsObj.optDouble("caloriesBurned", 0.0),
                            currentSpeedKmh = metricsObj.optDouble("currentSpeedKmh", 0.0),
                            currentGradePercentage = metricsObj.optDouble("currentGradePercentage", 0.0)
                        ),
                        pathPoints = points
                    )
                )
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Appends a newly completed hike to the top of the saved hikes list and writes to local storage.
     */
    fun saveHike(context: Context, hike: SavedHike): List<SavedHike> {
        val updatedList = listOf(hike) + getSavedHikes(context)
        writeSavedHikes(context, updatedList)
        return updatedList
    }

    private fun writeSavedHikes(context: Context, hikes: List<SavedHike>) {
        try {
            val array = JSONArray()
            for (hike in hikes) {
                val metricsObj = JSONObject().apply {
                    put("durationSeconds", hike.metrics.durationSeconds)
                    put("distanceMeters", hike.metrics.distanceMeters)
                    put("currentAltitudeMeters", hike.metrics.currentAltitudeMeters)
                    put("elevationGainMeters", hike.metrics.elevationGainMeters)
                    put("caloriesBurned", hike.metrics.caloriesBurned)
                    put("currentSpeedKmh", hike.metrics.currentSpeedKmh)
                    put("currentGradePercentage", hike.metrics.currentGradePercentage)
                }
                val pointsArray = JSONArray()
                for (pt in hike.pathPoints) {
                    pointsArray.put(
                        JSONObject().apply {
                            put("lng", pt.longitude())
                            put("lat", pt.latitude())
                        }
                    )
                }
                val obj = JSONObject().apply {
                    put("id", hike.id)
                    put("timestampMillis", hike.timestampMillis)
                    put("metrics", metricsObj)
                    put("pathPoints", pointsArray)
                }
                array.put(obj)
            }
            File(context.filesDir, SAVED_HIKES_FILE).writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
