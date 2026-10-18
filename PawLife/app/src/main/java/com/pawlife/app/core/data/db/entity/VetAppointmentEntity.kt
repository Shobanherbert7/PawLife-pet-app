package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vet_appointments",
    foreignKeys = [ForeignKey(
        entity = PetEntity::class, parentColumns = ["id"],
        childColumns = ["petId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class VetAppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val vetName: String,
    val clinicName: String,
    val clinicAddress: String?,
    val clinicPhone: String?,
    val appointmentTime: Long,
    val purpose: String,
    val calendarEventId: String?,
    val reminderMinutesBefore: Int = 60,
    val status: String = "scheduled", // "scheduled", "completed", "cancelled"
    val notes: String?
)
