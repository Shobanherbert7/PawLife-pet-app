package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feeding_schedules",
    foreignKeys = [ForeignKey(
        entity = PetEntity::class, parentColumns = ["id"],
        childColumns = ["petId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class FeedingScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val mealName: String,         // "Breakfast", "Dinner"
    val timeMinutes: Int,         // minutes from midnight
    val foodName: String,
    val portionGrams: Float,
    val calories: Int?,
    val barcode: String?,         // from scanner feature
    val isActive: Boolean = true
)
