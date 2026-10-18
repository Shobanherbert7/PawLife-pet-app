package com.pawlife.app.feature.weight.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import com.pawlife.app.core.domain.repository.WeightRepository
import com.pawlife.app.feature.weight.domain.GetWeightHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightUiState(
    val records: List<WeightRecordEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val getWeightHistoryUseCase: GetWeightHistoryUseCase,
    private val weightRepository: WeightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: Long = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow(WeightUiState(isLoading = true))
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getWeightHistoryUseCase(petId).collect { records ->
                _uiState.value = WeightUiState(records = records)
            }
        }
    }

    fun addWeight(weightKg: Float, notes: String? = null) {
        viewModelScope.launch {
            weightRepository.addWeightRecord(
                WeightRecordEntity(
                    petId = petId,
                    weightKg = weightKg,
                    recordedAt = System.currentTimeMillis(),
                    notes = notes
                )
            )
        }
    }
}
