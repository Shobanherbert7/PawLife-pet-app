package com.pawlife.app.feature.pets.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawlife.app.core.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val petName = inputData.getString("PET_NAME") ?: "your pet"
            val medicationName = inputData.getString("MEDICATION_NAME") ?: "medication"

            val notificationHelper = NotificationHelper(context)
            notificationHelper.showNotification(
                title = "Medication Reminder 🐾",
                message = "It's time for $petName's $medicationName!",
                notificationId = System.currentTimeMillis().toInt()
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
