package com.basitce.gfx.core.core_ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4/8 tabanlı boşluk ölçeği. Ekranlarda sihirli dp sayısı kalmaz. */
@Immutable
data class OmniSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val section: Dp = 40.dp,
    /** Ekran kenar boşluğu. */
    val screen: Dp = 20.dp,
    /** Kart/panel içi boşluk. */
    val panel: Dp = 16.dp,
    /** Liste öğeleri arası. */
    val listItem: Dp = 12.dp,
    /** Bölümler arası. */
    val betweenSections: Dp = 24.dp,
    /** Minimum dokunma hedefi. */
    val touchTarget: Dp = 48.dp
)

val LocalOmniSpacing = staticCompositionLocalOf { OmniSpacing() }
