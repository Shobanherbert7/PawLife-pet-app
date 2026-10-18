package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {
    private val _cartItems = MutableStateFlow<List<MarketplaceListingEntity>>(emptyList())
    val cartItems: Flow<List<MarketplaceListingEntity>> = _cartItems.asStateFlow()

    fun addToCart(item: MarketplaceListingEntity) {
        _cartItems.value = _cartItems.value + item
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}
