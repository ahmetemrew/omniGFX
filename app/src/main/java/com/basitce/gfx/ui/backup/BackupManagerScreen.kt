package com.basitce.gfx.ui.backup

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupManagerScreen(
    onBack: () -> Unit,
    viewModel: BackupManagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var profileMenuExpanded by remember { mutableStateOf(false) }

    val selectedProfile = state.profiles.firstOrNull {
        it.id == state.selectedProfileId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup Manager") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedProfile?.name ?: "Profil seç",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Profil") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { profileMenuExpanded = true }
                )

                DropdownMenu(
                    expanded = profileMenuExpanded,
                    onDismissRequest = { profileMenuExpanded = false }
                ) {
                    state.profiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.name) },
                            onClick = {
                                viewModel.selectProfile(profile.id)
                                profileMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row {
                Button(onClick = { viewModel.loadBackupsForSelectedProfile() }) {
                    Text("Yenile")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = { viewModel.cleanOldBackups(3) }) {
                    Text("Eski Backupları Temizle")
                }
            }

            state.message?.let { message ->
                Text(text = message)
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            if (state.backups.isEmpty() && !state.isLoading) {
                Text("Backup bulunamadı.")
            }

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.backups) { item ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.entry.fileName,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Target: ${item.targetPath}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        item.entry.timestamp?.let { timestamp ->
                            val dateFormat = SimpleDateFormat(
                                "dd.MM.yyyy HH:mm",
                                Locale.getDefault()
                            )

                            Text(
                                text = "Date: ${dateFormat.format(Date(timestamp))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row {
                            Button(
                                onClick = { viewModel.restoreBackup(item) }
                            ) {
                                Text("Restore")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { viewModel.deleteBackup(item) }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
