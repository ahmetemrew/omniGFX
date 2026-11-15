package com.basitce.gfx.ui.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniDropdown
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniTextButton
import com.basitce.gfx.core.core_ui.components.OmniTopBar
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupManagerScreen(
    onBack: () -> Unit,
    viewModel: BackupManagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = OmniTheme.spacing

    val selectedProfile = state.profiles.firstOrNull {
        it.id == state.selectedProfileId
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OmniTopBar(
                title = "Yedekler",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadBackupsForSelectedProfile() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Yenile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.screen),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            if (state.profiles.isNotEmpty()) {
                OmniDropdown(
                    label = "Profil",
                    options = state.profiles.map { it.name },
                    selectedOption = selectedProfile?.name ?: "",
                    onOptionSelected = { name ->
                        state.profiles.firstOrNull { it.name == name }?.let {
                            viewModel.selectProfile(it.id)
                        }
                    }
                )
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                state.isLoading -> OmniLoading(label = "Yedekler yükleniyor")

                state.backups.isEmpty() -> OmniEmptyState(
                    title = "Henüz yedek yok",
                    description = "Bu profil için henüz yedek alınmamış. Bir dosya yazıldığında ilk yedek otomatik oluşur.",
                    icon = Icons.Outlined.History
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = spacing.section),
                    verticalArrangement = Arrangement.spacedBy(spacing.listItem)
                ) {
                    item(key = "header") {
                        OmniSectionHeader(
                            title = "Yedek geçmişi",
                            count = state.backups.size,
                            action = {
                                OmniTextButton(
                                    text = "Eski yedekleri temizle",
                                    onClick = { viewModel.cleanOldBackups(3) }
                                )
                            }
                        )
                    }
                    items(state.backups, key = { it.entry.fileName }) { item ->
                        BackupRow(
                            fileName = item.entry.fileName,
                            targetPath = item.targetPath,
                            timestamp = item.entry.timestamp,
                            onRestore = { viewModel.restoreBackup(item) },
                            onDelete = { viewModel.deleteBackup(item) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Yedek kartı: birincil başlık insanca tarih/saat; sistem dosya adı değil.
 * Hedef yol ikinci satır, kısaltılmış yedek kimliği üçüncü satır mikro metin.
 * Silme eylemi sağ üst köşede hizalı — kartın ortasında yüzmez.
 */
@Composable
private fun BackupRow(
    fileName: String,
    targetPath: String,
    timestamp: Long?,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme

    val dateFormat = SimpleDateFormat("d MMMM yyyy · HH:mm", Locale("tr"))
    val backupId = fileName.substringAfter(".omnigfx.", "").substringBeforeLast(".bak")

    OmniCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timestamp?.let { dateFormat.format(Date(it)) } ?: fileName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(
                    text = targetPath,
                    style = OmniTheme.mono.key,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (backupId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(spacing.xxs))
                    Text(
                        text = "#$backupId · .bak",
                        style = OmniTheme.mono.micro,
                        color = colors.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Yedeği sil",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OmniPrimaryButton(
                text = "Geri yükle",
                onClick = onRestore,
                icon = Icons.Outlined.Restore,
                fillWidth = false
            )
        }
    }
}