package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RaspberryPrimary,
    onPrimary = CleanWhite,
    primaryContainer = PastelPillBackground,
    onPrimaryContainer = PastelPillText,
    secondary = PinkAccentMedium,
    onSecondary = CleanWhite,
    secondaryContainer = PinkAccentLight,
    onSecondaryContainer = RaspberryDark,
    tertiary = PinkAccentMedium,
    background = BlushBackground,
    onBackground = TextDarkPrimary,
    surface = CleanWhite,
    onSurface = TextDarkPrimary,
    surfaceVariant = PastelPillBackground,
    onSurfaceVariant = TextDarkSecondary,
    outline = CardBorderPink,
    outlineVariant = CardBorderPink
)

private val DarkColorScheme = darkColorScheme(
    primary = PinkAccentMedium,
    onPrimary = CleanWhite,
    primaryContainer = RaspberryDark,
    onPrimaryContainer = PastelPillBackground,
    secondary = PastelPillText,
    onSecondary = CleanWhite,
    tertiary = PinkAccentLight,
    background = Color(0xFF1E1719),
    onBackground = Color(0xFFF9EDEF),
    surface = Color(0xFF292023),
    onSurface = Color(0xFFF9EDEF),
    surfaceVariant = Color(0xFF382C30),
    onSurfaceVariant = Color(0xFFD6C8CC),
    outline = Color(0xFF4D3D42)
)

@Composable
fun SmartStepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SmartStepTheme(darkTheme = darkTheme, content = content)
}
