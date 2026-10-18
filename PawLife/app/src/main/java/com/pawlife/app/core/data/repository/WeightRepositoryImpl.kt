package com.pawlife.app.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pawlife.app.core.data.db.dao.WeightDao
import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import com.pawlife.app.core.domain.repository.WeightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao
) : WeightRepository {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Real-time ingest: listen to cloud weight_records and cache them into Room DB
        try {
            firestore.collection("weight_records")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    coroutineScope.launch {
                        for (doc in snapshot.documents) {
                            val petId = (doc.get("petId") as? Number)?.toLong() ?: 1L
                            val weightKg = (doc.get("weightKg") as? Number)?.toFloat() ?: continue
                            val recordedAt = (doc.get("recordedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                            val notes = doc.getString("notes")

                            try {
                                weightDao.insertWeight(
                                    WeightRecordEntity(
                                        petId = petId,
                                        weightKg = weightKg,
                                        recordedAt = recordedAt,
                                        notes = notes
                                    )
                                )
                            } catch (e: Exception) {
                                // Catch foreign key constraints safely
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getWeightHistory(petId: Long): Flow<List<WeightRecordEntity>> {
        return weightDao.getWeightHistory(petId)
    }

    override suspend fun getLatestWeight(petId: Long): WeightRecordEntity? {
        return weightDao.getLatestWeight(petId)
    }

    override suspend fun addWeightRecord(record: WeightRecordEntity) {
        // 1. Save locally to Room
        weightDao.insertWeight(record)

        // 2. Upload to Cloud Firestore for real-time Web Dashboard sync
        try {
            val cloudData = hashMapOf(
                "petId" to record.petId,
                "weightKg" to record.weightKg,
                "recordedAt" to record.recordedAt,
                "notes" to (record.notes ?: "Logged from Mobile"),
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("weight_records").document().set(cloudData, SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
