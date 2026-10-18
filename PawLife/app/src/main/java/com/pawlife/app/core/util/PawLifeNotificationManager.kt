package com.pawlife.app.core.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PawLifeNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Health Reminders"
            val descriptionText = "Notifications for medication and feeding schedules"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("HEALTH_REMINDERS", name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMedicationReminder(petName: String, medicationName: String, dosage: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return // Handle permission request gracefully in UI instead
        }

        val builder = NotificationCompat.Builder(context, "HEALTH_REMINDERS")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medication Due: $petName")
            .setContentText("It's time to give $petName their $medicationName ($dosage).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    fun showFeedingReminder(petName: String, mealName: String, foodName: String, portionGrams: Float) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val builder = NotificationCompat.Builder(context, "HEALTH_REMINDERS")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Feeding Time: $petName")
            .setContentText("It's time for $petName's $mealName ($portionGrams g of $foodName).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
