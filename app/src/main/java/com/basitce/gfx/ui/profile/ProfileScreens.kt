package com.basitce.gfx.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_engine.profile.ConfigFormatHint
import com.basitce.gfx.core.core_engine.profile.PatchValueType
import com.basitce.gfx.core.core_engine.profile.ProfileApplyResult
import com.basitce.gfx.presentation.profile.ApplyFlowViewModel
import com.basitce.gfx.presentation.profile.ProfileEditorViewModel
import com.basitce.gfx.ui.browser.RemotePathPickerDialog
import com.basitce.gfx.ui.diff.ConfigDiffPreviewDialog
import com.basitce.gfx.ui.diff.ConfigDiffPreviewViewModel

@Composable
internal fun ExportProfileDialog(
    json: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile JSON") },
        text = {
            SelectionContainer {
                Text(
                    text = json,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(json))
                }
            ) {
                Text("Kopyala")
            }
        }
    )
}

@Composable
internal fun ImportProfileDialog(
    importText: String,
    onImportTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil Import Et") },
        text = {
            OutlinedTextField(
                value = importText,
                onValueChange = onImportTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("JSON") }
            )
        },
        confirmButton = {
            TextButton(onClick = onImport) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    profileId: String?,
    onBack: () -> Unit,
    editorViewModel: ProfileEditorViewModel = hiltViewModel(),
    applyViewModel: ApplyFlowViewModel = hiltViewModel(),
    diffViewModel: ConfigDiffPreviewViewModel = hiltViewModel()
) {
    val state by editorViewModel.uiState.collectAsStateWithLifecycle()
    val pathTestState by editorViewModel.pathTestState.collectAsStateWithLifecycle()
    val applyState by applyViewModel.uiState.collectAsStateWithLifecycle()
    val diffState by diffViewModel.uiState.collectAsStateWithLifecycle()

    var showApplyDialog by remember { mutableStateOf(false) }
    var formatMenuExpanded by remember { mutableStateOf(false) }
    var showPathBrowser by remember { mutableStateOf(false) }
    var showDiffDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profileId) {
        if (profileId == null) {
            editorViewModel.resetForNewProfile()
        } else {
            editorViewModel.loadProfile(profileId)
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            editorViewModel.consumeSavedEvent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Editörü") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = editorViewModel::onNameChange,
                label = { Text("Profil adı") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.packageName,
                onValueChange = editorViewModel::onPackageNameChange,
                label = { Text("Package name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.targetPathTemplate,
                    onValueChange = editorViewModel::onTargetPathTemplateChange,
                    label = { Text("Target path template") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = { showPathBrowser = true }) {
                    Text("Browse")
                }
            }

            if (showPathBrowser) {
                RemotePathPickerDialog(
                    initialPath = state.targetPathTemplate,
                    onDismiss = { showPathBrowser = false },
                    onConfirm = { selectedPath ->
                        editorViewModel.onTargetPathTemplateChange(selectedPath)
                        showPathBrowser = false
                    }
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.format.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Format") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formatMenuExpanded = true }
                )

                DropdownMenu(
                    expanded = formatMenuExpanded,
                    onDismissRequest = { formatMenuExpanded = false }
                ) {
                    ConfigFormatHint.values().forEach { format ->
                        DropdownMenuItem(
                            text = { Text(format.name) },
                            onClick = {
                                editorViewModel.onFormatChange(format)
                                formatMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Patch Listesi",
                style = MaterialTheme.typography.titleMedium
            )

            state.patches.forEach { patch ->
                PatchEditorItem(
                    patch = patch,
                    onPathChange = { editorViewModel.onPatchPathChange(patch.id, it) },
                    onValueChange = { editorViewModel.onPatchValueChange(patch.id, it) },
                    onValueTypeChange = { editorViewModel.onPatchValueTypeChange(patch.id, it) },
                    onRemove = { editorViewModel.removePatch(patch.id) }
                )
            }

            OutlinedButton(onClick = { editorViewModel.addPatch() }) {
                Text("Patch Ekle")
            }

            Text(
                text = "Seçenekler",
                style = MaterialTheme.typography.titleMedium
            )

            OptionSwitch(
                label = "Dry run",
                checked = state.options.dryRun,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(dryRun = checked) }
                }
            )

            OptionSwitch(
                label = "Remote backup",
                checked = state.options.backupRemote,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(backupRemote = checked) }
                }
            )

            OptionSwitch(
                label = "Local backup",
                checked = state.options.backupLocal,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(backupLocal = checked) }
                }
            )

            OptionSwitch(
                label = "Atomic replace",
                checked = state.options.atomicReplace,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(atomicReplace = checked) }
                }
            )

            OptionSwitch(
                label = "Verify after apply",
                checked = state.options.verifyAfterApply,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(verifyAfterApply = checked) }
                }
            )

            OptionSwitch(
                label = "Auto rollback on verification failure",
                checked = state.options.autoRollbackOnVerificationFailure,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions {
                        it.copy(autoRollbackOnVerificationFailure = checked)
                    }
                }
            )

            OptionSwitch(
                label = "Force stop before apply",
                checked = state.options.forceStopBeforeApply,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(forceStopBeforeApply = checked) }
                }
            )

            OptionSwitch(
                label = "Launch after apply",
                checked = state.options.launchAfterApply,
                onCheckedChange = { checked ->
                    editorViewModel.updateOptions { it.copy(launchAfterApply = checked) }
                }
            )

            Row {
                Button(onClick = { editorViewModel.saveProfile() }) {
                    Text("Kaydet")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = { editorViewModel.testPath() }) {
                    Text("Path Test")
                }
            }

            Row {
                Button(
                    onClick = {
                        applyViewModel.applyCurrentEditorProfile(
                            editorViewModel = editorViewModel,
                            dryRunOverride = false
                        )
                        showApplyDialog = true
                    }
                ) {
                    Text("Apply")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        applyViewModel.applyCurrentEditorProfile(
                            editorViewModel = editorViewModel,
                            dryRunOverride = true
                        )
                        showApplyDialog = true
                    }
                ) {
                    Text("Dry Run")
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        diffViewModel.preview(editorViewModel.buildProfile())
                        showDiffDialog = true
                    }
                ) {
                    Text("Diff Preview")
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (pathTestState.isTesting) {
                CircularProgressIndicator()
            }

            pathTestState.error?.let { error ->
                Text(
                    text = "Path test hatası: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            pathTestState.capabilityMessage?.let { message ->
                Text(text = message)
            }

            pathTestState.securityErrors.forEach { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            pathTestState.securityWarnings.forEach { warning ->
                Text(
                    text = warning,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            pathTestState.candidates.forEach { candidate ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = candidate.path,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "exists=${candidate.exists} " +
                            "readable=${candidate.readable} " +
                            "writable=${candidate.writable} " +
                            "parentWritable=${candidate.parentWritable}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showApplyDialog) {
        ApplyDialog(
            state = applyState,
            onDismiss = {
                showApplyDialog = false
                applyViewModel.reset()
            }
        )
    }

    if (showDiffDialog) {
        ConfigDiffPreviewDialog(
            state = diffState,
            onDismiss = {
                showDiffDialog = false
                diffViewModel.reset()
            }
        )
    }
}

@Composable
private fun PatchEditorItem(
    patch: com.basitce.gfx.presentation.profile.ProfilePatchUi,
    onPathChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onValueTypeChange: (PatchValueType) -> Unit,
    onRemove: () -> Unit
) {
    var valueTypeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = patch.path,
            onValueChange = onPathChange,
            label = { Text("Path") },
            modifier = Modifier.fillMaxWidth()
        )

        if (patch.valueType != PatchValueType.NULL) {
            OutlinedTextField(
                value = patch.value,
                onValueChange = onValueChange,
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = patch.valueType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Value Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { valueTypeMenuExpanded = true }
                )

                DropdownMenu(
                    expanded = valueTypeMenuExpanded,
                    onDismissRequest = { valueTypeMenuExpanded = false }
                ) {
                    PatchValueType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                onValueTypeChange(type)
                                valueTypeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Patch sil"
                )
            }
        }
    }
}

@Composable
private fun OptionSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ApplyDialog(
    state: com.basitce.gfx.presentation.profile.ApplyFlowUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Durumu") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isApplying) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = state.lastEvent?.displayName()
                                ?: "İşlem devam ediyor..."
                        )
                    }
                }

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                when (val result = state.result) {
                    is ProfileApplyResult.Success -> {
                        Text("İşlem başarılı.")

                        Text("Path: ${result.selectedPath}")
                        Text("Changed: ${result.changed}")
                        Text("DryRun: ${result.dryRun}")

                        result.remoteBackupPath?.let { backup ->
                            Text("Remote backup: $backup")
                        }

                        result.verification?.let { verification ->
                            Text("Verify success: ${verification.success}")
                            Text("Verify message: ${verification.message}")
                        }

                        if (result.warnings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Warnings:")
                        }

                        result.warnings.forEach { warning ->
                            Text("• $warning")
                        }
                    }

                    is ProfileApplyResult.Failure -> {
                        Text(
                            text = "İşlem başarısız: ${result.message}",
                            color = MaterialTheme.colorScheme.error
                        )

                        if (result.rolledBack) {
                            Text("Otomatik rollback yapıldı.")
                        }

                        result.verification?.let { verification ->
                            Text("Verify success: ${verification.success}")
                            Text("Verify message: ${verification.message}")
                        }

                        if (result.warnings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Warnings:")
                        }

                        result.warnings.forEach { warning ->
                            Text("• $warning")
                        }
                    }

                    null -> Unit
                }

                if (state.events.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "İşlem Adımları",
                        style = MaterialTheme.typography.titleSmall
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.events) { event ->
                            EventTimelineRow(
                                event = event,
                                isCurrent = state.isApplying && event == state.lastEvent
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
internal fun EventTimelineRow(
    event: com.basitce.gfx.core.core_engine.profile.ProfileEngineEvent,
    isCurrent: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrent) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "✓",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = event.displayName(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
