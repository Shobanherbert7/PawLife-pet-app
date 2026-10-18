package com.pawlife.app.core.data.repository

import com.pawlife.app.core.data.db.dao.ActivityDao
import com.pawlife.app.core.data.db.entity.ActivityLogEntity
import com.pawlife.app.core.data.network.PawLifeBackendApi
import com.pawlife.app.core.data.network.WalkRequestDto
import com.pawlife.app.core.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val pawlifeBackendApi: PawLifeBackendApi
) : ActivityRepository {
    override fun getActivityLogs(petId: Long): Flow<List<ActivityLogEntity>> {
        return activityDao.getActivityLogs(petId)
    }

    override suspend fun getActivityLogById(activityId: Long): ActivityLogEntity? {
        return activityDao.getActivityLogById(activityId)
    }

    override suspend fun addActivityLog(log: ActivityLogEntity): Long {
        val id = activityDao.insertActivityLog(log)

        // Synchronize to Dedicated pawlife-server via Retrofit
        try {
            pawlifeBackendApi.logWalk(
                WalkRequestDto(
                    petId = log.petId,
                    durationSeconds = (log.durationMinutes ?: 0).toLong() * 60L,
                    distanceMeters = (log.distanceMeters ?: 0f).toDouble(),
                    caloriesBurned = log.caloriesBurned ?: 0,
                    routePointsJson = log.routeGeoJson
                )
            )
        } catch (e: Exception) {
            android.util.Log.w("ActivityRepo", "PawLife server walk sync notice: ${e.message}")
        }

        return id
    }

    override suspend fun updateActivityLog(log: ActivityLogEntity) {
        activityDao.updateActivityLog(log)
    }
}
