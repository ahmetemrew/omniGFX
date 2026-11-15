package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_engine.workflow.DetectedFormat
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniStatusChip
import com.basitce.gfx.core.core_ui.components.OmniTickRail
import com.basitce.gfx.core.core_ui.components.OmniTone
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.ConfigNode
import com.basitce.gfx.feature.feature_wizard.viewmodel.MoveDirection
import com.basitce.gfx.feature.feature_wizard.viewmodel.NodeType
import com.basitce.gfx.feature.feature_wizard.viewmodel.PinnedVariable
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardState
import com.basitce.gfx.feature.feature_wizard.viewmodel.SetupWizardViewModel
import com.basitce.gfx.feature.feature_wizard.viewmodel.UiComponentType

@Composable
fun StepEdit(
    state: SetupWizardState,
    viewModel: SetupWizardViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDiffPreview by remember { mutableStateOf(false) }
    var showPinSheetFor by remember { mutableStateOf<ConfigNode?>(null) }
    var editingNodeId by remember { mutableStateOf<String?>(null) }
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
                    isEditing = editingNodeId == node.id,
                    pinnedVariable = state.pinnedVariables[node.path],
                    onValueChange = { viewModel.updateNodeValue(node.id, it) },
                    onPinClick = { showPinSheetFor = node },
                    onDelete = { viewModel.deleteNode(node.id) },
                    onMoveUp = { viewModel.moveNode(node.id, MoveDirection.UP) },
                    onMoveDown = { viewModel.moveNode(node.id, MoveDirection.DOWN) },
                    onAddBelow = { viewModel.addNodeAfter(node.id) },
                    onStartEdit = { editingNodeId = node.id },
                    onEndEdit = { editingNodeId = null },
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
 * Config satırı = Değer Okuması (İMZA ANI 1). Sayısal değerler alet okuması olarak
 * çizilir: anahtar solda, eski değer üstü çizili + yeni değer pirinç, altında tik rayı.
 * Okumaya dokununca satır yerinde düzenleme alanına dönüşür. Sıralama/silme gibi
 * yapısal eylemler her kartta tekrarlanan metin dizisi yerine taşma menüsündedir.
 */
@Composable
private fun ConfigLineItem(
    node: ConfigNode,
    structuralEnabled: Boolean,
    isEditing: Boolean,
    pinnedVariable: PinnedVariable?,
    onValueChange: (String) -> Unit,
    onPinClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddBelow: () -> Unit,
    onStartEdit: () -> Unit,
    onEndEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme

    val isKeyValue = node.type == NodeType.KEY_VALUE
    val isNumeric = isKeyValue && node.value.toFloatOrNull() != null
    val changed = isKeyValue && node.originalValue.isNotEmpty() &&
        node.originalValue != node.value

    val borderColor = when {
        node.isPinned -> colors.primary
        node.isModified -> colors.secondary
        else -> null
    }

    Surface(
        color = colors.surfaceContainerLow,
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
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.width(28.dp)
                )

                if (isKeyValue) {
                    Text(
                        text = node.key,
                        style = OmniTheme.mono.key,
                        color = if (node.isPinned) colors.primary else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onPinClick) {
                        Icon(
                            imageVector = if (node.isPinned) Icons.Filled.PushPin
                                else Icons.Outlined.PushPin,
                            contentDescription = if (node.isPinned) "Sabiti kaldır" else "Sabitle",
                            tint = if (node.isPinned) colors.primary else colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Text(
                        text = node.rawLine.ifBlank { "Boş satır" },
                        style = OmniTheme.mono.key,
                        color = when (node.type) {
                            NodeType.COMMENT -> colors.onSurfaceVariant
                            NodeType.SECTION_HEADER -> colors.primary
                            else -> colors.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (structuralEnabled) {
                    RowActionsMenu(
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown,
                        onAddBelow = onAddBelow,
                        onDelete = onDelete
                    )
                }
            }

            if (isKeyValue) {
                if (isEditing) {
                    ValueEditor(
                        node = node,
                        onValueChange = onValueChange,
                        onEndEdit = onEndEdit
                    )
                } else {
                    ValueReadoutRow(
                        node = node,
                        isNumeric = isNumeric,
                        changed = changed,
                        pinnedVariable = pinnedVariable,
                        onStartEdit = onStartEdit
                    )
                }
            }
        }
    }
}

/** Salt okunur Değer Okuması: eski → yeni + tik rayı. Dokununca düzenleme açılır. */
@Composable
private fun ValueReadoutRow(
    node: ConfigNode,
    isNumeric: Boolean,
    changed: Boolean,
    pinnedVariable: PinnedVariable?,
    onStartEdit: () -> Unit
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    val semantic = OmniTheme.semantic

    val fraction = if (
        isNumeric &&
        pinnedVariable != null &&
        pinnedVariable.uiComponentType == UiComponentType.SLIDER &&
        pinnedVariable.max > pinnedVariable.min
    ) {
        ((node.value.toFloatOrNull() ?: pinnedVariable.min) - pinnedVariable.min) /
            (pinnedVariable.max - pinnedVariable.min)
    } else null

    val readoutDescription = buildString {
        append(node.key)
        append(": ")
        if (changed) {
            append(node.originalValue)
            append(" yerine ")
        }
        append(node.value)
        append(". Düzenlemek için dokun.")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.xs))
            .clickable(onClick = onStartEdit)
            .semantics { contentDescription = readoutDescription }
            .padding(top = spacing.xxs)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            if (changed) {
                Text(
                    text = node.originalValue,
                    style = OmniTheme.mono.key,
                    color = semantic.readoutMuted,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "›",
                    style = OmniTheme.mono.key,
                    color = colors.onSurfaceVariant
                )
            }
            Text(
                text = node.value.ifBlank { "—" },
                style = if (isNumeric) OmniTheme.mono.readout else OmniTheme.mono.value,
                color = if (changed) colors.secondary else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        if (isNumeric) {
            OmniTickRail(
                fraction = fraction,
                highlight = changed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.xxs)
                    .height(14.dp)
            )
        }
    }
}

/** Yerinde düzenleme: okuma alanı metin alanına dönüşür, onay/vazgeç ile kapanır. */
@Composable
private fun ValueEditor(
    node: ConfigNode,
    onValueChange: (String) -> Unit,
    onEndEdit: () -> Unit
) {
    val spacing = OmniTheme.spacing
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    var startValue by remember(node.id) { mutableStateOf(node.value) }
    var draft by remember(node.id) { mutableStateOf(node.value) }

    LaunchedEffect(node.id) {
        startValue = node.value
        draft = node.value
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = {
                draft = it
                onValueChange(it)
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = OmniTheme.mono.value,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEndEdit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface
            ),
            shape = RoundedCornerShape(OmniRadius.sm)
        )
        IconButton(onClick = onEndEdit) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = "Değeri kaydet",
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = {
                draft = startValue
                onValueChange(startValue)
                onEndEdit()
            }
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Vazgeç",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Yapısal eylemler tek taşma menüsünde: kart başına 4 metin düğmesi tekrarı yok. */
@Composable
private fun RowActionsMenu(
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddBelow: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    IconButton(onClick = { expanded = true }) {
        Icon(
            Icons.Outlined.MoreVert,
            contentDescription = "Satır işlemleri",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        shape = RoundedCornerShape(OmniRadius.md),
        containerColor = colors.surfaceContainer
    ) {
        DropdownMenuItem(
            text = { Text("Yukarı taşı", style = MaterialTheme.typography.bodyMedium) },
            onClick = { onMoveUp(); expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Aşağı taşı", style = MaterialTheme.typography.bodyMedium) },
            onClick = { onMoveDown(); expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Altına satır ekle", style = MaterialTheme.typography.bodyMedium) },
            onClick = { onAddBelow(); expanded = false }
        )
        DropdownMenuItem(
            text = {
                Text(
                    "Sil",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error
                )
            },
            onClick = { onDelete(); expanded = false }
        )
    }
}
