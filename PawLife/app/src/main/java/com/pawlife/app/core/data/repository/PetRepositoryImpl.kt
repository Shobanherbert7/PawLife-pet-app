package com.pawlife.app.core.data.repository

import com.pawlife.app.core.data.db.dao.PetDao
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.domain.repository.AuthRepository
import com.pawlife.app.core.domain.repository.PetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class PetRepositoryImpl @Inject constructor(
    private val dao: PetDao,
    private val authRepository: AuthRepository
) : PetRepository {
    override fun getAllPets(): Flow<List<PetEntity>> {
        return authRepository.currentUser.flatMapLatest { user ->
            val ownerId = user?.uid ?: "anonymous"
            dao.getAllPets(ownerId)
        }
    }

    override fun getPetById(id: Long): Flow<PetEntity?> {
        return authRepository.currentUser.flatMapLatest { user ->
            val ownerId = user?.uid ?: "anonymous"
            dao.getPetById(id, ownerId)
        }
    }

    override suspend fun claimAnonymousPets(newOwnerId: String) {
        dao.claimAnonymousPets(newOwnerId)
    }

    override suspend fun hasAnyPets(): Boolean {
        return dao.getAllPetsAnyOwner().firstOrNull()?.isNotEmpty() == true
    }

    override suspend fun upsertPet(pet: PetEntity): Long {
        val user = authRepository.currentUser.firstOrNull()
        val ownerId = user?.uid ?: "anonymous"
        return dao.upsertPet(pet.copy(ownerId = ownerId))
    }

    override suspend fun deletePet(pet: PetEntity) {
        dao.deletePet(pet)
    }
}
