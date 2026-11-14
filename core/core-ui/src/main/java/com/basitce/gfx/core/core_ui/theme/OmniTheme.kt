package com.basitce.gfx.core.core_ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalOmniSemantic = staticCompositionLocalOf { OmniDarkSemantic }

/**
 * Tek tema kaynağı. Dinamik renk (Material You) bilinçli olarak KAPALI: petrol+pirinç
 * kimliği ve "doğrulandı = petrol" semantiği duvar kâğıdı seed'iyle bozulmamalı.
 * Gerekçe: design/DESIGN.md §1.
 */
@Composable
fun OmniGfxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val motion = rememberOmniMotion()
    val semantic = if (darkTheme) OmniDarkSemantic else OmniLightSemantic

    CompositionLocalProvider(
        LocalOmniSemantic provides semantic,
        LocalOmniSpacing provides OmniSpacing(),
        LocalOmniMotion provides motion
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) OmniDarkColorScheme else OmniLightColorScheme,
            typography = OmniTypography,
            shapes = OmniShapes,
            content = content
        )
    }
}

/** Ekranların okuduğu tek kapı: OmniTheme.colors / .semantic / .spacing / .motion / .mono. */
object OmniTheme {
    val colors: androidx.compose.material3.ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme

    val typography: androidx.compose.material3.Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography

    val shapes: androidx.compose.material3.Shapes
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes

    val semantic: OmniSemantic
        @Composable @ReadOnlyComposable get() = LocalOmniSemantic.current

    val spacing: OmniSpacing
        @Composable @ReadOnlyComposable get() = LocalOmniSpacing.current

    val motion: OmniMotion
        @Composable @ReadOnlyComposable get() = LocalOmniMotion.current

    val mono: OmniMonoType
        @Composable @ReadOnlyComposable get() = OmniMonoType
}
