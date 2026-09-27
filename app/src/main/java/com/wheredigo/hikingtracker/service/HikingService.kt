package com.wheredigo.hikingtracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.wheredigo.hikingtracker.MainActivity
import com.wheredigo.hikingtracker.R
import com.wheredigo.hikingtracker.data.HikingRepository
import com.wheredigo.hikingtracker.utils.Formatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service for continuous, high-accuracy GPS tracking during hikes.
 * Persists tracking when the screen is locked and displays live metrics in the notification tray.
 */
class HikingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    companion object {
        const val ACTION_START = "com.wheredigo.hikingtracker.ACTION_START"
        const val ACTION_STOP = "com.wheredigo.hikingtracker.ACTION_STOP"

        const val CHANNEL_ID = "hiking_tracking_channel"
        const val NOTIFICATION_ID = 1001

        /**
         * Starts the hiking foreground service.
         */
        fun startService(context: Context) {
            val intent = Intent(context, HikingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stops the hiking foreground service.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, HikingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        acquireWakeLock()
        val userWeight = com.wheredigo.hikingtracker.data.HikingPreferences.getUserWeightLbs(this)
        HikingRepository.startSession(userWeight)

        // Start Foreground Service with Location Service Type for Android 14+ compatibility
        val initialNotification = buildNotification("Starting hike tracking...", "00:00:00")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        requestLocationUpdates()
        startTimerAndNotificationUpdater()
    }

    private fun stopTracking() {
        HikingRepository.stopSession()
        stopLocationUpdates()
        timerJob?.cancel()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        // High-accuracy location updates every 3–5 seconds
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(2.0f)
            .setMaxUpdateDelayMillis(5000L)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopTracking()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    HikingRepository.onLocationUpdate(location)
                }
            }
        }
    }

    private fun startTimerAndNotificationUpdater() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                HikingRepository.tickTimer()

                // Update notification text with current metrics
                val state = HikingRepository.sessionState.value
                val metrics = state.metrics
                val formattedTime = Formatters.formatDuration(metrics.durationSeconds)
                val formattedDist = Formatters.formatDistance(metrics.distanceMeters)
                val formattedCal = Formatters.formatCalories(metrics.caloriesBurned)
                val formattedAlt = Formatters.formatAltitudeCompact(metrics.currentAltitudeMeters)

                val contentText = "$formattedDist • $formattedCal • $formattedAlt"
                val updatedNotification = buildNotification(contentText, formattedTime)
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)
            }
        }
    }

    private fun buildNotification(contentText: String, timeString: String): Notification {
        // Intent to open MainActivity when user taps the notification
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action intent to Stop Hike directly from notification
        val stopIntent = Intent(this, HikingService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hike in Progress: $timeString")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_hiking_notification)
            .setContentIntent(pendingContentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.notification_action_stop),
                pendingStopIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "WhereDiGo::HikingTrackerWakeLock"
            ).apply {
                acquire()
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
        releaseWakeLock()
    }
}
