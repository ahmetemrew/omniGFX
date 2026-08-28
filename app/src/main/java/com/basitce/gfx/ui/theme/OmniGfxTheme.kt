package com.basitce.gfx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val OmniDarkColorScheme = darkColorScheme(
    primary = OmniPrimary,
    onPrimary = OmniOnPrimary,
    primaryContainer = OmniPrimaryContainer,
    onPrimaryContainer = OmniOnPrimaryContainer,
    secondary = OmniSecondary,
    onSecondary = OmniOnSecondary,
    background = OmniBackgroundDark,
    onBackground = OmniOnBackgroundDark,
    surface = OmniSurfaceDark,
    onSurface = OmniOnSurfaceDark,
    surfaceVariant = OmniSurfaceVariantDark,
    onSurfaceVariant = OmniOnSurfaceVariantDark,
    error = OmniError,
    onError = OmniOnError
)

private val OmniLightColorScheme = lightColorScheme(
    primary = OmniPrimary,
    onPrimary = OmniOnPrimary,
    primaryContainer = OmniPrimaryContainer,
    onPrimaryContainer = OmniOnPrimaryContainer,
    secondary = OmniSecondary,
    onSecondary = OmniOnSecondary,
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF0B0F14),
    surface = Color.White,
    onSurface = Color(0xFF0B0F14),
    surfaceVariant = Color(0xFFE7EEF6),
    onSurfaceVariant = Color(0xFF3B4A5A),
    error = OmniError,
    onError = OmniOnError
)

private val OmniShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun OmniGfxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        OmniDarkColorScheme
    } else {
        OmniLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OmniTypography,
        shapes = OmniShapes,
        content = content
    )
}
