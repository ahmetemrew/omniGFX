package com.basitce.gfx.core.core_ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Şekil hiyerarşisi: veri öğeleri küçük radius (teknik), birincil eylem stadium
 * (fiziksel alet tuşu). Her şeye aynı radius vermek slop'tur — design/DESIGN.md §4.
 */
val OmniShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

object OmniRadius {
    /** Çip, etiket, tik rayı, kod bloğu. */
    val xs = 4.dp

    /** Metin alanı, liste satırı. */
    val sm = 8.dp

    /** Kart, panel. */
    val md = 12.dp

    /** Alt sayfa, diyalog. */
    val lg = 16.dp

    /** Hero okuma penceresi. */
    val xl = 20.dp

    /** Birincil buton ve FAB: fiziksel alet tuşu. */
    val full = 999.dp
}
