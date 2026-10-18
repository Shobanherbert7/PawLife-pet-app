package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity
import kotlinx.coroutines.flow.Flow

interface MarketplaceRepository {
    fun getAllListings(): Flow<List<MarketplaceListingEntity>>
    suspend fun insertListing(listing: MarketplaceListingEntity): String
    suspend fun insertListings(listings: List<MarketplaceListingEntity>)
}
