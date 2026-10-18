package com.pawlife.app.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pawlife.app.core.data.db.dao.PetDao
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.domain.repository.PetRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePetRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val petDao: PetDao
) : PetRepository {

    private val currentUser get() = auth.currentUser
    private fun petsCollection(userId: String) = firestore.collection("users").document(userId).collection("pets")

    override fun getAllPets(): Flow<List<PetEntity>> = callbackFlow {
        // 1. Immediately emit from local Room database (instant, offline-first)
        val localJob = launch {
            petDao.getAllPetsAnyOwner().collect { pets ->
                trySend(pets)
            }
        }

        // 2. If authenticated, listen to Firestore and update local DB
        val user = currentUser
        if (user != null) {
            val listener = petsCollection(user.uid)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        launch {
                            val remotePets = snapshot.documents.mapNotNull { it.data?.toPetEntity() }
                            remotePets.forEach { pet ->
                                petDao.upsertPet(pet.copy(ownerId = user.uid))
                            }
                        }
                    }
                }
            awaitClose {
                listener.remove()
                localJob.cancel()
            }
        } else {
            awaitClose { localJob.cancel() }
        }
    }

    override fun getPetById(id: Long): Flow<PetEntity?> = callbackFlow {
        val user = currentUser
        val localJob = launch {
            val flow = if (user != null) petDao.getPetById(id, user.uid) else petDao.getPetById(id, "anonymous")
            flow.collect { pet ->
                trySend(pet)
            }
        }

        if (user != null) {
            val docId = id.toString()
            val listener = petsCollection(user.uid).document(docId).addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val remotePet = snapshot.data?.toPetEntity()
                    if (remotePet != null) {
                        launch { petDao.upsertPet(remotePet.copy(ownerId = user.uid)) }
                    }
                }
            }
            awaitClose {
                listener.remove()
                localJob.cancel()
            }
        } else {
            awaitClose { localJob.cancel() }
        }
    }

    override suspend fun claimAnonymousPets(newOwnerId: String) {
        petDao.claimAnonymousPets(newOwnerId)
        try {
            val localPets = petDao.getAllPetsAnyOwner().firstOrNull() ?: emptyList()
            for (pet in localPets) {
                petsCollection(newOwnerId).document(pet.id.toString())
                    .set(pet.copy(ownerId = newOwnerId).toMap(), SetOptions.merge())
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun hasAnyPets(): Boolean {
        return petDao.getAllPetsAnyOwner().firstOrNull()?.isNotEmpty() == true
    }

    override suspend fun upsertPet(pet: PetEntity): Long {
        val newId = if (pet.id == 0L) System.currentTimeMillis() else pet.id
        val user = currentUser
        val ownerId = user?.uid ?: "local_owner"
        val petToSave = pet.copy(id = newId, ownerId = ownerId)

        // Always persist to local Room DB first!
        petDao.upsertPet(petToSave)

        // Sync with Firestore if authenticated
        if (user != null) {
            try {
                val docId = newId.toString()
                petsCollection(user.uid).document(docId).set(petToSave.toMap(), SetOptions.merge()).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return newId
    }

    override suspend fun deletePet(pet: PetEntity) {
        petDao.deletePet(pet)
        val user = currentUser
        if (user != null) {
            try {
                val docId = pet.id.toString()
                petsCollection(user.uid).document(docId).update("isActive", false).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mapper extensions
    private fun PetEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "species" to species,
        "breed" to breed,
        "dateOfBirth" to dateOfBirth,
        "gender" to gender,
        "weight" to weight,
        "photoUri" to photoUri,
        "microchipId" to microchipId,
        "adoptionDate" to adoptionDate,
        "isActive" to isActive,
        "createdAt" to createdAt
    )

    private fun Map<String, Any?>.toPetEntity(): PetEntity = PetEntity(
        id = (this["id"] as? Number)?.toLong() ?: 0L,
        name = this["name"] as? String ?: "",
        species = this["species"] as? String ?: "",
        breed = this["breed"] as? String ?: "",
        dateOfBirth = (this["dateOfBirth"] as? Number)?.toLong() ?: 0L,
        gender = this["gender"] as? String ?: "",
        weight = (this["weight"] as? Number)?.toFloat() ?: 0f,
        photoUri = this["photoUri"] as? String,
        microchipId = this["microchipId"] as? String,
        adoptionDate = (this["adoptionDate"] as? Number)?.toLong(),
        isActive = this["isActive"] as? Boolean ?: true,
        createdAt = (this["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
    )
}
