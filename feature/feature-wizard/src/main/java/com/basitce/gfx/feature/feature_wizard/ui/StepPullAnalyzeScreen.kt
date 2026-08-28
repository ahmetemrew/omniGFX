package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun StepPullAnalyze(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isPulling -> {
                PullingAnimation(progressMessage = state.pullProgressMessage)
            }
            state.fileContent.isNotEmpty() && state.error == null -> {
                SuccessContent(state = state, viewModel = viewModel)
            }
            state.error != null -> {
                ErrorContent(state = state, viewModel = viewModel)
            }
            else -> {
                IdleContent(viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun PullingAnimation(progressMessage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pull_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pull_rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(OmniSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = OmniPrimary,
                modifier = Modifier
                    .size(60.dp)
                    .rotate(rotation)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = progressMessage.ifBlank { "Dosya çekiliyor..." },
            style = MaterialTheme.typography.bodyLarge,
            color = OmniOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator(
            color = OmniPrimary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun SuccessContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(OmniSuccess.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = OmniSuccess,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Dosya Başarıyla Çekildi",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = OmniOnSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoCard(
            icon = Icons.AutoMirrored.Filled.InsertDriveFile,
            title = "Dosya Bilgisi",
            items = listOf(
                "Dosya" to state.targetFilePath.substringAfterLast('/'),
                "Format" to state.detectedFormat.label,
                "Boyut" to formatFileSize(state.fileSizeBytes),
                "Satır" to "${state.configNodes.size}"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        state.fileMetadata?.let { meta ->
            InfoCard(
                icon = Icons.Default.Fingerprint,
                title = "Güvenlik & Metadata",
                items = listOfNotNull(
                    "Sahip (UID:GID)" to "${meta.uid ?: "?"}:${meta.gid ?: "?"}",
                    "İzin (Mode)" to (meta.mode ?: "?"),
                    meta.seContext?.let { "SELinux" to it }
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.isGameRunning) {
            Surface(
                color = OmniWarning.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = OmniWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Oyun şu an çalışıyor. Değişikliklerin uygulanması için oyunu kapatman gerekebilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.error != null && !state.isFileTooLarge) {
            Surface(
                color = OmniWarning.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = OmniWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        Button(
            onClick = { viewModel.proceedToStep3() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
        ) {
            Text(
                text = "Akıllı Düzenleyiciye Geç →",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OmniOnPrimary
            )
        }
    }
}

@Composable
private fun ErrorContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(OmniError.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = OmniError,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (state.isFileTooLarge) "Dosya Çok Büyük" else "Çekme Başarısız",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = OmniError
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = OmniSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.error ?: "Bilinmeyen hata.",
                color = OmniOnSurface,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.pathProbeMessages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = OmniSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Erişim Detayları",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OmniOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.pathProbeMessages.forEach { msg ->
                        Text(
                            text = "• $msg",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = OmniOnSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.proceedToStep2() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
        ) {
            Text(
                text = "Tekrar Dene",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OmniOnPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { viewModel.goBack() }
        ) {
            Text(
                text = "← Dosya Seçimine Dön",
                color = OmniOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun IdleContent(viewModel: SetupWizardViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(OmniSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = OmniPrimary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Dosya Çekmeye Hazır",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = OmniOnSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Seçilen dosya Shizuku üzerinden güvenli şekilde çekilecek,\nformat algılanacak ve düzenlemeye hazırlanacak.",
            style = MaterialTheme.typography.bodyMedium,
            color = OmniOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.proceedToStep2() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
        ) {
            Text(
                text = "Dosyayı Çek",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OmniOnPrimary
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    items: List<Pair<String, String>>
) {
    Surface(
        color = OmniSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = OmniPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OmniOnSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OmniOnSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = OmniOnSurface
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
