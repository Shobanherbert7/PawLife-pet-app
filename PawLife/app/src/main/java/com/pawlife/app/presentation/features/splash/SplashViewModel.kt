package com.pawlife.app.presentation.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.domain.repository.AppPreferencesRepository
import com.pawlife.app.core.domain.repository.AuthRepository
import com.pawlife.app.core.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SplashDestination {
    LOADING, LOGIN, ONBOARDING, HOME
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesRepository: AppPreferencesRepository,
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository
) : ViewModel() {
    val destination: StateFlow<SplashDestination> = combine(
        authRepository.currentUser,
        preferencesRepository.isUserLoggedIn,
        petRepository.getAllPets()
    ) { user, isSavedLoggedIn, pets ->
        val hasPets = pets.isNotEmpty() || petRepository.hasAnyPets()
        val isLoggedIn = user != null || isSavedLoggedIn

        if (hasPets) {
            // Once a pet profile exists on this device, never ask for setup or sign-in again!
            SplashDestination.HOME
        } else if (isLoggedIn) {
            val uid = user?.uid ?: "default_user"
            val completed = preferencesRepository.hasCompletedOnboarding(uid).firstOrNull() ?: false
            if (completed) SplashDestination.HOME else SplashDestination.ONBOARDING
        } else {
            SplashDestination.LOGIN
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SplashDestination.LOADING)
}
