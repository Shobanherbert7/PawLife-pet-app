package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [ForeignKey(
        entity = PetEntity::class, parentColumns = ["id"],
        childColumns = ["petId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val name: String,
    val dosage: String,
    val frequency: String,        // "daily", "weekly", cron-like string
    val startDate: Long,
    val endDate: Long?,
    val reminderEnabled: Boolean = true,
    val reminderTime: Int,        // minutes from midnight
    val notes: String?,
    val isActive: Boolean = true
)
