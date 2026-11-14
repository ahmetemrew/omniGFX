package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSectionHeader
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.GameDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun GameDetailScreen(
    gameId: String,
    onNewProfile: (packageName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val packageName by viewModel.packageName.collectAsStateWithLifecycle()
    val profileToDelete by viewModel.profileToDelete.collectAsStateWithLifecycle()
    val profileToApply by viewModel.profileToApply.collectAsStateWithLifecycle()
    val spacing = OmniTheme.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            packageName?.let { pkg ->
                ExtendedFloatingActionButton(
                    onClick = { onNewProfile(pkg) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Profil ekle", style = MaterialTheme.typography.labelLarge) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.sm, vertical = spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(spacing.xs))
                Column {
                    Text(
                        text = game?.name ?: "Oyun",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = game?.packageName ?: "",
                        style = OmniTheme.mono.micro,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    OmniEmptyState(
                        title = "Henüz profil yok",
                        description = "Bu oyun için ilk profilini oluştur; ayarlarını tek dokunuşla uygulayabilirsin.",
                        icon = Icons.Outlined.Tune,
                        action = {
                            packageName?.let { pkg ->
                                OmniPrimaryButton(
                                    text = "Profil ekle",
                                    onClick = { onNewProfile(pkg) },
                                    fillWidth = false
                                )
                            }
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.listItem)
                ) {
                    item {
                        OmniSectionHeader(title = "Kayıtlı profiller", count = profiles.size)
                    }
                    items(profiles, key = { it.id }) { profile ->
                        ProfileCard(
                            profile = profile,
                            onApply = { viewModel.requestApply(profile) },
                            onDelete = { viewModel.requestDelete(profile) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            icon = {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Profili sil") },
            text = {
                Text("\"${profile.name}\" profili kalıcı olarak silinecek. Bu işlem geri alınamaz.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Vazgeç")
                }
            },
            shape = RoundedCornerShape(OmniRadius.lg),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    profileToApply?.let { profile ->
        ProfileApplySheet(
            profile = profile,
            onDismiss = { viewModel.dismissApply() }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileEntity,
    onApply: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale)

    OmniCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(
                    text = profile.targetFilePath.substringAfterLast('/'),
                    style = OmniTheme.mono.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Text(
                    text = dateFormat.format(Date(profile.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            if (profile.isManual) {
                OmniStatusChip(text = "Ham dosya", tone = OmniTone.Warning)
            } else {
                OmniStatusChip(text = "Dinamik", tone = OmniTone.Success)
            }
        }

        Spacer(modifier = Modifier.height(spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Profili sil",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            OmniPrimaryButton(
                text = "Uygula",
                onClick = onApply,
                icon = Icons.Outlined.PlayArrow,
                fillWidth = false
            )
        }
    }
}