package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniCodeBlock
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTextButton
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun StepPullAnalyze(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    val spacing = OmniTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(spacing.xxl))

        when {
            state.isPulling -> OmniLoading(
                label = state.pullProgressMessage.ifBlank { "Dosya çekiliyor…" }
            )
            // İçerik çekildiyse başarı gösterilir; büyük dosya uyarısı (error alanı)
            // akışı bloklamaz, yalnızca uyarı paneli olarak görünür.
            state.fileContent.isNotEmpty() && !state.isFileTooLarge ->
                SuccessContent(state = state, viewModel = viewModel)
            state.error != null -> ErrorContent(state = state, viewModel = viewModel)
            else -> IdleContent(viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(spacing.xxl))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun SuccessContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    val spacing = OmniTheme.spacing

    // Ara adım: dev hero kutusu yok. Kompakt durum satırı + bilgi kartları;
    // mühür/hero yalnızca final adımında (Dosya uygulandı) kullanılır.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Dosya çekildi",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spacing.md))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = OmniTheme.semantic.success,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Dosya okundu · ${formatFileSize(state.fileSizeBytes)} · ${state.configNodes.size} satır",
                style = MaterialTheme.typography.titleSmall,
                color = OmniTheme.semantic.success
            )
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        InfoCard(
            icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
            title = "Dosya bilgisi",
            items = listOf(
                "Dosya" to state.targetFilePath.substringAfterLast('/'),
                "Biçim" to state.detectedFormat.label,
                "Boyut" to formatFileSize(state.fileSizeBytes),
                "Satır" to "${state.configNodes.size}"
            )
        )

        state.fileMetadata?.let { meta ->
            Spacer(modifier = Modifier.height(spacing.listItem))
            FileIdentityCard(meta = meta)
        }

        if (state.isGameRunning) {
            Spacer(modifier = Modifier.height(spacing.listItem))
            WarningPanel(
                icon = Icons.Outlined.Warning,
                text = "Oyun şu an çalışıyor. Değişikliklerin uygulanması için oyunu kapatman gerekebilir."
            )
        }

        if (state.error != null && !state.isFileTooLarge) {
            Spacer(modifier = Modifier.height(spacing.listItem))
            WarningPanel(icon = Icons.Outlined.Info, text = state.error)
        }

        Spacer(modifier = Modifier.height(spacing.xxxl))

        OmniPrimaryButton(
            text = "Düzenleyiciye geç",
            onClick = { viewModel.proceedToStep3() }
        )
    }
}

@Composable
private fun ErrorContent(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    val spacing = OmniTheme.spacing

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (state.isFileTooLarge) "Dosya çok büyük" else "Dosya çekilemedi",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        OmniErrorCard(
            title = if (state.isFileTooLarge) "Boyut sınırı aşıldı" else "Çekme başarısız",
            message = state.error ?: "Beklenmeyen bir sorun oluştu. Tekrar deneyebilirsin.",
            modifier = Modifier.fillMaxWidth()
        )

        if (state.pathProbeMessages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = "Erişim ayrıntıları",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            OmniCodeBlock(text = state.pathProbeMessages.joinToString("\n"))
        }

        Spacer(modifier = Modifier.height(spacing.xxxl))

        OmniPrimaryButton(
            text = "Tekrar dene",
            onClick = { viewModel.proceedToStep2() }
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        OmniTextButton(
            text = "Dosya seçimine dön",
            onClick = { viewModel.goBack() }
        )
    }
}

@Composable
private fun IdleContent(viewModel: SetupWizardViewModel) {
    val spacing = OmniTheme.spacing

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(OmniRadius.xl))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(spacing.xxl))

        Text(
            text = "Dosya çekmeye hazır",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Seçilen dosya güvenli şekilde çekilecek, biçimi algılanacak " +
                "ve düzenlemeye hazırlanacak.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xxxl))

        OmniPrimaryButton(
            text = "Dosyayı çek",
            onClick = { viewModel.proceedToStep2() }
        )
    }
}

@Composable
private fun WarningPanel(icon: ImageVector, text: String) {
    val spacing = OmniTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(OmniTheme.semantic.warningContainer)
            .padding(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = OmniTheme.semantic.onWarningContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OmniTheme.semantic.onWarningContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    items: List<Pair<String, String>>,
    note: String? = null
) {
    val spacing = OmniTheme.spacing

    OmniCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (note != null) {
            Spacer(modifier = Modifier.height(spacing.xxs))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(spacing.md))
        items.forEach { (key, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(spacing.md))
                Text(
                    text = value,
                    style = OmniTheme.mono.key,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f, fill = false)
                )
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

/**
 * Dosya kimliği: varsayılan görünüm insani ve güven verici — "korunuyor" rozeti.
 * Ham UID/izin/SELinux değerleri yalnızca teknik detay isteyene açılır;
 * son kullanıcı bu terimleri bilmez (BRIEF copy kuralı).
 */
@Composable
private fun FileIdentityCard(meta: FileMetadata) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    OmniCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Dosya kimliği",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            OmniStatusChip(text = "Korunuyor", tone = OmniTone.Success)
        }
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = "Orijinal dosya izinleri ve güvenlik kimliği yedeklendi — yazma sırasında korunur.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(OmniRadius.sm))
                .clickable { expanded = !expanded }
                .padding(vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Text(
                text = if (expanded) "Teknik detayları gizle" else "Teknik detayları göster",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(spacing.xs))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = "Sahip: ${meta.uid ?: "?"}:${meta.gid ?: "?"}",
                    style = OmniTheme.mono.micro,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = "İzinler: ${meta.mode ?: "?"}",
                    style = OmniTheme.mono.micro,
                    color = colors.onSurfaceVariant
                )
                meta.seContext?.let {
                    Text(
                        text = "Güvenlik etiketi: $it",
                        style = OmniTheme.mono.micro,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}