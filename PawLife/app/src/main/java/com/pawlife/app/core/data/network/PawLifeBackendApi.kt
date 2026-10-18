package com.pawlife.app.core.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class BackendHealthDto(
    val status: String? = null,
    val service: String? = null,
    val database: String? = null
)

@Serializable
data class PetDto(
    val id: Long,
    val name: String,
    val species: String = "Dog",
    val breed: String = "",
    val weight: Double = 5.0
)

@Serializable
data class FeedingRequestDto(
    val petId: Long,
    val foodName: String,
    val portionGrams: Double,
    val calories: Int? = null
)

@Serializable
data class FeedingResponseDto(
    val id: Long? = null,
    val petId: Long,
    val foodName: String,
    val portionGrams: Double = 150.0,
    val calories: Int? = null,
    val fedAt: String? = null
)

@Serializable
data class WalkRequestDto(
    val petId: Long,
    val startTime: String? = null,
    val endTime: String? = null,
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val caloriesBurned: Int = 0,
    val routePointsJson: String? = null
)

@Serializable
data class WalkResponseDto(
    val id: Long? = null,
    val petId: Long,
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val caloriesBurned: Int = 0,
    val createdAt: String? = null
)

@Serializable
data class TelemetryRequestDto(
    val petId: Long,
    val bpm: Int? = null,
    val bodyTemp: Double? = null,
    val steps: Int? = null,
    val battery: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class TelemetryResponseDto(
    val id: Long? = null,
    val petId: Long,
    val bpm: Int? = null,
    val isSafe: Boolean = true
)

interface PawLifeBackendApi {
    @GET("api/health")
    suspend fun getHealth(): BackendHealthDto

    @GET("api/pets")
    suspend fun getPets(): List<PetDto>

    @POST("api/feeding")
    suspend fun logFeeding(@Body req: FeedingRequestDto): FeedingResponseDto

    @GET("api/feeding/{petId}")
    suspend fun getFeedingLogs(@Path("petId") petId: Long): List<FeedingResponseDto>

    @POST("api/walks")
    suspend fun logWalk(@Body req: WalkRequestDto): WalkResponseDto

    @GET("api/walks/{petId}")
    suspend fun getWalkHistory(@Path("petId") petId: Long): List<WalkResponseDto>

    @POST("api/telemetry")
    suspend fun sendTelemetry(@Body req: TelemetryRequestDto): TelemetryResponseDto
}
