package com.basitce.gfx.core.core_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
 * Üç eşit segment (4×16dp) bir alet panelinin ölçek gözleri gibidir: dolu segment
 * erişim seviyesini, renk durumu anlatır. Hazır şebeke ikonu DEĞİL — telefon sinyali
 * çağrışımı yapan artan boyutlu çubuklar yerine eşit boyutlu kalibrasyon gözleri.
 * Teknik ayrıntı (UID, ADB/shell) son kullanıcıya gösterilmez.
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
    val idleColor = when (status) {
        ShizukuStatus.NotRunning -> colors.errorContainer
        else -> colors.outlineVariant
    }

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
        CalibrationGauge(filled = filled, active = animatedActive, idle = animatedIdle)

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

/** Üç eşit kalibrasyon segmenti — alet panelindeki bağlantı gözleri. */
@Composable
private fun CalibrationGauge(
    filled: Int,
    active: Color,
    idle: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(16.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < filled) active else idle)
            )
        }
    }
}
