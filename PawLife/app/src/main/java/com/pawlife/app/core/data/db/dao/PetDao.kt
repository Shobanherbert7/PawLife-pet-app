package com.pawlife.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.pawlife.app.core.data.db.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE isActive = 1 ORDER BY name")
    fun getAllPetsAnyOwner(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE isActive = 1 AND (ownerId = :ownerId OR ownerId = 'anonymous' OR ownerId = 'local_owner' OR ownerId = '') ORDER BY name")
    fun getAllPets(ownerId: String): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id AND (ownerId = :ownerId OR ownerId = 'anonymous' OR ownerId = 'local_owner' OR ownerId = '')")
    fun getPetById(id: Long, ownerId: String): Flow<PetEntity?>

    @Query("UPDATE pets SET ownerId = :newOwnerId WHERE ownerId = 'anonymous' OR ownerId = 'local_owner' OR ownerId = ''")
    suspend fun claimAnonymousPets(newOwnerId: String)

    @Upsert
    suspend fun upsertPet(pet: PetEntity): Long

    @Delete
    suspend fun deletePet(pet: PetEntity)
}
