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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Nokta + "Shizuku hazır • ADB/shell (UID 2000)" meta dizisi yerine segmentli sinyal
 * göstergesi + sade dilde durum + tek eylem. Uygulamanın ilk 2 saniyesi: kullanıcı
 * "dosyaya yazabilir miyim?" sorusunun cevabını burada görür.
 * Teknik ayrıntı (UID, ADB/shell) son kullanıcıya gösterilmez.
 */
@Composable
fun OmniShizukuBar(
    status: ShizukuStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    connectionDetail: String? = null
) {
    val semantic = OmniTheme.semantic
    val spacing = OmniTheme.spacing
    val motion = OmniTheme.motion

    val accent by animateColorAsState(
        targetValue = when (status) {
            ShizukuStatus.Connected -> semantic.success
            ShizukuStatus.PermissionRequired -> semantic.warning
            ShizukuStatus.NotRunning -> MaterialTheme.colorScheme.error
            ShizukuStatus.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = motion.mediumTween(),
        label = "shizuku_accent"
    )
    val container by animateColorAsState(
        targetValue = when (status) {
            ShizukuStatus.Connected -> semantic.successContainer
            ShizukuStatus.PermissionRequired -> semantic.warningContainer
            ShizukuStatus.NotRunning -> MaterialTheme.colorScheme.errorContainer
            ShizukuStatus.Unknown -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = motion.mediumTween(),
        label = "shizuku_container"
    )
    val onContainer by animateColorAsState(
        targetValue = when (status) {
            ShizukuStatus.Connected -> semantic.onSuccessContainer
            ShizukuStatus.PermissionRequired -> semantic.onWarningContainer
            ShizukuStatus.NotRunning -> MaterialTheme.colorScheme.onErrorContainer
            ShizukuStatus.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = motion.mediumTween(),
        label = "shizuku_on_container"
    )

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
    val actionable = status != ShizukuStatus.Connected
    val segments = when (status) {
        ShizukuStatus.Connected -> 4
        ShizukuStatus.PermissionRequired -> 2
        ShizukuStatus.NotRunning -> 0
        ShizukuStatus.Unknown -> 1
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(container)
            .then(
                if (actionable) Modifier.clickable(onClick = onClick) else Modifier
            )
            .heightIn(min = spacing.touchTarget)
            .padding(horizontal = spacing.lg, vertical = spacing.md)
            .semantics {
                contentDescription = if (actionable) "$title. $subtitle. Düzenlemek için dokun."
                else "$title. $subtitle"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        SignalGauge(
            filled = segments,
            accent = accent,
            track = onContainer.copy(alpha = 0.22f)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = onContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = onContainer.copy(alpha = 0.82f)
            )
        }

        if (actionable) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Dört segmentli sinyal göstergesi — alet panelindeki bağlantı kadranı. */
@Composable
private fun SignalGauge(
    filled: Int,
    accent: androidx.compose.ui.graphics.Color,
    track: androidx.compose.ui.graphics.Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(22.dp)
    ) {
        repeat(4) { index ->
            val heights = listOf(7.dp, 11.dp, 15.dp, 19.dp)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heights[index])
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (index < filled) accent else track)
            )
        }
    }
}
