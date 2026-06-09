package com.tamdao.cinestream.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val CineStreamColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = TextSecondary,
    tertiary = NeonDim,
    background = Obsidian,
    surface = SurfaceDark,
    onPrimary = Obsidian,
    onSecondary = TextPrimary,
    onTertiary = NeonCyan,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val CineStreamLightColorScheme = lightColorScheme(
    primary = DeepCyan,
    secondary = LightTextSecondary,
    tertiary = DeepCyan.copy(alpha = 0.1f),
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = LightTextPrimary,
    onTertiary = DeepCyan,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun CineStreamTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    val colorScheme = if (useDarkTheme) CineStreamColorScheme else CineStreamLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (useDarkTheme) Obsidian.toArgb() else LightBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}