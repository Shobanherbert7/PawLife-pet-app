package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String = "",
    val name: String,
    val species: String,          // "dog", "cat", "bird"…
    val breed: String,
    val dateOfBirth: Long,        // epoch millis
    val gender: String,
    val weight: Float,            // kg, latest snapshot
    val photoUri: String?,
    val microchipId: String?,
    val adoptionDate: Long?,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
