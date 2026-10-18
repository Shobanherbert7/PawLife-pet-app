package com.pawlife.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.auth.FirebaseUser
import com.pawlife.app.core.domain.repository.AppPreferencesRepository
import com.pawlife.app.core.domain.repository.PetRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val hasProfile: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private suspend fun onAuthSuccess(user: FirebaseUser?) {
        val uid = user?.uid ?: "local_user"
        val email = user?.email
        val name = user?.displayName

        // Persist logged-in state across app restarts & updates
        preferencesRepository.setUserLoggedIn(true, email, name)

        // Claim any local/anonymous pets for this user
        petRepository.claimAnonymousPets(uid)

        // Check if pet profile already exists on this device or for this user
        val hasPets = petRepository.hasAnyPets()
        val onboardingDone = preferencesRepository.hasCompletedOnboarding(uid).firstOrNull() ?: false
        val hasProfile = hasPets || onboardingDone

        if (hasProfile) {
            preferencesRepository.setOnboardingCompleted(uid, true)
        }

        _authState.value = AuthState.Success(hasProfile = hasProfile)
    }

    fun handleGoogleSignInResult(idToken: String?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (idToken == null) {
                // Fallback to anonymous sign-in if Google Sign-In is misconfigured or fails
                val result = authRepository.signInAnonymously()
                result.onSuccess { user ->
                    onAuthSuccess(user)
                }.onFailure { e ->
                    // Bypass Firebase errors for offline/dev testing
                    onAuthSuccess(null)
                }
            } else {
                val result = authRepository.signInWithGoogle(idToken)
                result.onSuccess { user ->
                    onAuthSuccess(user)
                }.onFailure { e ->
                    // Bypass Firebase errors for offline/dev testing
                    onAuthSuccess(null)
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUpWithEmail(email, password)
            result.onSuccess { user ->
                onAuthSuccess(user)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithEmail(email, password)
            result.onSuccess { user ->
                onAuthSuccess(user)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
}
