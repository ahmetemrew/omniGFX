package com.basitce.gfx.core.core_ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * OmniGFX "kalibrasyon tezgâhı" paleti. Tek kaynak burasıdır; ekranlar ham renk
 * sabiti kullanmaz, [ColorScheme] rollerini veya [OmniSemantic] okur.
 * Ayrıntı ve kontrast ölçümleri: design/DESIGN.md
 */

// --- İsimli renkler: KOYU (petrol dökümlü grafit gövde) ---
private val GraphiteBackground = Color(0xFF121A1B)
private val GraphiteSurface = Color(0xFF161F20)
private val GraphiteContainerLow = Color(0xFF1A2425)
private val GraphiteContainer = Color(0xFF1F2A2B)
private val GraphiteContainerHigh = Color(0xFF253132)
private val GraphiteContainerHighest = Color(0xFF2C393A)
private val GraphiteOnSurface = Color(0xFFE4EAE9)
private val GraphiteOnSurfaceVariant = Color(0xFFA8B6B5)
private val GraphiteOutline = Color(0xFF647673)
private val GraphiteOutlineVariant = Color(0xFF2E3B3A)

private val PetrolDark = Color(0xFF6ECDBD)
private val PetrolOnDark = Color(0xFF06231F)
private val PetrolContainerDark = Color(0xFF14453E)
private val PetrolOnContainerDark = Color(0xFFA8E6DA)

private val BrassDark = Color(0xFFE3A94F)
private val BrassOnDark = Color(0xFF2A1B02)
private val BrassContainerDark = Color(0xFF4A3413)
private val BrassOnContainerDark = Color(0xFFF6D9A4)

private val OxideDark = Color(0xFFF2837A)
private val OxideOnDark = Color(0xFF3A0A06)
private val OxideContainerDark = Color(0xFF5C1F19)
private val OxideOnContainerDark = Color(0xFFFBD5D0)

// --- İsimli renkler: AÇIK (soğuk çizim kâğıdı) ---
private val PaperBackground = Color(0xFFF4F7F6)
private val PaperSurface = Color(0xFFFFFFFF)
private val PaperContainerLow = Color(0xFFF0F4F3)
private val PaperContainer = Color(0xFFEAEFEE)
private val PaperContainerHigh = Color(0xFFE3E9E8)
private val PaperContainerHighest = Color(0xFFDBE3E2)
private val PaperOnSurface = Color(0xFF16211F)
private val PaperOnSurfaceVariant = Color(0xFF4C5A58)
private val PaperOutline = Color(0xFF7C8B89)
private val PaperOutlineVariant = Color(0xFFC6D0CF)

private val PetrolLight = Color(0xFF0E5F56)
private val PetrolOnLight = Color(0xFFFFFFFF)
private val PetrolContainerLight = Color(0xFFCDEAE4)
private val PetrolOnContainerLight = Color(0xFF06302A)

private val BrassLight = Color(0xFF8A5C10)
private val BrassOnLight = Color(0xFFFFFFFF)
private val BrassContainerLight = Color(0xFFF7E3BE)
private val BrassOnContainerLight = Color(0xFF3E2A05)

private val OxideLight = Color(0xFFA8342A)
private val OxideOnLight = Color(0xFFFFFFFF)
private val OxideContainerLight = Color(0xFFFADCD8)
private val OxideOnContainerLight = Color(0xFF4A0F09)

internal val OmniDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PetrolDark,
    onPrimary = PetrolOnDark,
    primaryContainer = PetrolContainerDark,
    onPrimaryContainer = PetrolOnContainerDark,
    inversePrimary = PetrolLight,
    secondary = BrassDark,
    onSecondary = BrassOnDark,
    secondaryContainer = BrassContainerDark,
    onSecondaryContainer = BrassOnContainerDark,
    tertiary = PetrolDark,
    onTertiary = PetrolOnDark,
    tertiaryContainer = PetrolContainerDark,
    onTertiaryContainer = PetrolOnContainerDark,
    background = GraphiteBackground,
    onBackground = GraphiteOnSurface,
    surface = GraphiteSurface,
    onSurface = GraphiteOnSurface,
    surfaceVariant = GraphiteContainerHigh,
    onSurfaceVariant = GraphiteOnSurfaceVariant,
    surfaceTint = PetrolDark,
    inverseSurface = PaperOnSurface,
    inverseOnSurface = PaperBackground,
    error = OxideDark,
    onError = OxideOnDark,
    errorContainer = OxideContainerDark,
    onErrorContainer = OxideOnContainerDark,
    outline = GraphiteOutline,
    outlineVariant = GraphiteOutlineVariant,
    scrim = Color(0xFF000000),
    surfaceBright = GraphiteContainerHighest,
    surfaceDim = GraphiteBackground,
    surfaceContainer = GraphiteContainer,
    surfaceContainerHigh = GraphiteContainerHigh,
    surfaceContainerHighest = GraphiteContainerHighest,
    surfaceContainerLow = GraphiteContainerLow,
    surfaceContainerLowest = GraphiteBackground
)

