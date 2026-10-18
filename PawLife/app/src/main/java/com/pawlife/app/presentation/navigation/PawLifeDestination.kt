package com.pawlife.app.presentation.navigation

/**
 * Sealed class defining all top-level navigation destinations in PawLife.
 * The [route] string is used by NavHost and navigation calls.
 */
sealed class PawLifeDestination(val route: String) {
    // Splash / Auth
    data object Splash        : PawLifeDestination("splash")
    data object Login         : PawLifeDestination("login")
    data object Onboarding    : PawLifeDestination("onboarding")
    data object SignIn        : PawLifeDestination("auth/signin")
    data object SignUp        : PawLifeDestination("auth/signup")
    data object ForgotPassword: PawLifeDestination("auth/forgot_password")
    data object PetSetupWizard: PawLifeDestination("setup/pet_wizard")

    // Main bottom-nav destinations
    data object Home          : PawLifeDestination("home")
    data object PetProfile    : PawLifeDestination("pet_profile/{petId}") {
        fun createRoute(petId: String) = "pet_profile/$petId"
    }
    data object AIAnalyzer    : PawLifeDestination("ai_analyzer")
    data object HealthTwin    : PawLifeDestination("health_twin")
    data object ActiveWalk    : PawLifeDestination("active_walk/{petId}") {
        fun createRoute(petId: Long) = "active_walk/$petId"
    }
    data object DocumentVault : PawLifeDestination("document_vault/{petId}") {
        fun createRoute(petId: Long) = "document_vault/$petId"
    }
    data object FamilyHub     : PawLifeDestination("family_hub")
    data object Community     : PawLifeDestination("community")
    data object ARWorld       : PawLifeDestination("ar_world")
    data object Settings      : PawLifeDestination("settings")
    data object Marketplace   : PawLifeDestination("marketplace")
    data object BreedScanner  : PawLifeDestination("breed_scanner")
    data object BarcodeScanner: PawLifeDestination("barcode_scanner/{petId}") {
        fun createRoute(petId: Long) = "barcode_scanner/$petId"
    }
    data object FoodScanner   : PawLifeDestination("food_scanner/{petId}") {
        fun createRoute(petId: Long) = "food_scanner/$petId"
    }
    data object NearbyPlaces  : PawLifeDestination("nearby_places")
    data object Passport      : PawLifeDestination("passport/{petId}") {
        fun createRoute(petId: Long) = "passport/$petId"
    }

    // Secondary screens
    data object WhatIfSimulator : PawLifeDestination("what_if_simulator")
    data object LegacyMemoryBook: PawLifeDestination("legacy_memory_book")
    data object EcoPaw          : PawLifeDestination("eco_paw")
    data object RecipeGenerator : PawLifeDestination("recipe_generator")
    data object Calendar        : PawLifeDestination("calendar")
    data object Notifications   : PawLifeDestination("notifications")
    data object Cart            : PawLifeDestination("cart")
    data object Checkout        : PawLifeDestination("checkout")
    data object Acoustics       : PawLifeDestination("acoustics")
}

/**
 * Bottom navigation bar items — a subset of all destinations.
 */
data class BottomNavItem(
    val destination: PawLifeDestination,
    val label: String,
    val iconRes: Int, // drawable resource id
)
