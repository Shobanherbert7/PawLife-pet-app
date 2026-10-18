package com.pawlife.app.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity
import com.pawlife.app.core.domain.repository.MarketplaceRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MarketplaceRepository {

    private val listingsCollection = firestore.collection("marketplace_listings")

    override fun getAllListings(): Flow<List<MarketplaceListingEntity>> = callbackFlow {
        val listener = listingsCollection
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MarketplaceListingEntity::class.java)?.copy(id = doc.id)
                    }
                    trySend(listings)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun insertListing(listing: MarketplaceListingEntity): String {
        val docRef = if (listing.id.isNotBlank()) listingsCollection.document(listing.id) else listingsCollection.document()
        val listingToSave = listing.copy(id = docRef.id)
        docRef.set(listingToSave).await()
        return docRef.id
    }

    override suspend fun insertListings(listings: List<MarketplaceListingEntity>) {
        for (listing in listings) {
            insertListing(listing)
        }
    }
}
