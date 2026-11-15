package com.basitce.gfx.core.core_ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

/** Anlamsal ton. Success = petrol (doğrulandı), Warning = pirinç (sinyal). */
enum class OmniTone { Neutral, Success, Warning, Error }

@Composable
private fun toneContainer(tone: OmniTone): Color = when (tone) {
    OmniTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHighest
    OmniTone.Success -> OmniTheme.semantic.successContainer
    OmniTone.Warning -> OmniTheme.semantic.warningContainer
    OmniTone.Error -> MaterialTheme.colorScheme.errorContainer
}

@Composable
private fun toneContent(tone: OmniTone): Color = when (tone) {
    OmniTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    OmniTone.Success -> OmniTheme.semantic.onSuccessContainer
    OmniTone.Warning -> OmniTheme.semantic.onWarningContainer
    OmniTone.Error -> MaterialTheme.colorScheme.onErrorContainer
}

/** Küçük durum etiketi. Veri öğesi olduğu için 4dp — kartlarla şekil kontrastı. */
@Composable
fun OmniStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: OmniTone = OmniTone.Neutral,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(OmniRadius.xs))
            .background(toneContainer(tone))
            .padding(horizontal = OmniTheme.spacing.sm, vertical = OmniTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OmniTheme.spacing.xs)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = toneContent(tone),
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = toneContent(tone),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Hata kartı: özür dilemez. Ne oldu + ne yapmalı + tek eylem.
 * Başlık hatanın adı, mesaj kullanıcının yapabileceği şey.
 */
@Composable
fun OmniErrorCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val spacing = OmniTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        if (actionLabel != null && onAction != null) {
            // Hata zemininde petrol metin renk semantiğini bozuyordu (tur 2 P0-4):
            // eylem onErrorContainer metin + error %35 çerçeveli 36dp hap.
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(OmniRadius.full),
                contentPadding = PaddingValues(
                    horizontal = spacing.lg,
                    vertical = spacing.sm
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.heightIn(min = 36.dp)
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/**
 * Yükleniyor: sakin yer tutucu. Shimmer yok, sürekli dönen dekor yok.
 * Ne olduğunu söyleyen bir etiket + ince ilerleme çubuğu.
 */
@Composable
fun OmniLoading(
    label: String,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .heightIn(min = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Genel liste satırı (ayarlar, yedekler, gezgin). Tüm satır tıklanabilir ve
 * ≥48dp; ikon 24dp, başlık + açıklama solda, trailing sağda.
 */
@Composable
fun OmniListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    // Büyük fontta satır içi dikey pay sıkılaşır: daha çok satır ilk ekranda
    // tam görünür (tur 2 P0-2). Dokunma hedefi heightIn ile ≥48dp kalır.
    val largeFont = LocalDensity.current.fontScale > 1.15f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .heightIn(min = spacing.touchTarget)
            .padding(
                horizontal = spacing.lg,
                vertical = if (largeFont) spacing.sm else spacing.md
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

/**
 * Config/code bloğu: uygulamanın gerçek malzemesi. Monospace, gömülü zemin,
 * 4dp radius, yatay kaydırma (uzun yollar ve hash'ler kesilmez).
 */
@Composable
fun OmniCodeBlock(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.xs))
            .background(OmniTheme.semantic.codeSurface)
            .horizontalScroll(rememberScrollState())
            .padding(OmniTheme.spacing.md)
    ) {
        Text(
            text = text,
            style = OmniTheme.mono.key,
            color = OmniTheme.semantic.onCodeSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}
