package com.pawlife.app.core.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    private val PUSH_NOTIFICATIONS_KEY = booleanPreferencesKey("push_notifications_enabled")
    private val GLOBAL_ONBOARDING_KEY = booleanPreferencesKey("global_onboarding_completed")
    private val USER_LOGGED_IN_KEY = booleanPreferencesKey("user_logged_in")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    fun hasCompletedOnboarding(userId: String): Flow<Boolean> = context.dataStore.data.map { preferences ->
        val userKey = booleanPreferencesKey("onboarding_completed_$userId")
        val global = preferences[GLOBAL_ONBOARDING_KEY] ?: false
        val userSpecific = preferences[userKey] ?: false
        global || userSpecific
    }

    val isUserLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_LOGGED_IN_KEY] ?: false
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: true // Default to true for PawLife
    }

    val pushNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PUSH_NOTIFICATIONS_KEY] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setPushNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setUserLoggedIn(loggedIn: Boolean, email: String? = null, name: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[USER_LOGGED_IN_KEY] = loggedIn
            if (email != null) preferences[USER_EMAIL_KEY] = email
            if (name != null) preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun setOnboardingCompleted(userId: String, completed: Boolean) {
        context.dataStore.edit { preferences ->
            val key = booleanPreferencesKey("onboarding_completed_$userId")
            preferences[key] = completed
            preferences[GLOBAL_ONBOARDING_KEY] = completed
        }
    }
}
