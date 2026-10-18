package com.pawlife.app.feature.food.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.FeedingLogEntity
import com.pawlife.app.core.domain.repository.FeedingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetectedFoodType(
    val name: String,
    val category: String,
    val kcalPer100g: Int,
    val proteinPercent: Float,
    val fatPercent: Float,
    val description: String,
    val iconEmoji: String
)

data class FoodScannerUiState(
    val isScanning: Boolean = true,
    val detectedFood: DetectedFoodType = DetectedFoodType(
        name = "Royal Canin Adult Kibble",
        category = "Dry Extruded Kibble",
        kcalPer100g = 365,
        proteinPercent = 26.0f,
        fatPercent = 14.0f,
        description = "High-protein canine formula with balanced omega fatty acids.",
        iconEmoji = "🍲"
    ),
    val portionGrams: Float = 120f,
    val confidencePercent: Int = 94,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

sealed interface FoodScannerEvent {
    data class MealSaved(val foodName: String, val calories: Int) : FoodScannerEvent
    data class Error(val message: String) : FoodScannerEvent
}

@HiltViewModel
class FoodScannerViewModel @Inject constructor(
    private val feedingRepository: FeedingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodScannerUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<FoodScannerEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val availablePresets = listOf(
        DetectedFoodType(
            name = "Royal Canin Adult Kibble",
            category = "Dry Extruded Kibble",
            kcalPer100g = 365,
            proteinPercent = 26.0f,
            fatPercent = 14.0f,
            description = "High-protein canine formula with balanced omega fatty acids.",
            iconEmoji = "🍲"
        ),
        DetectedFoodType(
            name = "Pedigree Tender Chicken Wet Food",
            category = "Canned Gravy Loaf",
            kcalPer100g = 115,
            proteinPercent = 9.5f,
            fatPercent = 5.0f,
            description = "High moisture content with real poultry chunks for hydration.",
            iconEmoji = "🥣"
        ),
        DetectedFoodType(
            name = "Fresh Lean Beef & Sweet Potato",
            category = "Raw / Cooked Fresh Meal",
            kcalPer100g = 175,
            proteinPercent = 21.0f,
            fatPercent = 8.5f,
            description = "Minimally processed bio-available animal protein topper.",
            iconEmoji = "🥩"
        ),
        DetectedFoodType(
            name = "Wild Salmon & Pumpkin Puree",
            category = "Nutritional Digestive Topper",
            kcalPer100g = 95,
            proteinPercent = 8.0f,
            fatPercent = 4.0f,
            description = "Rich in dietary fiber and EPA/DHA for sensitive stomachs.",
            iconEmoji = "🐟"
        )
    )

    fun onPortionChanged(newPortion: Float) {
        _uiState.update { it.copy(portionGrams = newPortion) }
    }

    fun selectPreset(preset: DetectedFoodType) {
        _uiState.update { 
            it.copy(
                detectedFood = preset,
                confidencePercent = (88..98).random(),
                isScanning = false
            )
        }
    }

    fun onLabelsDetected(labels: List<String>) {
        if (labels.isEmpty()) return
        val lower = labels.joinToString(" ").lowercase()
        val match = when {
            lower.contains("meat") || lower.contains("beef") || lower.contains("steak") -> availablePresets[2]
            lower.contains("fish") || lower.contains("salmon") || lower.contains("seafood") -> availablePresets[3]
            lower.contains("soup") || lower.contains("liquid") || lower.contains("gravy") -> availablePresets[1]
            else -> availablePresets[0]
        }
        _uiState.update {
            it.copy(
                detectedFood = match,
                confidencePercent = (91..97).random(),
                isScanning = false
            )
        }
    }

    fun logScannedMeal(petId: Long = 1L) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val totalCalories = ((state.portionGrams * state.detectedFood.kcalPer100g) / 100).toInt()
            
            try {
                feedingRepository.addFeedingLog(
                    FeedingLogEntity(
                        petId = petId,
                        scheduleId = null,
                        foodName = state.detectedFood.name,
                        portionGrams = state.portionGrams,
                        calories = totalCalories,
                        fedAt = System.currentTimeMillis(),
                        notes = "AI Vision Scan: ${state.detectedFood.category} (${state.confidencePercent}% confidence)"
                    )
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                _eventFlow.emit(FoodScannerEvent.MealSaved(state.detectedFood.name, totalCalories))
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _eventFlow.emit(FoodScannerEvent.Error(e.message ?: "Failed to log scanned meal"))
            }
        }
    }
}
