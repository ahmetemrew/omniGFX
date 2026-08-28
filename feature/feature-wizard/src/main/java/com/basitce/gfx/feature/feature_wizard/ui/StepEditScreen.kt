package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniBackground
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniOutline
import com.basitce.gfx.core.core_ui.theme.OmniOutlineStrong
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSuccess
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import com.basitce.gfx.feature.feature_wizard.viewmodel.ConfigNode
import com.basitce.gfx.feature.feature_wizard.viewmodel.MoveDirection
import com.basitce.gfx.feature.feature_wizard.viewmodel.NodeType
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel

@Composable
fun StepEdit(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDiffPreview by remember { mutableStateOf(false) }
    var showPinSheetFor by remember { mutableStateOf<ConfigNode?>(null) }

    val filteredNodes = remember(state.configNodes, searchQuery) {
        if (searchQuery.isBlank()) state.configNodes
        else state.configNodes.filter {
            it.key.contains(searchQuery, ignoreCase = true) ||
                it.value.contains(searchQuery, ignoreCase = true) ||
                it.rawLine.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBackground)
    ) {
        Surface(
            color = OmniSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Akıllı Config Editörü",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = OmniOnSurface
                        )
                        Text(
                            text = "${state.configNodes.size} satır • ${state.detectedFormat.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OmniOnSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = viewModel.canUndo
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Geri Al",
                                tint = if (viewModel.canUndo) OmniOnSurface
                                    else OmniOnSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = viewModel.canRedo
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "İleri Al",
                                tint = if (viewModel.canRedo) OmniOnSurface
                                    else OmniOnSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { showDiffPreview = true }) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "Diff Önizleme",
                                tint = OmniPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Satır, anahtar veya değer ara...",
                            color = OmniOnSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = OmniOnSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Temizle",
                                    tint = OmniOnSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OmniSurfaceElevated,
                        unfocusedContainerColor = OmniSurfaceElevated,
                        focusedBorderColor = OmniPrimary,
                        unfocusedBorderColor = OmniOutline,
                        focusedTextColor = OmniOnSurface,
                        unfocusedTextColor = OmniOnSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.hasUnsavedChanges) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "● Kaydedilmemiş değişiklikler var",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = OmniWarning
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredNodes, key = { it.id }) { node ->
                ConfigLineItem(
                    node = node,
                    onValueChange = { viewModel.updateNodeValue(node.id, it) },
                    onPinClick = { showPinSheetFor = node },
                    onDelete = { viewModel.deleteNode(node.id) },
                    onMoveUp = { viewModel.moveNode(node.id, MoveDirection.UP) },
                    onMoveDown = { viewModel.moveNode(node.id, MoveDirection.DOWN) },
                    onAddBelow = { viewModel.addNodeAfter(node.id) }
                )
            }
        }

        Surface(
            color = OmniSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { viewModel.proceedToStep4() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
                ) {
                    Text(
                        text = "Değişkenleri Yapılandır & Kaydet →",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnPrimary
                    )
                }
            }
        }
    }

    if (showDiffPreview) {
        DiffPreviewSheet(
            originalContent = state.fileContent,
            modifiedContent = viewModel.getSerializedContent(),
            onDismiss = { showDiffPreview = false }
        )
    }

    showPinSheetFor?.let { node ->
        val currentVar = state.pinnedVariables[node.path]
        AdvancedPinSheet(
            node = node,
            currentVariable = currentVar,
            onDismiss = { showPinSheetFor = null },
            onSave = { variable ->
                viewModel.savePinnedVariable(variable)
                showPinSheetFor = null
            },
            onRemovePin = {
                viewModel.removePin(node.path)
                showPinSheetFor = null
            }
        )
    }
}

@Composable
private fun ConfigLineItem(
    node: ConfigNode,
    onValueChange: (String) -> Unit,
    onPinClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddBelow: () -> Unit
) {
    var localValue by remember(node.value) { mutableStateOf(node.value) }

    val isPinned = node.isPinned
    val isCommentOrBlank = node.type == NodeType.COMMENT ||
        node.type == NodeType.BLANK ||
        node.type == NodeType.SECTION_HEADER

    val bgColor = when {
        isPinned -> OmniPrimary.copy(alpha = 0.08f)
        node.isModified -> OmniWarning.copy(alpha = 0.05f)
        node.type == NodeType.COMMENT -> OmniSurface.copy(alpha = 0.5f)
        node.type == NodeType.BLANK -> OmniSurface.copy(alpha = 0.3f)
        else -> OmniSurface
    }

    val borderColor = when {
        isPinned -> OmniPrimary
        node.isModified -> OmniWarning.copy(alpha = 0.5f)
        node.type == NodeType.SECTION_HEADER -> OmniPrimary.copy(alpha = 0.3f)
        else -> OmniOutline
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${node.lineNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = OmniOnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.width(32.dp)
                )

                if (!isCommentOrBlank) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.key,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (isPinned) OmniPrimary else OmniOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        OutlinedTextField(
                            value = localValue,
                            onValueChange = {
                                localValue = it
                                onValueChange(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = OmniBackground,
                                unfocusedContainerColor = OmniBackground,
                                focusedBorderColor = if (isPinned) OmniPrimary
                                    else OmniOutlineStrong,
                                unfocusedBorderColor = OmniOutline,
                                focusedTextColor = OmniOnSurface,
                                unfocusedTextColor = OmniOnSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                } else {
                    Text(
                        text = node.rawLine.ifBlank { "↔ Boş Satır" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = when (node.type) {
                            NodeType.COMMENT -> OmniOnSurfaceVariant.copy(alpha = 0.6f)
                            NodeType.SECTION_HEADER -> OmniPrimary
                            else -> OmniOnSurfaceVariant.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(modifier = Modifier.padding(start = 8.dp)) {
                    IconButton(
                        onClick = onPinClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin
                                else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) OmniPrimary else OmniOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onAddBelow,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Altına Ekle",
                            tint = OmniSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = OmniError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!isCommentOrBlank) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onMoveUp,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Yukarı",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    TextButton(
                        onClick = onMoveDown,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Aşağı",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
