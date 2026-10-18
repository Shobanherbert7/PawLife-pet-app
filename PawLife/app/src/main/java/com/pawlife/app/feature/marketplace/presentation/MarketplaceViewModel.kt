package com.pawlife.app.feature.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity
import com.pawlife.app.core.domain.repository.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: MarketplaceRepository
) : ViewModel() {
    private val _listings = MutableStateFlow<List<MarketplaceListingEntity>>(emptyList())
    val listings: StateFlow<List<MarketplaceListingEntity>> = _listings

    private val _filter = MutableStateFlow("ALL")
    val filter: StateFlow<String> = _filter

    init {
        viewModelScope.launch {
            repository.getAllListings().collect { data ->
                if (data.isEmpty() || data.none { it.type == "FOOD" }) {
                    seedMockData()
                } else {
                    _listings.value = data
                }
            }
        }
    }

    private fun seedMockData() {
        val mocks = listOf(
            MarketplaceListingEntity(
                id = "1",
                title = "Golden Retriever Puppy",
                description = "Friendly and playful golden retriever puppy.",
                type = "ADOPT",
                species = "Dog",
                breed = "Golden Retriever",
                price = 0f,
                contactInfo = "contact@rescue.org",
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d",
                createdAt = System.currentTimeMillis()
            ),
            MarketplaceListingEntity(
                id = "2",
                title = "Persian Cat",
                description = "Purebred Persian cat, very affectionate.",
                type = "SELL",
                species = "Cat",
                breed = "Persian",
                price = 300f,
                contactInfo = "seller@example.com",
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba",
                createdAt = System.currentTimeMillis()
            ),
            MarketplaceListingEntity(
                id = "3",
                title = "Premium Dog Food",
                description = "Unopened 20lb bag of premium salmon dog food.",
                type = "FOOD",
                species = "Dog",
                breed = "",
                price = 45f,
                contactInfo = "foodseller@example.com",
                imageUrl = "https://images.unsplash.com/photo-1589924691995-400dc9ecc119",
                createdAt = System.currentTimeMillis()
            ),
            MarketplaceListingEntity(
                id = "4",
                title = "Cat Tree Tower",
                description = "6-level cat tower, barely used.",
                type = "SELL",
                species = "Cat",
                breed = "",
                price = 80f,
                contactInfo = "catsupply@example.com",
                imageUrl = "https://images.unsplash.com/photo-1543852786-1cf6624b9987",
                createdAt = System.currentTimeMillis()
            ),
            MarketplaceListingEntity(
                id = "5",
                title = "Grain-Free Cat Food",
                description = "Healthy grain-free dry food for adult cats.",
                type = "FOOD",
                species = "Cat",
                breed = "",
                price = 25f,
                contactInfo = "foodseller@example.com",
                imageUrl = "https://images.unsplash.com/photo-1623366302587-bca86fcb80cc", // Some cat food image
                createdAt = System.currentTimeMillis()
            )
        )
        _listings.value = mocks
        
        // Also try to persist it, but don't wait or crash if it fails
        viewModelScope.launch {
            try {
                repository.insertListings(mocks)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setFilter(filterType: String) {
        _filter.value = filterType
    }
}
