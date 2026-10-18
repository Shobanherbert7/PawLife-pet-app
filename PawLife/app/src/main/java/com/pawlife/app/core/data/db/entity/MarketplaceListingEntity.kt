package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marketplace_listings")
data class MarketplaceListingEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "ADOPT", // "ADOPT" or "SELL"
    val species: String = "Dog", // "Dog", "Cat", etc.
    val breed: String = "",
    val price: Float = 0f,
    val contactInfo: String = "",
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
