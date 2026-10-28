package com.example.safenumber.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = SlateDarkSurfaceVariant,
    onPrimaryContainer = SlateDarkTextPrimary,
    secondary = AccentIndigo,
    onSecondary = Color.White,
    background = SlateDarkBackground,
    onBackground = SlateDarkTextPrimary,
    surface = SlateDarkSurface,
    onSurface = SlateDarkTextPrimary,
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = SlateDarkTextSecondary,
    outline = SlateDarkBorder,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = SlateLightSurfaceVariant,
    onPrimaryContainer = SlateLightTextPrimary,
    secondary = AccentIndigo,
    onSecondary = Color.White,
    background = SlateLightBackground,
    onBackground = SlateLightTextPrimary,
    surface = SlateLightSurface,
    onSurface = SlateLightTextPrimary,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = SlateLightTextSecondary,
    outline = SlateLightBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun SafeNumberTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
