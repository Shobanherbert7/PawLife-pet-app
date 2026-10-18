package com.pawlife.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pawlife.app.presentation.features.stubs.*
import com.pawlife.app.presentation.features.splash.SplashScreen
import com.pawlife.app.feature.home.presentation.HomeScreen
import com.pawlife.app.feature.vault.presentation.DocumentVaultScreen
import com.pawlife.app.feature.vet.presentation.VetBookingScreen
import com.pawlife.app.feature.lostpet.presentation.LostPetScreen
import com.pawlife.app.feature.scanner.presentation.BarcodeScannerScreen
import com.pawlife.app.feature.community.presentation.CommunityScreen
import com.pawlife.app.feature.collar.presentation.CollarScannerScreen
import kotlinx.serialization.Serializable

private const val NAV_ANIM_DURATION = 300

/** Slide + fade enter from right */
private val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(tween(NAV_ANIM_DURATION)) { it } + fadeIn(tween(NAV_ANIM_DURATION))
}

/** Slide + fade exit to left */
private val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(tween(NAV_ANIM_DURATION)) { -it } + fadeOut(tween(NAV_ANIM_DURATION))
}

/** Pop enter from left */
private val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(tween(NAV_ANIM_DURATION)) { -it } + fadeIn(tween(NAV_ANIM_DURATION))
}

/** Pop exit to right */
private val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(tween(NAV_ANIM_DURATION)) { it } + fadeOut(tween(NAV_ANIM_DURATION))
}

// ── Bottom nav configuration ────────────────────────────────────────────────

@Serializable data class DocumentVaultRoute(val petId: Long)
@Serializable data class LostPetRoute(val petId: Long)
@Serializable object VetBookingRoute
@Serializable object BarcodeScannerRoute
@Serializable object CommunityFeedRoute

private data class BottomNavEntry(
    val destination: PawLifeDestination,
    val label: String,
    val icon: ImageVector,
    val contentDesc: String,
)

private val bottomNavItems = listOf(
    BottomNavEntry(PawLifeDestination.Home,       "Home",    Icons.Default.Home,      "Home screen"),
    BottomNavEntry(PawLifeDestination.AIAnalyzer, "AI",      Icons.Default.Analytics, "AI Analyzer"),
    BottomNavEntry(PawLifeDestination.FamilyHub,  "Family",  Icons.Default.Group,     "Family Hub"),
    BottomNavEntry(PawLifeDestination.Community,  "Explore", Icons.Default.Explore,   "Community"),
)

// ── Root routes that show the bottom bar ────────────────────────────────────

private val bottomBarRoutes = setOf(
    PawLifeDestination.Home.route,
    PawLifeDestination.PetProfile.route,
    PawLifeDestination.AIAnalyzer.route,
    PawLifeDestination.HealthTwin.route,
    PawLifeDestination.FamilyHub.route,
    PawLifeDestination.Community.route,
    PawLifeDestination.ARWorld.route,
    PawLifeDestination.Settings.route,
)

// ── NavHost ─────────────────────────────────────────────────────────────────

