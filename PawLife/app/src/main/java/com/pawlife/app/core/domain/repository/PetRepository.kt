package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.PetEntity
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getAllPets(): Flow<List<PetEntity>>
    fun getPetById(id: Long): Flow<PetEntity?>
    suspend fun claimAnonymousPets(newOwnerId: String)
    suspend fun hasAnyPets(): Boolean
    suspend fun upsertPet(pet: PetEntity): Long
    suspend fun deletePet(pet: PetEntity)
}
