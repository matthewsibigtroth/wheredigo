package com.wheredigo.hikingtracker

import android.app.Application
import com.mapbox.common.MapboxOptions

/**
 * Application class for WhereDiGo hiking tracker.
 * Initializes the Mapbox Maps SDK with the public access token on startup.
 */
class HikingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure Mapbox Public Access Token
        val mapboxToken = getString(R.string.mapbox_access_token)
        if (mapboxToken.isNotEmpty()) {
            MapboxOptions.accessToken = mapboxToken
        }
    }
}
