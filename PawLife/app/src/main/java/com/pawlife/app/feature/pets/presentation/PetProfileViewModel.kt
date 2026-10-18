package com.pawlife.app.feature.pets.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import com.pawlife.app.core.domain.repository.PetRepository
import com.pawlife.app.core.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    fun updatePetPhoto(newUri: String) {
        val currentPet = pet.value ?: return
        viewModelScope.launch {
            petRepository.upsertPet(currentPet.copy(photoUri = newUri))
        }
    }

    fun addWeightRecord(weightKg: Float, notes: String = "") {
        val currentPet = pet.value ?: return
        viewModelScope.launch {
            weightRepository.addWeightRecord(
                WeightRecordEntity(
                    petId = currentPet.id,
                    weightKg = weightKg,
                    recordedAt = System.currentTimeMillis(),
                    notes = notes
                )
            )
            petRepository.upsertPet(currentPet.copy(weight = weightKg))
        }
    }
    
    val petId: Long = savedStateHandle.get<String>("petId")?.toLongOrNull() ?: 1L

    val pet: StateFlow<PetEntity?> = petRepository.getPetById(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val weightRecords: StateFlow<List<WeightRecordEntity>> = weightRepository.getWeightHistory(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _walkRecords = kotlinx.coroutines.flow.MutableStateFlow<List<com.pawlife.app.core.data.db.entity.WalkRecordEntity>>(emptyList())
    val walkRecords: StateFlow<List<com.pawlife.app.core.data.db.entity.WalkRecordEntity>> = _walkRecords

    init {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("walk_history").whereEqualTo("petId", petId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val list = snapshot.documents.mapNotNull { it.toObject(com.pawlife.app.core.data.db.entity.WalkRecordEntity::class.java) }
                _walkRecords.value = list.sortedByDescending { it.startTime }
            }
    }
}
