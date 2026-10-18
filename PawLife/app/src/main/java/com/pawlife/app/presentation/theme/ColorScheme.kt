package com.pawlife.app.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val PawLifeDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.Black,
    tertiary = Warning,
    background = BackgroundDark,
    onBackground = SlateLight,
    surface = CardDark,
    onSurface = SlateLight,
    surfaceVariant = Color(0xFF232836),
    onSurfaceVariant = SlateSubtle,
    error = Danger,
    onError = Color.White,
    outline = Color(0xFF2A3040)
)

val PawLifeLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.Black,
    tertiary = Warning,
    background = BackgroundLight,
    onBackground = SlateDark,
    surface = CardLight,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = SlateDarkSubtle,
    error = Danger,
    onError = Color.White,
    outline = Color(0xFFCBD5E1)
)
