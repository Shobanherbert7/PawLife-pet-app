package com.pawlife.app.feature.marketplace.presentation

import androidx.lifecycle.ViewModel
import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity
import com.pawlife.app.core.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    val cartItems: Flow<List<MarketplaceListingEntity>> = cartRepository.cartItems

    fun addToCart(item: MarketplaceListingEntity) {
        cartRepository.addToCart(item)
    }

    fun clearCart() {
        cartRepository.clearCart()
    }
}
