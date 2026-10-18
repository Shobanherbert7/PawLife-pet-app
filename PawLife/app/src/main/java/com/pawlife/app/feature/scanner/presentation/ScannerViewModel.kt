package com.pawlife.app.feature.scanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.FeedingLogEntity
import com.pawlife.app.core.data.network.ProductData
import com.pawlife.app.core.domain.repository.FeedingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isScanning: Boolean = true,
    val isLoading: Boolean = false,
    val scannedProduct: ProductData? = null,
    val error: String? = null,
    val logSuccess: Boolean = false
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val feedingRepository: FeedingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeScanned(barcode: String) {
        // Prevent multiple scans of the same item triggering multiple requests
        if (!_uiState.value.isScanning || _uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isScanning = false, isLoading = true, error = null)

        viewModelScope.launch {
            val result = feedingRepository.getFoodInfoFromBarcode(barcode)
            result.onSuccess { product ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scannedProduct = product
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isScanning = true, // Resume scanning on failure
                    error = error.message ?: "Failed to fetch product info"
                )
            }
        }
    }

    fun logScannedMeal(petId: Long, portionGrams: Float = 100f) {
        val product = _uiState.value.scannedProduct ?: return
        
        viewModelScope.launch {
            val productName = product.product_name ?: "Unknown Food"
            val brand = product.brands?.let { "$it " } ?: ""
            val fullFoodName = "$brand$productName"
            
            // Calculate calories if available (calories per 100g)
            val kcalPer100g = product.nutriments?.energy_kcal_100g?.toString()?.toDoubleOrNull()
            val totalCalories = kcalPer100g?.let { (it * (portionGrams / 100f)).toInt() }
            
            val feedingLog = FeedingLogEntity(
                petId = petId,
                scheduleId = null,
                foodName = fullFoodName,
                portionGrams = portionGrams,
                calories = totalCalories,
                fedAt = System.currentTimeMillis(),
                notes = "Scanned via barcode"
            )
            
            try {
                feedingRepository.addFeedingLog(feedingLog)
                _uiState.value = _uiState.value.copy(logSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to log meal: ${e.message}")
            }
        }
    }

    fun resetScanner() {
        _uiState.value = ScannerUiState()
    }
}
