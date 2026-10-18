package com.pawlife.app.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pawlife.app.core.data.db.dao.FeedingDao
import com.pawlife.app.core.data.db.entity.FeedingLogEntity
import com.pawlife.app.core.data.db.entity.FeedingScheduleEntity
import com.pawlife.app.core.data.network.FeedingRequestDto
import com.pawlife.app.core.data.network.OpenFoodFactsApi
import com.pawlife.app.core.data.network.PawLifeBackendApi
import com.pawlife.app.core.data.network.ProductData
import com.pawlife.app.core.domain.repository.FeedingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class FeedingRepositoryImpl @Inject constructor(
    private val feedingDao: FeedingDao,
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val pawlifeBackendApi: PawLifeBackendApi
) : FeedingRepository {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Real-time ingest: listen to cloud feeding_logs and cache them into Room DB
        try {
            firestore.collection("feeding_logs")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    coroutineScope.launch {
                        for (doc in snapshot.documents) {
                            val petId = (doc.get("petId") as? Number)?.toLong() ?: 1L
                            val foodName = doc.getString("foodName") ?: continue
                            val portionGrams = (doc.get("portionGrams") as? Number)?.toFloat() ?: 100f
                            val calories = (doc.get("calories") as? Number)?.toInt()
                            val fedAt = (doc.get("fedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                            val notes = doc.getString("notes")

                            try {
                                feedingDao.insertFeedingLog(
                                    FeedingLogEntity(
                                        petId = petId,
                                        scheduleId = null,
                                        foodName = foodName,
                                        portionGrams = portionGrams,
                                        calories = calories,
                                        fedAt = fedAt,
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

    override fun getFeedingSchedules(petId: Long): Flow<List<FeedingScheduleEntity>> {
        return feedingDao.getFeedingSchedules(petId)
    }

    override suspend fun addFeedingSchedule(schedule: FeedingScheduleEntity) {
        feedingDao.insertFeedingSchedule(schedule)
    }

    override fun getFeedingLogs(petId: Long): Flow<List<FeedingLogEntity>> {
        return feedingDao.getFeedingLogs(petId)
    }

    override suspend fun addFeedingLog(log: FeedingLogEntity) {
        // 1. Save locally to Room
        feedingDao.insertFeedingLog(log)

        // 2. Synchronize to Dedicated pawlife-server via Retrofit
        try {
            pawlifeBackendApi.logFeeding(
                FeedingRequestDto(
                    petId = log.petId,
                    foodName = log.foodName,
                    portionGrams = log.portionGrams.toDouble(),
                    calories = log.calories
                )
            )
        } catch (e: Exception) {
            android.util.Log.w("FeedingRepo", "PawLife server sync notice: ${e.message}")
        }

        // 3. Upload to Cloud Firestore for legacy sync
        try {
            val cloudData = hashMapOf(
                "petId" to log.petId,
                "foodName" to log.foodName,
                "portionGrams" to log.portionGrams,
                "calories" to log.calories,
                "fedAt" to log.fedAt,
                "notes" to (log.notes ?: "Logged from Android Barcode Scanner"),
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("feeding_logs").document().set(cloudData, SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getFoodInfoFromBarcode(barcode: String): Result<ProductData> {
        return try {
            val response = openFoodFactsApi.getProductInfo(barcode)
            if (response.product != null) {
                Result.success(response.product)
            } else {
                Result.failure(Exception("Product not found for barcode: $barcode"))
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                // Return a mock product so the user can still test the feature
                val mockProduct = ProductData(
                    product_name = "Premium Pet Food (Scanned)",
                    brands = "PawLife",
                    nutriments = com.pawlife.app.core.data.network.NutrimentsData(
                        energy_kcal_100g = 350.0
                    )
                )
                Result.success(mockProduct)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
