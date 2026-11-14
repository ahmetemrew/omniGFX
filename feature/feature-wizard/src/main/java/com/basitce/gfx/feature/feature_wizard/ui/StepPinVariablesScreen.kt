package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.ConfigNode
import com.basitce.gfx.feature.feature_wizard.viewmodel.NodeType
import com.basitce.gfx.feature.feature_wizard.viewmodel.ProfileType
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun StepPinVariables(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    var showPinSheetFor by remember { mutableStateOf<ConfigNode?>(null) }
    val spacing = OmniTheme.spacing

    val pinnableNodes = remember(state.configNodes) {
        state.configNodes.filter { it.type == NodeType.KEY_VALUE }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            item {
                Text(
                    text = "Düzenlediğin değerleri kaydırıcı veya liste olarak sabitle, profil olarak kaydet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.requiresRawProfile) item {
                Text(
                    text = "Bu INI dosyasında aynı bölümde tekrarlanan anahtarlar var. " +
                        "Satırların korunması için profil ham dosya olarak kaydedilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = state.profileName,
                    onValueChange = { viewModel.updateProfileName(it) },
                    label = { Text("Profil adı") },
                    placeholder = { Text("örn: Rekabet modu, 60 FPS, Düşük grafik") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(OmniRadius.sm)
                )
            }

            item {
                OmniSectionHeader(title = "Profil tipi")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.listItem)
                ) {
                    ProfileTypeCard(
                        title = "Dinamik profil",
                        description = "Kaydırıcı ve menülerle kolayca değiştirilir.",
                        icon = Icons.Outlined.Tune,
                        isSelected = state.profileType == ProfileType.DYNAMIC,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setProfileType(ProfileType.DYNAMIC) }
                    )
                    ProfileTypeCard(
                        title = "Ham dosya",
                        description = "Tam yedek. Tek dokunuşla olduğu gibi geri yazar.",
                        icon = Icons.Outlined.Code,
                        isSelected = state.profileType == ProfileType.MANUAL,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setProfileType(ProfileType.MANUAL) }
                    )
                }
            }

            if (state.pinnedVariables.isNotEmpty() &&
                state.profileType == ProfileType.DYNAMIC
            ) {
                item {
                    OmniSectionHeader(
                        title = "Sabitlenen değişkenler",
                        count = state.pinnedVariables.size
                    )
                }

                items(state.pinnedVariables.values.toList(), key = { it.id }) { variable ->
                    PinnedVariableCard(
                        label = variable.label,
                        path = variable.path,
                        typeLabel = variable.uiComponentType.label,
                        onRemove = { viewModel.removePin(variable.path) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            if (state.profileType == ProfileType.DYNAMIC) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                        OmniSectionHeader(title = "Sabitlenebilir değişkenler")
                        Text(
                            text = "Bir değişkene dokunarak kaydırıcı, liste veya anahtar olarak sabitle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(pinnableNodes, key = { it.id }) { node ->
                    val isPinned = state.pinnedVariables.containsKey(node.path)
                    PinnableNodeRow(
                        node = node,
                        isPinned = isPinned,
                        onClick = { showPinSheetFor = node },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item {
                OmniCard {
                    Text(
                        text = "Kayıt özeti",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.md))
                    SummaryRow("Oyun", state.gameName)
                    SummaryRow("Hedef dosya", state.targetFilePath.substringAfterLast('/'))
                    SummaryRow("Biçim", state.detectedFormat.label)
                    if (state.profileType == ProfileType.DYNAMIC) {
                        SummaryRow("Dinamik değişken", "${state.pinnedVariables.size} adet")
                    } else {
                        SummaryRow("İçerik", "${state.fileContent.length} karakter")
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            text = "Bu profil dosyanın tamamını saklar. " +
                                "Uygulandığında dosyanın tümü değişir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OmniTheme.semantic.onWarningContainer
                        )
                    }
                }
            }

            state.error?.let { error ->
                item {
                    OmniErrorCard(
                        title = "Profil kaydedilemedi",
                        message = error
                    )
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = spacing.screen, vertical = spacing.md)
                    .navigationBarsPadding()
            ) {
                OmniPrimaryButton(
                    text = "Kaydet ve cihaza yaz",
                    onClick = { viewModel.saveProfileAndProceed() },
                    enabled = state.profileName.isNotBlank(),
                    loading = state.isLoading,
                    icon = Icons.Outlined.Save
                )
            }
        }
    }

    showPinSheetFor?.let { node ->
        val currentVar = state.pinnedVariables[node.path]
        AdvancedPinSheet(
            node = node,
            currentVariable = currentVar,
            onDismiss = { showPinSheetFor = null },
            onSave = { variable ->
                viewModel.savePinnedVariable(variable)
                showPinSheetFor = null
            },
            onRemovePin = {
                viewModel.removePin(node.path)
                showPinSheetFor = null
            }
        )
    }
}

@Composable
private fun ProfileTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val spacing = OmniTheme.spacing
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(OmniRadius.md),
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(OmniRadius.md))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(spacing.panel)
                .heightIn(min = spacing.touchTarget),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(OmniRadius.sm))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.xxs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PinnedVariableCard(
    label: String,
    path: String,
    typeLabel: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(OmniRadius.sm),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(OmniRadius.sm))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.md, vertical = spacing.sm)
                .heightIn(min = spacing.touchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = OmniTheme.semantic.success,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = path,
                    style = OmniTheme.mono.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            OmniStatusChip(text = typeLabel, tone = OmniTone.Success)
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Sabiti kaldır",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PinnableNodeRow(
    node: ConfigNode,
    isPinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(OmniRadius.sm),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isPinned) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(OmniRadius.sm)
                ) else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.md, vertical = spacing.sm)
                .heightIn(min = spacing.touchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.PushPin,
                contentDescription = null,
                tint = if (isPinned) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.key,
                    style = OmniTheme.mono.key,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Mevcut: ${node.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isPinned) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Sabitlendi",
                    tint = OmniTheme.semantic.success,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val spacing = OmniTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
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