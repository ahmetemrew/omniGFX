package com.basitce.gfx.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_engine.profile.FindingSeverity
import com.basitce.gfx.core.core_engine.profile.ProfileRiskLevel
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.components.OmniTopBar
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme

private fun ProfileRiskLevel.label(): String = when (this) {
    ProfileRiskLevel.SAFE -> "Güvenli"
    ProfileRiskLevel.LOW -> "Düşük risk"
    ProfileRiskLevel.MEDIUM -> "Orta risk"
    ProfileRiskLevel.HIGH -> "Yüksek risk"
    ProfileRiskLevel.CRITICAL -> "Kritik risk"
}

private fun ProfileRiskLevel.tone(): OmniTone = when (this) {
    ProfileRiskLevel.SAFE, ProfileRiskLevel.LOW -> OmniTone.Success
    ProfileRiskLevel.MEDIUM -> OmniTone.Warning
    ProfileRiskLevel.HIGH, ProfileRiskLevel.CRITICAL -> OmniTone.Error
}

private fun FindingSeverity.label(): String = when (this) {
    FindingSeverity.INFO -> "Bilgi"
    FindingSeverity.WARNING -> "Uyarı"
    FindingSeverity.HIGH -> "Yüksek"
    FindingSeverity.CRITICAL -> "Kritik"
}

private fun FindingSeverity.tone(): OmniTone = when (this) {
    FindingSeverity.INFO -> OmniTone.Neutral
    FindingSeverity.WARNING -> OmniTone.Warning
    FindingSeverity.HIGH, FindingSeverity.CRITICAL -> OmniTone.Error
}

/**
 * Profil analizi: dışarıdan gelen bir profilin güvenliğini gösterir.
 * Ürünün "güven" vaadinin doğrudan karşılığı — puan ve risk net, bulgular okunur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMarketplaceScreen(
    onBack: () -> Unit,
    viewModel: ProfileMarketplaceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            OmniTopBar(
                title = "Profil analizi",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.screen)
                .padding(bottom = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text = "Bir profil JSON'u yapıştır, içeriğini çözümle. " +
                    "İçe aktarmadan önce ne yaptığını gör.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val clipboard = LocalClipboardManager.current
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(OmniRadius.xs))
                        .background(colors.surfaceContainerHigh)
                        .clickable {
                            clipboard.getText()?.text?.takeIf { it.isNotBlank() }?.let {
                                viewModel.onJsonInputChange(it)
                            }
                        }
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPaste,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Panodan yapıştır",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = state.jsonInput,
                onValueChange = viewModel::onJsonInputChange,
                placeholder = {
                    Text(
                        text = "{ \"name\": \"...\", \"rules\": [...] }",
                        style = OmniTheme.mono.key,
                        color = colors.onSurfaceVariant
                    )
                },
                textStyle = OmniTheme.mono.key,
                shape = RoundedCornerShape(OmniRadius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainerLow,
                    unfocusedContainerColor = colors.surfaceContainerLow,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                OmniPrimaryButton(
                    text = "Çözümle",
                    onClick = { viewModel.analyze() },
                    enabled = state.jsonInput.isNotBlank() && !state.isAnalyzing,
                    modifier = Modifier.weight(1f)
                )
                OmniSecondaryButton(
                    text = "İçe aktar",
                    onClick = { viewModel.importProfile() },
                    enabled = state.analysis?.canImport == true && !state.isImporting,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.isAnalyzing || state.isImporting) {
                OmniLoading(
                    label = if (state.isImporting) "Profil içe aktarılıyor…"
                    else "Profil çözümleniyor…"
                )
            }

            state.error?.let { error ->
                OmniErrorCard(title = "İşlem tamamlanamadı", message = error)
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface
                )
            }

            state.analysis?.let { analysis ->
                OmniSectionHeader(title = "Sonuç")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    OmniStatusChip(
                        text = "Puan ${analysis.score}/100",
                        tone = OmniTone.Neutral
                    )
                    OmniStatusChip(
                        text = analysis.riskLevel.label(),
                        tone = analysis.riskLevel.tone()
                    )
                }

                if (analysis.findings.isNotEmpty()) {
                    OmniSectionHeader(
                        title = "Bulgular",
                        count = analysis.findings.size
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        items(analysis.findings) { finding ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                OmniStatusChip(
                                    text = finding.severity.label(),
                                    tone = finding.severity.tone()
                                )
                                Text(
                                    text = finding.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
