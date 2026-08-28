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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.shizuku.RemoteFileSystemEntry
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniOutline
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
import com.basitce.gfx.feature.feature_wizard.viewmodel.GamePathPreset
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun StepSelectFile(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    val currentPath by viewModel.browserPath.collectAsState()
    val entries by viewModel.browserEntries.collectAsState()
    val presets = remember(state.packageName) {
        viewModel.getPathPresets()
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
            if (presets.isNotEmpty()) {
                item {
                    Text(
                        text = "💡 Bilinen Config Dosyaları",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OmniOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bu oyun için bilinen config dosyaları. Birini seçerek doğrudan başlayabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniOnSurfaceVariant
                    )
                }
                items(presets) { preset ->
                    PresetCard(
                        preset = preset,
                        onSelect = { viewModel.selectPreset(preset) },
                        onBrowse = { viewModel.openPresetDirectory(preset) }
                    )
                }
            }

            item {
                Text(
                    text = "📝 Manuel Dosya Yolu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OmniOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.targetFilePath,
                    onValueChange = { viewModel.onManualPathChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "/data/data/com.example/files/config.ini",
                            color = OmniOnSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = OmniOnSurfaceVariant
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                    text = "📂 Dosya Gezgini",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OmniOnSurface
                )
            }

            item {
                BreadcrumbRow(
                    path = currentPath,
                    viewModel = viewModel
                )
            }

            state.browserError?.let { error ->
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

            if (state.isBrowsing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OmniPrimary)
                    }
                }
            } else {
                items(entries) { entry ->
                    FileListItem(
                        entry = entry,
                        isSelected = entry.path == state.targetFilePath,
                        onClick = {
                            if (entry.isDirectory) {
                                viewModel.browseTo(entry.path)
                            } else {
                                viewModel.selectFile(entry.path)
                            }
                        }
                    )
                }
            }
        }

        Surface(
            color = OmniSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                if (state.targetFilePath.isNotBlank()) {
                    Surface(
                        color = OmniPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = OmniPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Seçili: ${state.targetFilePath.substringAfterLast('/')}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = OmniPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = state.targetFilePath,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = OmniOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = { viewModel.proceedToStep2() },
                    enabled = state.targetFilePath.isNotBlank() && !state.isBrowsing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OmniPrimary,
                        disabledContainerColor = OmniSurfaceElevated
                    )
                ) {
                    Text(
                        text = "Dosyayı Çek ve Analiz Et →",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (state.targetFilePath.isNotBlank()) OmniOnPrimary else OmniOnSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: GamePathPreset,
    onSelect: () -> Unit,
    onBrowse: () -> Unit
) {
    Surface(
        color = OmniSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OmniOutline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OmniSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = OmniPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.label,
                        color = OmniOnSurface,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = preset.description,
                        color = OmniOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OmniPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = preset.expectedFormat.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = OmniPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    TextButton(onClick = onBrowse) {
                        Text(
                            "Dizini Aç",
                            style = MaterialTheme.typography.labelMedium,
                            color = OmniOnSurfaceVariant
                        )
                    }
                    TextButton(onClick = onSelect) {
                        Text(
                            "Bunu Seç",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = OmniPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbRow(
    path: String,
    viewModel: SetupWizardViewModel
) {
    val parts = viewModel.getBreadcrumb()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OmniSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(parts) { (name, fullPath) ->
            TextButton(
                onClick = { viewModel.browseTo(fullPath) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = if (fullPath == path) OmniPrimary else OmniOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (fullPath != path) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = OmniOnSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun FileListItem(
    entry: RemoteFileSystemEntry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) OmniPrimary.copy(alpha = 0.15f) else OmniSurface
    val borderColor = if (isSelected) OmniPrimary else OmniOutline

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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OmniSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = if (entry.isDirectory) Color(0xFFFBBF24) else Color(0xFF60A5FA),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    color = OmniOnSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitleParts = listOfNotNull(
                    if (!entry.isDirectory) entry.readableSize.takeIf { it.isNotBlank() } else null,
                    entry.modifiedDate,
                    entry.permissions
                )
                if (subtitleParts.isNotEmpty()) {
                    Text(
                        text = subtitleParts.joinToString(" • "),
                        color = OmniOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seçili",
                    tint = OmniPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
