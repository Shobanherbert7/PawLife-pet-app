package com.pawlife.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY recordedAt ASC")
    fun getWeightHistory(petId: Long): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestWeight(petId: Long): WeightRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(record: WeightRecordEntity)
}
