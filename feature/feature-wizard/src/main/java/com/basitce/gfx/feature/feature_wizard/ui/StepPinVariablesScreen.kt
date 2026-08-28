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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniOutline
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
import com.basitce.gfx.core.core_ui.theme.OmniWarning
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

    val pinnableNodes = remember(state.configNodes) {
        state.configNodes.filter { it.type == NodeType.KEY_VALUE }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBackground)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "📌 Değişkenler & Profil",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OmniOnSurface
                )
                Text(
                    text = "Düzenlediğin değişkenleri slider/dropdown olarak pinle ve profil olarak kaydet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OmniOnSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = state.profileName,
                    onValueChange = { viewModel.updateProfileName(it) },
                    label = { Text("Profil Adı") },
                    placeholder = { Text("örn: Rekabet Modu, 60 FPS, Düşük Grafik") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OmniSurface,
                        unfocusedContainerColor = OmniSurface,
                        focusedBorderColor = OmniPrimary,
                        unfocusedBorderColor = OmniOutline,
                        focusedTextColor = OmniOnSurface,
                        unfocusedTextColor = OmniOnSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Text(
                    text = "Profil Tipi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OmniOnSurface
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileTypeCard(
                        title = "Dinamik Profil",
                        description = "Slider ve menülerle kolayca değiştirilebilir.",
                        icon = Icons.Default.Tune,
                        isSelected = state.profileType == ProfileType.DYNAMIC,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setProfileType(ProfileType.DYNAMIC) }
                    )
                    ProfileTypeCard(
                        title = "Ham Dosya",
                        description = "Tam yedek. Tek tıkla olduğu gibi geri yazar.",
                        icon = Icons.Default.Code,
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
                    Text(
                        text = "Pinlenmiş Değişkenler (${state.pinnedVariables.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnSurface
                    )
                }

                items(state.pinnedVariables.values.toList(), key = { it.id }) { variable ->
                    PinnedVariableCard(
                        label = variable.label,
                        path = variable.path,
                        typeLabel = variable.uiComponentType.name,
                        onRemove = { viewModel.removePin(variable.path) }
                    )
                }
            }

            if (state.profileType == ProfileType.DYNAMIC) {
                item {
                    Text(
                        text = "Pinlenebilir Değişkenler",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnSurface
                    )
                    Text(
                        text = "Bir değişkene basarak slider, dropdown veya toggle olarak pinle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniOnSurfaceVariant
                    )
                }

                items(pinnableNodes, key = { it.id }) { node ->
                    val isPinned = state.pinnedVariables.containsKey(node.path)
                    PinnableNodeRow(
                        node = node,
                        isPinned = isPinned,
                        onClick = { showPinSheetFor = node }
                    )
                }
            }

            item {
                Surface(
                    color = OmniSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kayıt Özeti",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = OmniOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow("Oyun", state.gameName)
                        SummaryRow("Hedef Dosya", state.targetFilePath.substringAfterLast('/'))
                        SummaryRow("Format", state.detectedFormat.label)
                        if (state.profileType == ProfileType.DYNAMIC) {
                            SummaryRow("Dinamik Değişken", "${state.pinnedVariables.size} adet")
                        } else {
                            SummaryRow("İçerik", "${state.fileContent.length} karakter")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Bu profil dosyanın ham halini saklar. " +
                                    "Uygulandığında dosya tamamen değiştirilir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OmniWarning
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Surface(
                        color = OmniError.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = OmniError,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Surface(
            color = OmniSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { viewModel.saveProfileAndProceed() },
                    enabled = state.profileName.isNotBlank() && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OmniPrimary,
                        disabledContainerColor = OmniSurfaceElevated
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = OmniOnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kaydet ve Cihaza Yaz →",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (state.profileName.isNotBlank()) OmniOnPrimary
                                else OmniOnSurface.copy(alpha = 0.38f)
                        )
                    }
                }
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
    val bgColor = if (isSelected) OmniPrimary.copy(alpha = 0.1f) else OmniSurface
    val borderColor = if (isSelected) OmniPrimary else OmniOutline

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) OmniPrimary else OmniSurfaceElevated
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) OmniOnPrimary else OmniOnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) OmniPrimary else OmniOnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OmniOnSurfaceVariant,
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
    onRemove: () -> Unit
) {
    Surface(
        color = OmniPrimary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OmniPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = OmniSuccess,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OmniOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = OmniOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OmniPrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OmniPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Kaldır",
                    tint = OmniError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PinnableNodeRow(
    node: ConfigNode,
    isPinned: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isPinned) OmniPrimary.copy(alpha = 0.08f) else OmniSurface
    val borderColor = if (isPinned) OmniPrimary else OmniOutline

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PushPin,
                contentDescription = null,
                tint = if (isPinned) OmniPrimary else OmniOnSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.key,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = OmniOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Mevcut: ${node.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OmniOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isPinned) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Pinlendi",
                    tint = OmniSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OmniOnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            color = OmniOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
