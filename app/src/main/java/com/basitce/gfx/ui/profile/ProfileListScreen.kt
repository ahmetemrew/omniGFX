package com.basitce.gfx.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.model.ProfileWithSchema
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTabHeader
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.presentation.profile.ProfileListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    onLoadProfile: (gameId: String, profileId: String) -> Unit,
    onOpenBackupManager: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    onOpenSync: () -> Unit = {},
    onOpenShizukuSetup: () -> Unit = {},
    onAddGame: () -> Unit = {},
    viewModel: ProfileListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            OmniTabHeader(
                title = "Profiller",
                subtitle = "Kayıtlı ayar profillerin",
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Diğer işlemler")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(OmniRadius.md),
                        containerColor = colors.surfaceContainer
                    ) {
                        MenuItem("Yedekler") { onOpenBackupManager(); menuExpanded = false }
                        MenuItem("Profil analizi") { onOpenMarketplace(); menuExpanded = false }
                        MenuItem("İçe / dışa aktar") { onOpenSync(); menuExpanded = false }
                        MenuItem("Shizuku ayarı") { onOpenShizukuSetup(); menuExpanded = false }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> OmniLoading(
                    label = "Profiller yükleniyor…",
                    modifier = Modifier.align(Alignment.Center)
                )

                state.profiles.isEmpty() -> OmniEmptyState(
                    title = "Henüz profil yok",
                    description = "Bir oyunun ayarlarını kaydettiğinde burada görünür. " +
                        "Her oyun için istediğin kadar profil oluşturabilirsin.",
                    icon = Icons.Outlined.Tune,
                    action = {
                        OmniPrimaryButton(text = "Oyun ekle", onClick = onAddGame)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = spacing.screen,
                        vertical = spacing.lg
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.listItem)
                ) {
                    items(state.profiles, key = { it.profile.id }) { profileWithSchema ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteProfile(profileWithSchema.profile.id)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            colors.errorContainer,
                                            RoundedCornerShape(OmniRadius.md)
                                        )
                                        .padding(horizontal = spacing.xl),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "Sil",
                                        tint = colors.onErrorContainer
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier.animateItem()
                        ) {
                            ProfileRow(
                                profile = profileWithSchema,
                                onLoad = {
                                    onLoadProfile(
                                        profileWithSchema.gameId,
                                        profileWithSchema.profile.id
                                    )
                                },
                                onDelete = {
                                    viewModel.deleteProfile(profileWithSchema.profile.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            shape = RoundedCornerShape(OmniRadius.lg),
            containerColor = colors.surfaceContainerHigh,
            title = {
                Text("Profil işlemi tamamlanamadı", style = MaterialTheme.typography.titleLarge)
            },
            text = { Text(error, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("Tamam") }
            }
        )
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        onClick = onClick
    )
}

@Composable
private fun ProfileRow(
    profile: ProfileWithSchema,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    val isManual = profile.profile.isManual
    var menuExpanded by remember { mutableStateOf(false) }

    val fileName = profile.profile.targetFilePath.substringAfterLast('/')
    val variableCount = remember(profile.profile.userValuesJson) {
        runCatching { org.json.JSONObject(profile.profile.userValuesJson).length() }.getOrDefault(0)
    }
    val fileSummary = when {
        fileName.isBlank() -> null
        isManual -> fileName
        else -> "$fileName · $variableCount değişken"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .background(colors.surfaceContainerLow, RoundedCornerShape(OmniRadius.md))
            .clickable(onClick = onLoad)
            .padding(start = spacing.lg, end = spacing.xs, top = spacing.md, bottom = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Text(
                text = profile.profile.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = profile.gameName,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (fileSummary != null) {
                Text(
                    text = fileSummary,
                    style = OmniTheme.mono.micro,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OmniStatusChip(
                text = if (isManual) "Ham dosya" else "Dinamik",
                tone = if (isManual) OmniTone.Warning else OmniTone.Success
            )
        }

        // Birincil eylem görünür: profil kartının işi "uygula".
        OmniPrimaryButton(
            text = "Uygula",
            onClick = onLoad,
            fillWidth = false,
            compact = true
        )

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "${profile.profile.name} profil işlemleri",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(OmniRadius.md),
                containerColor = colors.surfaceContainer
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Sil",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.error
                        )
                    },
                    onClick = { onDelete(); menuExpanded = false }
                )
            }
        }
    }
}
