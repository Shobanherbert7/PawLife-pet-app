package com.pawlife.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pawlife.app.core.data.db.entity.FeedingLogEntity
import com.pawlife.app.core.data.db.entity.FeedingScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feeding_schedules WHERE petId = :petId AND isActive = 1 ORDER BY timeMinutes ASC")
    fun getFeedingSchedules(petId: Long): Flow<List<FeedingScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedingSchedule(schedule: FeedingScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedingLog(log: FeedingLogEntity)

    @Query("SELECT * FROM feeding_logs WHERE petId = :petId ORDER BY fedAt DESC")
    fun getFeedingLogs(petId: Long): Flow<List<FeedingLogEntity>>
}
