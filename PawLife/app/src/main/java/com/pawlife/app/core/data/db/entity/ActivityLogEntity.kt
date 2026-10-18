package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [ForeignKey(
        entity = PetEntity::class, parentColumns = ["id"],
        childColumns = ["petId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val type: String,             // "walk", "play", "swim", "training"
    val startTime: Long,
    val endTime: Long?,
    val durationMinutes: Int?,
    val distanceMeters: Float?,
    val caloriesBurned: Int?,
    val routeGeoJson: String?,    // GeoJSON LineString of GPS track
    val mapSnapshotUri: String?,
    val notes: String?
)
