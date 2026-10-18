package com.pawlife.app.feature.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawlife.app.core.data.db.dao.FeedingDao
import com.pawlife.app.core.data.db.dao.PetDao
import com.pawlife.app.core.domain.repository.AuthRepository
import com.pawlife.app.core.util.PawLifeNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class FeedingReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val feedingDao: FeedingDao,
    private val petDao: PetDao,
    private val authRepository: AuthRepository,
    private val notificationManager: PawLifeNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val petId = inputData.getLong("pet_id", -1L)
        val scheduleId = inputData.getLong("schedule_id", -1L)
        if (petId == -1L || scheduleId == -1L) return Result.failure()

        val user = authRepository.currentUser.firstOrNull()
        val ownerId = user?.uid ?: "anonymous"

        val pet = petDao.getPetById(petId, ownerId).firstOrNull() ?: return Result.failure()
        
        // Find the specific schedule
        val schedules = feedingDao.getFeedingSchedules(petId).firstOrNull() ?: return Result.failure()
        val schedule = schedules.find { it.id == scheduleId } ?: return Result.failure()

        notificationManager.showFeedingReminder(
            petName = pet.name,
            mealName = schedule.mealName,
            foodName = schedule.foodName,
            portionGrams = schedule.portionGrams
        )

        return Result.success()
    }
}
