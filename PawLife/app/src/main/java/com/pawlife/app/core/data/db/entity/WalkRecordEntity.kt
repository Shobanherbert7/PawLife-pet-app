package com.pawlife.app.core.data.db.entity

data class WalkRecordEntity(
    val id: String = "",
    val petId: Long = 0L,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val distanceMeters: Float = 0f,
    // Store simple array of lat,lng strings like "34.05,-118.24"
    val routePoints: List<String> = emptyList()
)
