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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniDropdown
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniTextButton
import com.basitce.gfx.core.core_ui.components.OmniTopBar
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import java.text.SimpleDateFormat
import java.util.Date

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OmniSecondaryButton(
                    text = "Yenile",
                    onClick = { viewModel.loadBackupsForSelectedProfile() },
                    fillWidth = false
                )
                if (state.backups.isNotEmpty()) {
                    OmniTextButton(
                        text = "Eski yedekleri temizle",
                        onClick = { viewModel.cleanOldBackups(3) }
                    )
                }
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

    OmniCard(modifier = modifier) {
        Text(
            text = fileName,
            style = OmniTheme.mono.key,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(spacing.xxs))
        Text(
            text = targetPath,
            style = OmniTheme.mono.micro,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        timestamp?.let {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale)
            Spacer(modifier = Modifier.height(spacing.xxs))
            Text(
                text = dateFormat.format(Date(it)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Yedeği sil",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            OmniPrimaryButton(
                text = "Geri yükle",
                onClick = onRestore,
                icon = Icons.Outlined.Restore,
                fillWidth = false
            )
        }
    }
}