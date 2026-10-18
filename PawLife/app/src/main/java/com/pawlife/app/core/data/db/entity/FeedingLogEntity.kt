package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feeding_logs",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class, parentColumns = ["id"],
            childColumns = ["petId"], onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FeedingScheduleEntity::class, parentColumns = ["id"],
            childColumns = ["scheduleId"], onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("scheduleId"), Index("petId")]
)
data class FeedingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val scheduleId: Long?,
    val foodName: String,
    val portionGrams: Float,
    val calories: Int?,
    val fedAt: Long,
    val notes: String?
)
