package com.pawlife.app.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.domain.repository.PetRepository
import com.pawlife.app.core.domain.repository.AppPreferencesRepository
import com.pawlife.app.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    fun savePet(name: String, breed: String, weightStr: String, photoUri: String? = null) {
        val weight = weightStr.toFloatOrNull() ?: 0f
        viewModelScope.launch {
            petRepository.upsertPet(
                PetEntity(
                    name = name,
                    species = "Dog",
                    breed = breed,
                    dateOfBirth = System.currentTimeMillis() - 31536000000L, // 1 year old mock
                    gender = "Unknown",
                    weight = weight,
                    photoUri = photoUri,
                    microchipId = null,
                    adoptionDate = null
                )
            )
            val user = authRepository.currentUser.firstOrNull()
            val userId = user?.uid ?: "anonymous"
            preferencesRepository.setOnboardingCompleted(userId, true)
            preferencesRepository.setUserLoggedIn(true)
            _isFinished.value = true
        }
    }
}
