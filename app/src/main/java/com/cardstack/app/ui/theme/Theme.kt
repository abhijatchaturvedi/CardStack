package com.cardstack.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun CardStackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
