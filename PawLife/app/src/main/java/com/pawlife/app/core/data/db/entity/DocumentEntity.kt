package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    foreignKeys = [ForeignKey(
        entity = PetEntity::class, parentColumns = ["id"],
        childColumns = ["petId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val title: String,
    val type: String,             // "vaccine_cert", "adoption_paper", "insurance", "other"
    val encryptedFilePath: String,
    val mimeType: String,
    val fileSize: Long,
    val uploadedAt: Long,
    val expiryDate: Long?
)
