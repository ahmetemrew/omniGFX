package com.basitce.gfx.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RemotePathPickerDialog(
    initialPath: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    viewModel: RemoteBrowserViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val startPath = if (initialPath.isNullOrBlank()) {
            "/"
        } else if (initialPath.contains("/")) {
            initialPath.substringBeforeLast('/').ifBlank { "/" }
        } else {
            "/"
        }

        viewModel.load(startPath)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remote Path Seç") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Current: ${state.currentPath}",
                    style = MaterialTheme.typography.bodySmall
                )

                Row {
                    OutlinedButton(onClick = { viewModel.goUp() }) {
                        Text("Üst Dizin")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(onClick = { viewModel.selectCurrentDirectory() }) {
                        Text("Bu Dizini Seç")
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.entries) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onEntryClick(entry) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (entry.isDirectory) "📁" else "📄",
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Column {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = entry.path,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                state.selectedFilePath?.let { selected ->
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Selected: $selected",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = state.selectedFilePath != null,
                onClick = {
                    state.selectedFilePath?.let { onConfirm(it) }
                }
            ) {
                Text("Seç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