internal val OmniLightColorScheme: ColorScheme = lightColorScheme(
    primary = PetrolLight,
    onPrimary = PetrolOnLight,
    primaryContainer = PetrolContainerLight,
    onPrimaryContainer = PetrolOnContainerLight,
    inversePrimary = PetrolDark,
    secondary = BrassLight,
    onSecondary = BrassOnLight,
    secondaryContainer = BrassContainerLight,
    onSecondaryContainer = BrassOnContainerLight,
    tertiary = PetrolLight,
    onTertiary = PetrolOnLight,
    tertiaryContainer = PetrolContainerLight,
    onTertiaryContainer = PetrolOnContainerLight,
    background = PaperBackground,
    onBackground = PaperOnSurface,
    surface = PaperSurface,
    onSurface = PaperOnSurface,
    surfaceVariant = PaperContainerHigh,
    onSurfaceVariant = PaperOnSurfaceVariant,
    surfaceTint = PetrolLight,
    inverseSurface = GraphiteOnSurface,
    inverseOnSurface = GraphiteBackground,
    error = OxideLight,
    onError = OxideOnLight,
    errorContainer = OxideContainerLight,
    onErrorContainer = OxideOnContainerLight,
    outline = PaperOutline,
    outlineVariant = PaperOutlineVariant,
    scrim = Color(0xFF000000),
    surfaceBright = PaperSurface,
    surfaceDim = PaperContainerHighest,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = PaperContainerHighest,
    surfaceContainerLow = PaperContainerLow,
    surfaceContainerLowest = PaperSurface
)

/**
 * Material3 [ColorScheme]'de karşılığı olmayan anlamsal roller.
 * success = primary (petrol): "doğrulandı" marka vaadi. warning = secondary (pirinç):
 * sinyal lambası. Bilinçli karar, design/DESIGN.md §1.
 */
@Immutable
data class OmniSemantic(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    /** Değer okumasının tik rayı ve eski (sönük) değer rengi. */
    val readoutRail: Color,
    val readoutMuted: Color,
    /** Kod/config bloğu zemini — yüzeyden bir adım daha gömülü. */
    val codeSurface: Color,
    val onCodeSurface: Color,
    /** Diff: eklenen / silinen satır zemini. */
    val diffAdded: Color,
    val diffRemoved: Color,
    val onDiffAdded: Color,
    val onDiffRemoved: Color
)

internal val OmniDarkSemantic = OmniSemantic(
    success = PetrolDark,
    onSuccess = PetrolOnDark,
    successContainer = PetrolContainerDark,
    onSuccessContainer = PetrolOnContainerDark,
    warning = BrassDark,
    onWarning = BrassOnDark,
    warningContainer = BrassContainerDark,
    onWarningContainer = BrassOnContainerDark,
    info = GraphiteOnSurfaceVariant,
    readoutRail = GraphiteOutline,
    readoutMuted = Color(0xFF7C8D8A),
    codeSurface = Color(0xFF0E1516),
    onCodeSurface = Color(0xFFD3DEDC),
    diffAdded = Color(0xFF12332C),
    diffRemoved = Color(0xFF3A1D1A),
    onDiffAdded = Color(0xFF9FE0CF),
    onDiffRemoved = Color(0xFFF0A79C)
)

internal val OmniLightSemantic = OmniSemantic(
    success = PetrolLight,
    onSuccess = PetrolOnLight,
    successContainer = PetrolContainerLight,
    onSuccessContainer = PetrolOnContainerLight,
    warning = BrassLight,
    onWarning = BrassOnLight,
    warningContainer = BrassContainerLight,
    onWarningContainer = BrassOnContainerLight,
    info = PaperOnSurfaceVariant,
    readoutRail = PaperOutline,
    readoutMuted = Color(0xFF7E8D8B),
    codeSurface = Color(0xFFEFF3F2),
    onCodeSurface = Color(0xFF22302E),
    diffAdded = Color(0xFFDCEFE7),
    diffRemoved = Color(0xFFF7E2DE),
    onDiffAdded = Color(0xFF0B4A3F),
    onDiffRemoved = Color(0xFF7E2A20)
)
