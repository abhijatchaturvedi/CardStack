package com.cardstack.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeChoice { LIGHT, DARK, SYSTEM }

private val DarkColorScheme = darkColorScheme(
    primary = IndigoAccent,
    onPrimary = AmoledBlack,
    primaryContainer = IndigoDeep,
    onPrimaryContainer = IndigoBright,
    secondary = ElectricBlue,
    onSecondary = AmoledBlack,
    background = AmoledBlack,
    onBackground = OnSurfacePrimary,
    surface = SurfaceDark,
    onSurface = OnSurfacePrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceSecondary,
    outline = OnSurfaceSecondary.copy(alpha = 0.4f),
    error = RedDue,
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE0FF),
    onPrimaryContainer = Color(0xFF0A1280),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OnSurfaceVariantLight.copy(alpha = 0.5f),
    error = RedDue,
)

@Composable
fun CardStackTheme(themeChoice: ThemeChoice = ThemeChoice.SYSTEM, content: @Composable () -> Unit) {
    val isDark = when (themeChoice) {
        ThemeChoice.DARK   -> true
        ThemeChoice.LIGHT  -> false
        ThemeChoice.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
