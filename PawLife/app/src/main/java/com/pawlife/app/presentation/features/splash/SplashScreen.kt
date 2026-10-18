package com.pawlife.app.presentation.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.Secondary
import com.pawlife.app.presentation.theme.BackgroundDark
import kotlinx.coroutines.delay

/**
 * Splash screen with radial gradient bloom + paw logo animation.
 * Auto-navigates after 2.5 seconds.
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val glowRadius = remember { Animatable(0f) }

    LaunchedEffect(destination) {
        // Wait until destination is resolved from LOADING
        if (destination == SplashDestination.LOADING) return@LaunchedEffect
        
        // Animate in
        scale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        alpha.animateTo(1f, tween(600))
        glowRadius.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))

        delay(1500) // Shorter delay since we wait for loading

        when (destination) {
            SplashDestination.HOME -> onNavigateToHome()
            SplashDestination.ONBOARDING -> onNavigateToOnboarding()
            SplashDestination.LOGIN -> onNavigateToLogin()
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.25f * glowRadius.value),
                        Secondary.copy(alpha = 0.08f * glowRadius.value),
                        BackgroundDark,
                    ),
                    radius = 900f * glowRadius.value + 200f,
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Paw emoji as logo placeholder (replace with actual vector drawable)
            Text(
                text = "🐾",
                fontSize = 96.sp,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value),
            )
        }

        // App name below logo
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(alpha.value),
        ) {
            Text(
                text = "PawLife",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    brush = Brush.horizontalGradient(listOf(Primary, Secondary))
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
