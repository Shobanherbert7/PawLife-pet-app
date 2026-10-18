package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.FeedingLogEntity
import com.pawlife.app.core.data.db.entity.FeedingScheduleEntity
import com.pawlife.app.core.data.network.ProductData
import kotlinx.coroutines.flow.Flow

interface FeedingRepository {
    fun getFeedingSchedules(petId: Long): Flow<List<FeedingScheduleEntity>>
    suspend fun addFeedingSchedule(schedule: FeedingScheduleEntity)
    fun getFeedingLogs(petId: Long): Flow<List<FeedingLogEntity>>
    suspend fun addFeedingLog(log: FeedingLogEntity)
    suspend fun getFoodInfoFromBarcode(barcode: String): Result<ProductData>
}
