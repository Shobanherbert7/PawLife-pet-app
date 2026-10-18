package com.pawlife.app.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pawlife.app.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BleWearableManager {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _stepCount = MutableStateFlow(1420)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _heartRateBpm = MutableStateFlow(84)
    val heartRateBpm: StateFlow<Int> = _heartRateBpm.asStateFlow()

    private val _batteryLevel = MutableStateFlow(92)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    fun updateConnectionStatus(connected: Boolean) {
        _isConnected.value = connected
    }

    fun updateSteps(steps: Int) {
        _stepCount.value = steps
    }

    fun updateHeartRate(bpm: Int) {
        _heartRateBpm.value = bpm
    }

    fun updateBattery(level: Int) {
        _batteryLevel.value = level
    }
}

class BleWearableService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            startForegroundService()
            simulateBleConnection()
        } else if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "ble_wearable_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Wearable Sync", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PawLife Smart Collar")
            .setContentText("Syncing BLE telemetry & vitals...")
            .setSmallIcon(R.drawable.ic_paw_logo) // Using existing paw logo
            .setOngoing(true)
            .build()

        startForeground(2, notification)
    }

    private fun simulateBleConnection() {
        serviceScope.launch(Dispatchers.IO) {
            // Simulate connecting to the collar
            delay(1500)
            BleWearableManager.updateConnectionStatus(true)
            
            var currentSteps = BleWearableManager.stepCount.value
            var currentBpm = BleWearableManager.heartRateBpm.value
            var battery = BleWearableManager.batteryLevel.value
            var ticks = 0

            while (isActive) {
                delay(2500) // update telemetry every 2.5s
                ticks++
                currentSteps += (3..10).random()
                
                // Realistic pet heart rate variation (78 - 115 bpm)
                val deltaBpm = (-3..4).random()
                currentBpm = (currentBpm + deltaBpm).coerceIn(76, 120)

                if (ticks % 80 == 0 && battery > 10) {
                    battery -= 1
                }

                BleWearableManager.updateSteps(currentSteps)
                BleWearableManager.updateHeartRate(currentBpm)
                BleWearableManager.updateBattery(battery)

                // Sync live telemetry to Firestore
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val collarData = hashMapOf(
                        "petId" to 1L,
                        "deviceName" to "PawLife Collar BLE-89X",
                        "isConnected" to true,
                        "heartRateBpm" to currentBpm,
                        "stepCount" to currentSteps,
                        "batteryLevel" to battery,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    db.collection("collar_telemetry").document("current").set(collarData)
                } catch (e: Exception) {
                    // Graceful offline ignore
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        BleWearableManager.updateConnectionStatus(false)
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("collar_telemetry").document("current").set(
                hashMapOf(
                    "petId" to 1L,
                    "deviceName" to "PawLife Collar BLE-89X",
                    "isConnected" to false,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
