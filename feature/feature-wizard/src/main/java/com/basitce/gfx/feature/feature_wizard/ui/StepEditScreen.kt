package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.workflow.DetectedFormat
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
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
    val spacing = OmniTheme.spacing

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.configNodes.size} satır, ${state.detectedFormat.label} biçimi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = state.canUndo
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Geri al",
                                tint = if (state.canUndo) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = state.canRedo
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Redo,
                                contentDescription = "İleri al",
                                tint = if (state.canRedo) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { showDiffPreview = true }) {
                            Icon(
                                Icons.Outlined.Visibility,
                                contentDescription = "Değişiklikleri önizle",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Satır, anahtar veya değer ara",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = "Aramayı temizle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(OmniRadius.sm)
                )

                if (state.hasUnsavedChanges) {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    OmniStatusChip(
                        text = "Kaydedilmemiş değişiklik var",
                        tone = OmniTone.Warning
                    )
                }
            }
        }

        state.error?.let { error ->
            OmniErrorCard(
                title = "Düzenleme sorunu",
                message = error,
                modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.sm)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            items(filteredNodes, key = { it.id }) { node ->
                ConfigLineItem(
                    node = node,
                    structuralEnabled = state.detectedFormat in listOf(
                        DetectedFormat.INI,
                        DetectedFormat.PLAIN_TEXT
                    ),
                    onValueChange = { viewModel.updateNodeValue(node.id, it) },
                    onPinClick = { showPinSheetFor = node },
                    onDelete = { viewModel.deleteNode(node.id) },
                    onMoveUp = { viewModel.moveNode(node.id, MoveDirection.UP) },
                    onMoveDown = { viewModel.moveNode(node.id, MoveDirection.DOWN) },
                    onAddBelow = { viewModel.addNodeAfter(node.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = spacing.screen, vertical = spacing.md)
                    .navigationBarsPadding()
            ) {
                OmniPrimaryButton(
                    text = "Değişkenleri ayarla",
                    onClick = { viewModel.proceedToStep4() }
                )
            }
        }
    }

    if (showDiffPreview) {
        DiffPreviewSheet(
            originalContent = state.fileContent,
            modifiedContent = runCatching { viewModel.getSerializedContent() }.getOrElse { "İçerik oluşturulamadı: ${it.message}" },
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

/**
 * Config satırı: veri öğesi olduğu için 8dp ve kenarlıksız zemin. Kenarlık yalnızca
 * durumu anlatır — sabitlenmiş = petrol, değişmiş = pirinç. Satır içi eylemler
 * ikinci satırda metin butonu olarak durur ki dokunma hedefleri ≥48dp kalsın.
 */
@Composable
private fun ConfigLineItem(
    node: ConfigNode,
    structuralEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onPinClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddBelow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localValue by remember(node.value) { mutableStateOf(node.value) }
    val spacing = OmniTheme.spacing

    val isPinned = node.isPinned
    val isCommentOrBlank = node.type == NodeType.COMMENT ||
        node.type == NodeType.BLANK ||
        node.type == NodeType.SECTION_HEADER

    val borderColor = when {
        isPinned -> MaterialTheme.colorScheme.primary
        node.isModified -> MaterialTheme.colorScheme.secondary
        else -> null
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(OmniRadius.sm),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (borderColor != null) Modifier.border(
                    1.dp,
                    borderColor,
                    RoundedCornerShape(OmniRadius.sm)
                ) else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${node.lineNumber}",
                    style = OmniTheme.mono.micro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.width(28.dp)
                )

                if (!isCommentOrBlank) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.key,
                            style = OmniTheme.mono.key,
                            color = if (isPinned) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
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
                            textStyle = OmniTheme.mono.key,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = if (isPinned) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(OmniRadius.sm)
                        )
                    }
                } else {
                    Text(
                        text = node.rawLine.ifBlank { "Boş satır" },
                        style = OmniTheme.mono.key,
                        color = when (node.type) {
                            NodeType.COMMENT -> MaterialTheme.colorScheme.onSurfaceVariant
                            NodeType.SECTION_HEADER -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = spacing.sm),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onPinClick) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin
                            else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Sabiti kaldır" else "Sabitle",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.xxs),
                horizontalArrangement = Arrangement.End
            ) {
                RowAction("Yukarı", structuralEnabled, onMoveUp)
                RowAction("Aşağı", structuralEnabled, onMoveDown)
                RowAction("Altına ekle", structuralEnabled, onAddBelow)
                RowAction("Sil", structuralEnabled, onDelete, destructive = true)
            }
        }
    }
}

@Composable
private fun RowAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = OmniTheme.spacing.sm, vertical = OmniTheme.spacing.xs),
        modifier = Modifier.heightIn(min = OmniTheme.spacing.touchTarget)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                destructive -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}