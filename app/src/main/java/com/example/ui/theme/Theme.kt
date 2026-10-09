package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AnimeViolet,
    onPrimary = Color.White,
    primaryContainer = AnimeVioletDark,
    onPrimaryContainer = Color.White,
    secondary = AnimeCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = AnimeCyanLight,
    tertiary = AnimePink,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = RedAdult,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // Keep stylish dark palette default for anime aesthetic
    primary = AnimeViolet,
    onPrimary = Color.White,
    primaryContainer = AnimeVioletDark,
    onPrimaryContainer = Color.White,
    secondary = AnimeCyan,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = RedAdult
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark mode default as requested in prompt
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

