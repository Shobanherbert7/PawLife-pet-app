package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_records",
    foreignKeys = [ForeignKey(
        entity = PetEntity::class, parentColumns = ["id"],
        childColumns = ["petId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class WeightRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val weightKg: Float,
    val recordedAt: Long,
    val notes: String?
)
