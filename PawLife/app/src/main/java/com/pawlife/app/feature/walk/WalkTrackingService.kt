package com.pawlife.app.feature.walk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object WalkTracker {
    private val _routePoints = MutableStateFlow<List<Location>>(emptyList())
    val routePoints: StateFlow<List<Location>> = _routePoints.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _startTime = MutableStateFlow<Long?>(null)
    val startTime: StateFlow<Long?> = _startTime.asStateFlow()

    fun addPoint(location: Location) {
        _routePoints.update { it + location }
    }

    fun startTracking() {
        _routePoints.value = emptyList()
        _startTime.value = System.currentTimeMillis()
        _isTracking.value = true
    }

    fun stopTracking() {
        _isTracking.value = false
        _startTime.value = null
    }
}

class WalkTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    WalkTracker.addPoint(location)

                    // Stream live active walk & collar telemetry to Firestore
                    try {
                        val points = WalkTracker.routePoints.value
                        var dist = 0f
                        for (i in 0 until points.size - 1) {
                            dist += points[i].distanceTo(points[i + 1])
                        }
                        val startTime = WalkTracker.startTime.value ?: System.currentTimeMillis()
                        val durSec = (System.currentTimeMillis() - startTime) / 1000L

                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val walkData = hashMapOf(
                            "petId" to 1L,
                            "isActive" to true,
                            "startTime" to startTime,
                            "durationSeconds" to durSec,
                            "distanceMeters" to dist,
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "routePoints" to points.takeLast(50).map { "${it.latitude},${it.longitude}" },
                            "heartRateBpm" to com.pawlife.app.data.service.BleWearableManager.heartRateBpm.value,
                            "stepCount" to com.pawlife.app.data.service.BleWearableManager.stepCount.value,
                            "batteryLevel" to com.pawlife.app.data.service.BleWearableManager.batteryLevel.value,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        db.collection("active_walk").document("current").set(walkData)

                        // Geofence Safe Zone Check (Home: 13.0827, 80.2707, Radius: 150m)
                        val homeLoc = Location("").apply {
                            latitude = 13.0827
                            longitude = 80.2707
                        }
                        val distFromHome = location.distanceTo(homeLoc)
                        val isBreached = distFromHome > 150f

                        val geofenceData = hashMapOf(
                            "petId" to 1L,
                            "isBreached" to isBreached,
                            "distanceFromHome" to distFromHome,
                            "safeRadiusMeters" to 150f,
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        db.collection("geofence_status").document("current").set(geofenceData)

                        // Update Live Lockscreen Notification HUD
                        val currentBpm = com.pawlife.app.data.service.BleWearableManager.heartRateBpm.value
                        val currentSteps = com.pawlife.app.data.service.BleWearableManager.stepCount.value
                        updateLiveNotification(currentBpm, currentSteps, dist, durSec, isBreached, distFromHome)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            startForegroundService()
            startLocationUpdates()
            // Automatically launch BLE Collar service during walk
            try {
                val bleIntent = Intent(this, com.pawlife.app.data.service.BleWearableService::class.java).apply {
                    action = com.pawlife.app.data.service.BleWearableService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(bleIntent)
                } else {
                    startService(bleIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (intent?.action == ACTION_STOP) {
            stopLocationUpdates()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        createNotificationChannels()
        val notification = buildWalkNotification(84, 1420, 0f, 0L, false, 35f)
        startForeground(1, notification)
        WalkTracker.startTracking()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val walkChannel = NotificationChannel(
                CHANNEL_ID_WALK,
                "Live Walk Telemetry",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live collar heart rate and walk distance on lockscreen"
            }
            val breachChannel = NotificationChannel(
                CHANNEL_ID_BREACH,
                "Safe Zone Escape Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarms when pet exits safe perimeter"
                enableVibration(true)
            }
            manager.createNotificationChannel(walkChannel)
            manager.createNotificationChannel(breachChannel)
        }
    }

    private fun buildWalkNotification(
        bpm: Int,
        steps: Int,
        distMeters: Float,
        durSec: Long,
        isBreached: Boolean,
        distFromHome: Float
    ): android.app.Notification {
        val channelId = if (isBreached) CHANNEL_ID_BREACH else CHANNEL_ID_WALK

        val stopIntent = Intent(this, WalkTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this,
            101,
            stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = android.app.PendingIntent.getActivity(
            this,
            102,
            launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val distKmStr = String.format("%.2f", distMeters / 1000f)
        val mins = durSec / 60
        val secs = durSec % 60
        val timeStr = String.format("%02d:%02d", mins, secs)

        val title = if (isBreached) {
            "🚨 ESCAPE ALARM: Max is ${distFromHome.toInt()}m from Home!"
        } else {
            "🐾 Live Walk: $distKmStr km ($timeStr)"
        }

        val content = "♥ $bpm BPM • $steps collar steps • ${if (isBreached) "PERIMETER BREACH!" else "Safe Zone Active"}"

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop Walk", stopPendingIntent)
            .setPriority(if (isBreached) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW)
            .setCategory(if (isBreached) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_WORKOUT)
            .build()
    }

    private fun updateLiveNotification(
        bpm: Int,
        steps: Int,
        distMeters: Float,
        durSec: Long,
        isBreached: Boolean,
        distFromHome: Float
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = buildWalkNotification(bpm, steps, distMeters, durSec, isBreached, distFromHome)
        manager.notify(1, notification)
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        val points = WalkTracker.routePoints.value
        val startTime = WalkTracker.startTime.value ?: System.currentTimeMillis()
        val endTime = System.currentTimeMillis()
        
        var distance = 0f
        for (i in 0 until points.size - 1) {
            distance += points[i].distanceTo(points[i+1])
        }
        
        val record = com.pawlife.app.core.data.db.entity.WalkRecordEntity(
            petId = 1L, // Demo hardcoded
            startTime = startTime,
            endTime = endTime,
            distanceMeters = distance,
            routePoints = points.map { "${it.latitude},${it.longitude}" }
        )
        
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection("walk_history").document()
            db.collection("walk_history").document(docRef.id).set(record.copy(id = docRef.id))

            val stopData = hashMapOf(
                "petId" to 1L,
                "isActive" to false,
                "endTime" to endTime,
                "distanceMeters" to distance,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("active_walk").document("current").set(stopData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        WalkTracker.stopTracking()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val CHANNEL_ID_WALK = "walk_tracking_channel"
        const val CHANNEL_ID_BREACH = "geofence_siren_channel"
    }
}
