package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getWeightHistory(petId: Long): Flow<List<WeightRecordEntity>>
    suspend fun getLatestWeight(petId: Long): WeightRecordEntity?
    suspend fun addWeightRecord(record: WeightRecordEntity)
}
