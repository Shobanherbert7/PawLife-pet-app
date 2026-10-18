package com.pawlife.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pawlife.app.presentation.navigation.PawLifeNavHost
import com.pawlife.app.presentation.theme.PawLifeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.pawlife.app.core.domain.repository.AppPreferencesRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: AppPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep the splash screen on-screen until the NavHost is ready (optional)
        // splashScreen.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        setContent {
            val isDarkMode by preferences.darkModeEnabled.collectAsState(initial = true)
            PawLifeTheme(darkTheme = isDarkMode) {
                PawLifeNavHost()
            }
        }
    }
}
