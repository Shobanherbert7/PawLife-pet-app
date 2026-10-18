package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivityLogs(petId: Long): Flow<List<ActivityLogEntity>>
    suspend fun getActivityLogById(activityId: Long): ActivityLogEntity?
    suspend fun addActivityLog(log: ActivityLogEntity): Long
    suspend fun updateActivityLog(log: ActivityLogEntity)
}
