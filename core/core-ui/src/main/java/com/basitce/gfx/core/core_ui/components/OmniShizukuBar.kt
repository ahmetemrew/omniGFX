package com.basitce.gfx.core.core_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

sealed class ShizukuStatus {
    data object Connected : ShizukuStatus()
    data object PermissionRequired : ShizukuStatus()
    data object NotRunning : ShizukuStatus()
    data object Unknown : ShizukuStatus()
}

/**
 * İMZA ANI 2 — Bağlantı Göstergesi.
 *
 * Nokta + meta dizisi yerine kalibrasyon segmentleri + sade dilde durum + tek eylem.
 * Üç Kademeli göz (10/14/18dp, alt hizalı) bir alet panelinin ölçek yuvası gibidir:
 * dolu segment erişim seviyesini, renk durumu anlatır. Eşit boyutlu çubuklar tur 2'de
 * "Duraklat (⏸)" ikonuna benzediği için kademeli boyuta geçildi; pasif gözler zeminle
 * kaybolmasın diye surfaceContainerHighest dolgu + 1dp outline çerçeve taşır —
 * yuva her zaman "3 gözlü" okunur. Teknik ayrıntı (UID, ADB/shell) gösterilmez.
 */
@Composable
fun OmniShizukuBar(
    status: ShizukuStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    connectionDetail: String? = null,
    /** Bağlı ama sınırlı erişim (ADB): 2 segment dolu. Tam erişim (root): 3 segment. */
    limitedAccess: Boolean = false
) {
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion
    val colors = MaterialTheme.colorScheme

    val title = when (status) {
        ShizukuStatus.Connected -> "Bağlantı hazır"
        ShizukuStatus.PermissionRequired -> "İzin gerekiyor"
        ShizukuStatus.NotRunning -> "Shizuku çalışmıyor"
        ShizukuStatus.Unknown -> "Bağlantı denetleniyor"
    }
    val subtitle = when (status) {
        ShizukuStatus.Connected -> connectionDetail ?: "Dosya okuyup yazabilirsin"
        ShizukuStatus.PermissionRequired -> "OmniGFX'in Shizuku'ya erişmesine izin ver"
        ShizukuStatus.NotRunning -> "Shizuku'yu başlat, sonra buraya dön"
        ShizukuStatus.Unknown -> "Birkaç saniye sürebilir"
    }
    val actionLabel = when (status) {
        ShizukuStatus.Connected -> null
        ShizukuStatus.PermissionRequired -> "İzin ver"
        ShizukuStatus.NotRunning, ShizukuStatus.Unknown -> "Bağla"
    }

    val filled = when (status) {
        ShizukuStatus.Connected -> if (limitedAccess) 2 else 3
        ShizukuStatus.PermissionRequired -> 1
        ShizukuStatus.NotRunning -> 1
        ShizukuStatus.Unknown -> 0
    }
    val activeColor = when (status) {
        ShizukuStatus.Connected -> colors.primary
        ShizukuStatus.PermissionRequired -> colors.secondary
        ShizukuStatus.NotRunning -> colors.error
        ShizukuStatus.Unknown -> colors.onSurfaceVariant
    }
    // Pasif gözler zeminde kaybolmamalı: bir yüzey adımı yukarıda dolgu + outline
    // çerçeve (tur 2 P1-7). errorContainer türevi koyu zeminde 1.25:1 kalıyordu.
    val idleColor = colors.surfaceContainerHighest

    val animatedActive by animateColorAsState(
        targetValue = activeColor,
        animationSpec = motion.mediumTween(),
        label = "shizuku_segment_active"
    )
    val animatedIdle by animateColorAsState(
        targetValue = idleColor,
        animationSpec = motion.mediumTween(),
        label = "shizuku_segment_idle"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(colors.surfaceContainerLow)
            .then(
                if (actionLabel != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .heightIn(min = spacing.touchTarget)
            .padding(horizontal = spacing.lg, vertical = spacing.md)
            .semantics {
                contentDescription = if (actionLabel != null) "$title. $subtitle. $actionLabel."
                else "$title. $subtitle"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        CalibrationGauge(
            filled = filled,
            active = animatedActive,
            idle = animatedIdle,
            idleBorder = colors.outline
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        if (actionLabel != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(OmniRadius.full))
                    .background(colors.primaryContainer)
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onPrimaryContainer
                )
            }
        }
    }
}

/** Üç kademeli kalibrasyon gözü (10/14/18dp, alt hizalı) — alet paneli ölçek yuvası. */
@Composable
private fun CalibrationGauge(
    filled: Int,
    active: Color,
    idle: Color,
    idleBorder: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp)
    ) {
        repeat(3) { index ->
            val isFilled = index < filled
            val shape = RoundedCornerShape(2.dp)
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .height((10 + index * 4).dp)
                    .clip(shape)
                    .background(if (isFilled) active else idle)
                    .border(1.dp, if (isFilled) active else idleBorder, shape)
            )
        }
    }
}
