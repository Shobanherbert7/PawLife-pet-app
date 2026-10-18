package com.pawlife.app.core.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pawlife.app.feature.pets.worker.MedicationReminderWorker
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {

    fun scheduleMedicationReminder(petName: String, medicationName: String, delaySeconds: Long) {
        val data = Data.Builder()
            .putString("PET_NAME", petName)
            .putString("MEDICATION_NAME", medicationName)
            .build()

        val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
