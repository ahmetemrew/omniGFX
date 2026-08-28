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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import com.basitce.gfx.feature.feature_wizard.viewmodel.GameDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    Scaffold(
        containerColor = OmniBackground,
        floatingActionButton = {
            packageName?.let { pkg ->
                FloatingActionButton(
                    onClick = { onNewProfile(pkg) },
                    containerColor = OmniPrimary,
                    contentColor = OmniOnPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Yeni Profil")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = OmniOnSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = game?.name ?: "Oyun",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OmniOnSurface
                    )
                    Text(
                        text = game?.packageName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniOnSurfaceVariant
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
                        description = "Sağ alttaki + butonuna basarak yeni bir profil oluşturun."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Kayıtlı Profiller (${profiles.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OmniOnSurface
                        )
                    }
                    items(profiles, key = { it.id }) { profile ->
                        ProfileCard(
                            profile = profile,
                            onApply = { viewModel.requestApply(profile) },
                            onDelete = { viewModel.requestDelete(profile) }
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
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = OmniError,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Profili Sil",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OmniOnSurface
                )
            },
            text = {
                Text(
                    "\"${profile.name}\" profili kalıcı olarak silinecek. Bu işlem geri alınamaz.",
                    color = OmniOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = OmniError)
                ) {
                    Text("Sil", color = androidx.compose.ui.graphics.Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("İptal", color = OmniOnSurfaceVariant)
                }
            },
            containerColor = OmniSurface
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
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val isManual = profile.isManual

    OmniCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OmniOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.targetFilePath.substringAfterLast('/'),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = OmniOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(profile.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = OmniOnSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            val (badgeText, badgeColor) = if (isManual) {
                "HAM DOSYA" to OmniWarning
            } else {
                "DİNAMİK" to OmniSuccess
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onApply) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Uygula",
                    tint = OmniPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = OmniError,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
