package com.pawlife.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.pawlife.app.core.domain.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val darkModeEnabled: StateFlow<Boolean> = preferencesRepository.darkModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val pushNotificationsEnabled: StateFlow<Boolean> = preferencesRepository.pushNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }

    fun setPushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPushNotifications(enabled)
            if (enabled) {
                FirebaseMessaging.getInstance().subscribeToTopic("general")
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("general")
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        viewModelScope.launch {
            preferencesRepository.setUserLoggedIn(false)
            _navigationEvent.emit("login")
        }
    }

    fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener {
            logout()
        } ?: logout()
    }
}
