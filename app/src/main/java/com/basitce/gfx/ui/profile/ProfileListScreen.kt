package com.basitce.gfx.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.model.ProfileWithSchema
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniTopBar
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import com.basitce.gfx.presentation.profile.ProfileListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    onLoadProfile: (gameId: String, profileId: String) -> Unit,
    onOpenBackupManager: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    onOpenSync: () -> Unit = {},
    onOpenShizukuSetup: () -> Unit = {},
    viewModel: ProfileListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OmniTopBar(
                title = "Profiller",
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Backups") },
                            onClick = {
                                onOpenBackupManager()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Marketplace") },
                            onClick = {
                                onOpenMarketplace()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sync") },
                            onClick = {
                                onOpenSync()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shizuku Ayarı") },
                            onClick = {
                                onOpenShizukuSetup()
                                menuExpanded = false
                            }
                        )
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
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                state.profiles.isEmpty() -> {
                    OmniEmptyState(
                        title = "Henüz profil yok",
                        description = "Bir oyunun ayarlarını kaydettiğinde burada listelenecek. Her oyun için istediğin kadar profil oluşturabilirsin.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.profiles, key = { it.profile.id }) { profileWithSchema ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteProfile(profileWithSchema.profile.id)
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(OmniError, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White)
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                ProfileCard(
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
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Hata") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileWithSchema,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val isManual = profile.profile.isManual

    OmniCard(
        modifier = Modifier.clickable(onClick = onLoad)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = profile.gameName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ─── TİP ROZETİ (BADGE) ───────────────────────
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isManual) OmniWarning.copy(alpha = 0.15f) else OmniPrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isManual) "HAM DOSYA" else "DİNAMİK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isManual) OmniWarning else OmniPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.padding(start = 8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
