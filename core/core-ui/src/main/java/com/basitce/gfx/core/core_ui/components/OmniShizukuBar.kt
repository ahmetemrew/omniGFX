package com.basitce.gfx.core.core_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
import com.basitce.gfx.core.core_ui.theme.OmniWarning

sealed class ShizukuStatus {
    data object Connected : ShizukuStatus()
    data object PermissionRequired : ShizukuStatus()
    data object NotRunning : ShizukuStatus()
    data object Unknown : ShizukuStatus()
}

@Composable
fun OmniShizukuBar(
    status: ShizukuStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val containerColor by animateColorAsState(
        targetValue = when (status) {
            is ShizukuStatus.Connected -> OmniSuccess.copy(alpha = 0.12f)
            is ShizukuStatus.PermissionRequired -> OmniWarning.copy(alpha = 0.12f)
            is ShizukuStatus.NotRunning -> OmniError.copy(alpha = 0.12f)
            is ShizukuStatus.Unknown -> OmniSurfaceElevated
        },
        label = "shizuku_container_color"
    )

    val indicatorColor by animateColorAsState(
        targetValue = when (status) {
            is ShizukuStatus.Connected -> OmniSuccess
            is ShizukuStatus.PermissionRequired -> OmniWarning
            is ShizukuStatus.NotRunning -> OmniError
            is ShizukuStatus.Unknown -> OmniOnSurface.copy(alpha = 0.38f)
        },
        label = "shizuku_indicator_color"
    )

    val message = when (status) {
        is ShizukuStatus.Connected -> "Shizuku hazır"
        is ShizukuStatus.PermissionRequired -> "Shizuku izni gerekli"
        is ShizukuStatus.NotRunning -> "Shizuku çalışmıyor"
        is ShizukuStatus.Unknown -> "Shizuku durumu kontrol ediliyor"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(enabled = status !is ShizukuStatus.Connected, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = indicatorColor
        )

        if (status is ShizukuStatus.NotRunning || status is ShizukuStatus.PermissionRequired) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = indicatorColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
