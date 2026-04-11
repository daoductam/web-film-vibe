package com.tamdao.cinestream.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun CineStreamTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CineStreamColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Obsidian.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}