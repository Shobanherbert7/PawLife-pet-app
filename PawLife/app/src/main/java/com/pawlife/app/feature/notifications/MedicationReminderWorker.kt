package com.pawlife.app.feature.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawlife.app.core.util.PawLifeNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: PawLifeNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val petName = inputData.getString("pet_name") ?: "Your Pet"
        val medicationName = inputData.getString("medication_name") ?: "Medication"
        val dosage = inputData.getString("dosage") ?: "1 dose"

        // In a real scenario, we might fetch updated info from a repository here
        // val medication = repository.getMedicationById(id)
        
        notificationManager.showMedicationReminder(
            petName = petName,
            medicationName = medicationName,
            dosage = dosage
        )

        Result.success()
    }
}
