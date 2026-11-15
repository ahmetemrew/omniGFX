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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.shizuku.RemoteFileSystemEntry
import com.basitce.gfx.core.core_engine.shizuku.ShizukuConnectionState
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
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
    val spacing = OmniTheme.spacing

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
            if (presets.isEmpty()) item {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(OmniRadius.md)) {
                    Column(Modifier.padding(spacing.lg)) {
                        Text("Bu oyun için doğrulanmış hazır config yolu yok", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(spacing.sm))
                        Text("Sınırlı erişim özel uygulama verilerini açamaz. Yalnızca erişilebilir ve oyunun kullandığı metin config dosyalarını seç. Reklam, analiz ve motor kaynak dosyaları grafik ayarı değildir.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (presets.isNotEmpty()) {
                item {
                    Text(
                        text = "Önerilen config yolları",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        text = "Yollar oyun sürümüne göre değişebilir. Dosyanın varlığı ve erişimi analiz adımında kontrol edilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Kendin yol gir",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                OutlinedTextField(
                    value = state.targetFilePath,
                    onValueChange = { viewModel.onManualPathChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "/data/data/com.example/files/config.ini",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                Text(
                    text = "Dosya gezgini",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
                    val connection by viewModel.shizukuState.collectAsState()
                    if (connection.connectionState != ShizukuConnectionState.CONNECTED) {
                        OmniErrorCard(
                            title = "Shizuku bağlantısı gerekli",
                            message = "Dosya gezgini için Shizuku bağlantısı gerekli. " +
                                "Shizuku'yu başlatıp tekrar dene.",
                            actionLabel = "Shizuku'yu bağla",
                            onAction = { viewModel.connectShizuku() }
                        )
                    } else {
                        OmniErrorCard(
                            title = "Dizin okunamadı",
                            message = error,
                            actionLabel = "Tekrar dene",
                            onAction = { viewModel.browseTo(currentPath) }
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(spacing.lg)
                    .navigationBarsPadding()
            ) {
                if (state.targetFilePath.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(OmniRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(spacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Seçili: ${state.targetFilePath.substringAfterLast('/')}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = state.targetFilePath,
                                    style = OmniTheme.mono.micro,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(spacing.md))
                }

                OmniPrimaryButton(
                    text = "Dosyayı çek ve çözümle",
                    onClick = { viewModel.proceedToStep2() },
                    enabled = state.targetFilePath.isNotBlank() && !state.isBrowsing
                )
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
    val spacing = OmniTheme.spacing
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(OmniRadius.lg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(OmniRadius.lg))
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(OmniRadius.sm))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = preset.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OmniStatusChip(
                    text = preset.expectedFormat.label,
                    tone = OmniTone.Neutral
                )

                Row {
                    TextButton(onClick = onBrowse) {
                        Text(
                            "Dizini aç",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onSelect) {
                        Text(
                            "Bunu seç",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
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
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Yol derinleşince son segment görünür kalsın: açılışta en sağa odaklan.
    androidx.compose.runtime.LaunchedEffect(parts.size, path) {
        if (parts.isNotEmpty()) {
            listState.animateScrollToItem(parts.size - 1)
        }
    }

    // Kök görünür alandan çıktıysa başa "…" gelir; satır asla çıplak "›" ile
    // başlamaz (tur 2 P0-4). Örtü genişliği sabit değil, kısmi ilk öğenin sağ
    // ucuna kadar dinamik uzar: sabit genişlik, kaydırma konumuna göre kırpık
    // kök glifi sızdırıyordu (tur 3). layoutInfo koordinatları LazyRow'ın kendi
    // viewport'una göredir; örtü dış Box'ın solundan başladığı için LazyRow'ın
    // yatay padding'i (spacing.md) genişliğe eklenir (tur 3 devamı).
    val density = androidx.compose.ui.platform.LocalDensity.current
    val startPadPx = with(density) { spacing.md.toPx() }
    val coverWidth by remember(listState, startPadPx) {
        androidx.compose.runtime.derivedStateOf {
            val info = listState.layoutInfo
            val first = info.visibleItemsInfo.firstOrNull()
            if (first != null &&
                (first.index > 0 || first.offset < info.beforeContentPadding)
            ) {
                (startPadPx + first.offset + first.size).coerceAtLeast(0f)
            } else {
                0f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.md))
            .background(colors.surface)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(parts) { index, (name, fullPath) ->
                // TextButton'un 58dp asgari genişliği kısa segmentlerde ("data")
                // anlamsız boşluk üretiyordu: eşit 4dp padding'li düz hedef.
                // Asgari 48dp dokunma hedefi korunur (kısa segmentte ~7dp merkezleme).
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(OmniRadius.xs))
                        .clickable { viewModel.browseTo(fullPath) }
                        .heightIn(min = spacing.touchTarget)
                        .widthIn(min = spacing.touchTarget)
                        .padding(horizontal = spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = OmniTheme.mono.micro,
                        color = if (fullPath == path) colors.primary else colors.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                // Ayraç sonda: örtüden sonra gelen ilk şey daima segment adıdır,
                // çıplak "›" değil (tur 3).
                if (index < parts.lastIndex) {
                    Text(
                        text = "›",
                        style = OmniTheme.mono.micro,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = spacing.xxs)
                    )
                }
            }
        }
        // "…" örtüsü kısmi ilk öğeyi tamamen kapatır: genişlik öğenin sağ ucu,
        // yükseklik satırın tamamı; kesik harf kalmaz (tur 2 P0-4, tur 3).
        if (coverWidth > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(with(density) { coverWidth.toDp() })
                    .fillMaxHeight()
                    .clipToBounds()
                    .background(colors.surface)
            ) {
                Text(
                    text = "…",
                    style = OmniTheme.mono.micro,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = spacing.md)
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
    val spacing = OmniTheme.spacing
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(OmniRadius.md),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(OmniRadius.md))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(OmniRadius.sm))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.isDirectory) Icons.Outlined.Folder else Icons.AutoMirrored.Outlined.InsertDriveFile,
                    contentDescription = null,
                    tint = if (entry.isDirectory) OmniTheme.semantic.warning
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
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
                        text = subtitleParts.joinToString("   "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = OmniTheme.mono.micro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Seçili",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
