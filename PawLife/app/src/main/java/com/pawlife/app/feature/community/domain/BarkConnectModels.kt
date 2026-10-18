package com.pawlife.app.feature.community.domain

data class ActiveParkDog(
    val petName: String = "",
    val breed: String = "",
    val avatarEmoji: String = "🐕",
    val checkedInAgo: String = "Just now",
    val ownerName: String = "Community Member"
)

data class ParkLocation(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val distanceKm: Double = 1.0,
    val totalCheckedIn: Int = 0,
    val activeDogs: List<ActiveParkDog> = emptyList(),
    val bestTimeToVisit: String = "06:30 - 08:30 AM",
    val isOffLeash: Boolean = true,
    val isUserCheckedIn: Boolean = false
)

data class PlaydateCandidate(
    val id: String = "",
    val petName: String = "",
    val breed: String = "",
    val size: String = "Medium", // Small, Medium, Large
    val energyLevel: String = "High", // High, Moderate, Calm
    val playStyle: String = "Gentle Chaser", // Gentle Chaser, Ball Fetcher, Rough & Tumble
    val compatibilityPercent: Int = 92,
    val distanceKm: Double = 1.2,
    val ownerName: String = "",
    val avatarEmoji: String = "🐶",
    val isVerifiedVaccinated: Boolean = true
)
