package com.pawlife.app.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.domain.repository.PetRepository
import com.pawlife.app.core.domain.repository.FeedingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import com.pawlife.app.presentation.theme.Primary
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val feedingRepository: FeedingRepository
) : ViewModel() {
    val pets: StateFlow<List<PetEntity>> = petRepository.getAllPets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun getRecentActivities(petId: Long): Flow<List<RecentActivityUiModel>> {
        if (petId == -1L) return flowOf(emptyList())
        return feedingRepository.getFeedingLogs(petId).map { logs ->
            logs.sortedByDescending { it.fedAt }.map { log ->
                RecentActivityUiModel(
                    id = "feeding_${log.id}",
                    title = "Food Logged",
                    subtitle = log.foodName + if(log.calories != null) " (${log.calories} kcal)" else "",
                    timestampMs = log.fedAt,
                    icon = Icons.Default.Restaurant,
                    color = Primary
                )
            }
        }
    }

    fun addDummyPet() {
        viewModelScope.launch {
            try {
                petRepository.upsertPet(
                    PetEntity(
                        name = "Max",
                        species = "Dog",
                        breed = "Golden Retriever",
                        dateOfBirth = System.currentTimeMillis() - 94608000000, // ~3 years ago
                        gender = "Male",
                        weight = 25.0f,
                        photoUri = "https://images.unsplash.com/photo-1552053831-71594a27632d",
                        microchipId = null,
                        adoptionDate = null
                    )
                )
            } catch (e: Exception) {
                // Log and ignore to prevent crashes if offline or unauthenticated
                e.printStackTrace()
            }
        }
    }
}
