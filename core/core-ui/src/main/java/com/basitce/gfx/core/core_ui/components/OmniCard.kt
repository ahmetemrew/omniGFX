package com.basitce.gfx.core.core_ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/**
 * Panel: kart değil, yüzey adımı. Koyu temada hiyerarşi gölgeyle değil
 * surfaceContainer tonuyla kurulur; bu yüzden varsayılan kenarlık ve gölge YOK.
 * "Her şey özdeş kartlara bölünmüş SaaS kiti" slop'unun karşılığı.
 */
@Composable
fun OmniCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = OmniRadius.md,
    contentPadding: Dp = OmniTheme.spacing.panel,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/** Vurgulu panel: birincil bilgi veya onay kutusu. Kenarlıklı, bir adım daha yüksek. */
@Composable
fun OmniEmphasisPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(OmniRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = Dp.Hairline,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(OmniTheme.spacing.panel)) {
            content()
        }
    }
}

/**
 * Bölüm başlığı. BÜYÜK HARFLİ küçük etiket slop'tur; bunun yerine normal cümle
 * düzeni + ağırlık kontrastı. İsteğe bağlı sayaç ve eylem aynı hizada.
 */
@Composable
fun OmniSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = OmniTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OmniTheme.spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (count != null) {
            Text(
                text = count.toString(),
                style = OmniTheme.mono.key,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (action != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            action()
        }
    }
}