@Composable
fun PawLifeNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = PawLifeDestination.Splash.route,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                PawLifeBottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        ) {
            // ── Auth / Setup ──────────────────────────────────────────────
            composable(PawLifeDestination.Splash.route) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(PawLifeDestination.Login.route) {
                            popUpTo(PawLifeDestination.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(PawLifeDestination.Home.route) {
                            popUpTo(PawLifeDestination.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(PawLifeDestination.Onboarding.route) {
                            popUpTo(PawLifeDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PawLifeDestination.Login.route) {
                com.pawlife.app.feature.auth.presentation.LoginScreen(
                    onLoginSuccess = { hasProfile ->
                        val targetRoute = if (hasProfile) PawLifeDestination.Home.route else PawLifeDestination.Onboarding.route
                        navController.navigate(targetRoute) {
                            popUpTo(PawLifeDestination.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PawLifeDestination.Onboarding.route) {
                com.pawlife.app.feature.onboarding.presentation.OnboardingScreen(
                    onGetStarted = {
                        navController.navigate(PawLifeDestination.Home.route) {
                            popUpTo(PawLifeDestination.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Main bottom-nav screens ───────────────────────────────────
            composable(PawLifeDestination.Home.route) {
                HomeScreen(
                    onNavigateToAI = { navController.navigate(PawLifeDestination.AIAnalyzer.route) },
                    onNavigateToPetProfile = { petId ->
                        navController.navigate(PawLifeDestination.PetProfile.createRoute(petId))
                    },
                    onNavigateToNotifications = {
                        navController.navigate(PawLifeDestination.Notifications.route)
                    },
                    onNavigateToLostPet = { navController.navigate("lost_pet") },
                    onNavigateToVetBooking = { navController.navigate("vet_booking") },
                    onNavigateToCollar = { navController.navigate("collar_scanner") },
                    onNavigateToActiveWalk = { petId ->
                        navController.navigate(PawLifeDestination.ActiveWalk.createRoute(petId))
                    },
                    onNavigateToAR = { navController.navigate(PawLifeDestination.ARWorld.route) },
                    onNavigateToSettings = { navController.navigate(PawLifeDestination.Settings.route) },
                    onNavigateToMarketplace = { navController.navigate(PawLifeDestination.Marketplace.route) },
                    onNavigateToScanner = { petId -> navController.navigate(PawLifeDestination.BarcodeScanner.createRoute(petId)) },
                    onNavigateToOnboarding = { navController.navigate(PawLifeDestination.Onboarding.route) },
                    onNavigateToFoodScanner = { petId -> navController.navigate(PawLifeDestination.FoodScanner.createRoute(petId)) },
                    onNavigateToNearbyPlaces = { navController.navigate(PawLifeDestination.NearbyPlaces.route) },
                    onNavigateToPassport = { petId -> navController.navigate(PawLifeDestination.Passport.createRoute(petId)) },
                    onNavigateToCommunity = { navController.navigate(PawLifeDestination.Community.route) },
                    onNavigateToAcoustics = { navController.navigate(PawLifeDestination.Acoustics.route) }
                )
            }

            composable(PawLifeDestination.PetProfile.route) {
                com.pawlife.app.feature.pets.presentation.PetProfileScreen(
                    onNavigateToActiveWalk = { petId ->
                        navController.navigate(PawLifeDestination.ActiveWalk.createRoute(petId))
                    },
                    onNavigateToVault = { petId ->
                        navController.navigate(PawLifeDestination.DocumentVault.createRoute(petId))
                    }
                )
            }
            
            composable(PawLifeDestination.ActiveWalk.route) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")?.toLongOrNull() ?: 1L
                com.pawlife.app.feature.walk.presentation.ActiveWalkScreen(
                    petId = petId,
                    onWalkFinished = { navController.popBackStack() }
                )
            }
            
            composable(PawLifeDestination.DocumentVault.route) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")?.toLongOrNull() ?: 1L
                com.pawlife.app.feature.vault.presentation.DocumentVaultScreen(
                    petId = petId
                )
            }

            composable("lost_pet") {
                com.pawlife.app.feature.lostpet.presentation.LostPetScreen()
            }

            composable("collar_scanner") {
                CollarScannerScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("vet_booking") {
                com.pawlife.app.feature.vet.presentation.VetBookingScreen()
            }

            composable(PawLifeDestination.AIAnalyzer.route) {
                com.pawlife.app.feature.ai.presentation.AIAnalyzerScreen(
                    onNavigateToSimulator = { navController.navigate(PawLifeDestination.WhatIfSimulator.route) },
                    onNavigateToScanner = { navController.navigate(PawLifeDestination.BreedScanner.route) }
                )
            }

            composable(PawLifeDestination.FamilyHub.route) {
                com.pawlife.app.feature.family.presentation.FamilyHubScreen()
            }

            composable(PawLifeDestination.Community.route) {
                com.pawlife.app.feature.community.presentation.CommunityScreen()
            }

            composable(PawLifeDestination.ARWorld.route) {
                com.pawlife.app.feature.ar.presentation.ARWorldScreen()
            }

            composable(PawLifeDestination.Settings.route) {
                com.pawlife.app.feature.settings.presentation.SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(PawLifeDestination.Login.route) {
                            popUpTo(PawLifeDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(PawLifeDestination.Marketplace.route) {
                com.pawlife.app.feature.marketplace.presentation.MarketplaceScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(PawLifeDestination.Cart.route) }
                )
            }

            composable(PawLifeDestination.Cart.route) {
                com.pawlife.app.feature.marketplace.presentation.CartScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(PawLifeDestination.Checkout.route) }
                )
            }

            composable(PawLifeDestination.Checkout.route) {
                com.pawlife.app.feature.marketplace.presentation.CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCheckoutSuccess = { 
                        navController.popBackStack(PawLifeDestination.Marketplace.route, inclusive = false)
                    }
                )
            }

            composable(PawLifeDestination.WhatIfSimulator.route) {
                com.pawlife.app.feature.ai.presentation.WhatIfSimulatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(PawLifeDestination.BreedScanner.route) {
                com.pawlife.app.feature.ai.presentation.BreedScannerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(PawLifeDestination.BarcodeScanner.route) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")?.toLongOrNull() ?: 1L
                com.pawlife.app.feature.scanner.presentation.BarcodeScannerScreen(
                    petId = petId,
                    onBarcodeScanned = { barcode ->
                        // Navigating back after successful log
                        navController.popBackStack()
                    }
                )
            }
            
            composable(PawLifeDestination.Notifications.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Notifications Coming Soon")
                }
            }

            composable(PawLifeDestination.FoodScanner.route) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")?.toLongOrNull() ?: 1L
                com.pawlife.app.feature.food.presentation.FoodScannerScreen(
                    petId = petId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(PawLifeDestination.NearbyPlaces.route) {
                com.pawlife.app.feature.places.presentation.NearbyPlacesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(PawLifeDestination.Passport.route) {
                com.pawlife.app.feature.passport.presentation.PetPassportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(PawLifeDestination.Acoustics.route) {
                com.pawlife.app.feature.acoustics.presentation.AcousticMoodScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
private fun PawLifeBottomNavBar(
    navController: NavHostController,
    currentRoute: String?,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(com.pawlife.app.presentation.theme.CardDark.copy(alpha = 0.9f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = backStackEntry?.destination?.hierarchy?.any {
                    it.route == item.destination.route
                } == true

                val animatedColor by animateColorAsState(
                    targetValue = if (selected) com.pawlife.app.presentation.theme.Primary else com.pawlife.app.presentation.theme.SlateSubtle,
                    animationSpec = tween(300), label = "color"
                )

                val animatedBackground by animateColorAsState(
                    targetValue = if (selected) com.pawlife.app.presentation.theme.Primary.copy(alpha = 0.2f) else Color.Transparent,
                    animationSpec = tween(300), label = "bg"
                )

                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(item.destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .background(animatedBackground, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDesc,
                            tint = animatedColor,
                            modifier = Modifier.size(24.dp)
                        )
                        AnimatedVisibility(visible = selected) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = animatedColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
