package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.ConfigNode
import com.basitce.gfx.feature.feature_wizard.viewmodel.PinnedVariable
import com.basitce.gfx.feature.feature_wizard.viewmodel.UiComponentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedPinSheet(
    node: ConfigNode,
    currentVariable: PinnedVariable?,
    onDismiss: () -> Unit,
    onSave: (PinnedVariable) -> Unit,
    onRemovePin: () -> Unit
) {
    val spacing = OmniTheme.spacing

    var selectedType by remember(currentVariable) {
        mutableStateOf(currentVariable?.uiComponentType ?: UiComponentType.SLIDER)
    }
    var label by remember(currentVariable) {
        mutableStateOf(currentVariable?.label ?: node.key)
    }
    var description by remember(currentVariable) {
        mutableStateOf(currentVariable?.description ?: "")
    }
    var min by remember(currentVariable) {
        mutableStateOf(currentVariable?.min?.toString() ?: "0")
    }
    var max by remember(currentVariable) {
        mutableStateOf(currentVariable?.max?.toString() ?: "100")
    }
    var step by remember(currentVariable) {
        mutableStateOf(currentVariable?.step?.toString() ?: "1")
    }
    var mappings by remember(currentVariable) {
        mutableStateOf<Map<String, String>>(
            currentVariable?.valueMapping
                ?: mapOf("Düşük" to "0", "Orta" to "1", "Yüksek" to "2")
        )
    }
    var newOptionLabel by remember { mutableStateOf("") }
    var newOptionValue by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = OmniRadius.lg, topEnd = OmniRadius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Değişkeni yapılandır",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = node.path,
                        style = OmniTheme.mono.micro,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (currentVariable != null) {
                    IconButton(onClick = onRemovePin) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Sabiti kaldır",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Görünen ad") },
                placeholder = { Text("örn: FPS sınırı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = pinTextFieldColors(),
                shape = RoundedCornerShape(OmniRadius.sm)
            )

            Spacer(modifier = Modifier.height(spacing.listItem))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                placeholder = { Text("örn: Oyun içi maksimum kare hızı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = pinTextFieldColors(),
                shape = RoundedCornerShape(OmniRadius.sm)
            )

            Spacer(modifier = Modifier.height(spacing.xxl))

            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedType.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Arayüz bileşeni") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                    colors = pinTextFieldColors(),
                    shape = RoundedCornerShape(OmniRadius.sm)
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    UiComponentType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                selectedType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            when (selectedType) {
                UiComponentType.SLIDER -> {
                    Text(
                        text = "Kaydırıcı aralığı",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        OutlinedTextField(
                            value = min,
                            onValueChange = { min = it },
                            label = { Text("En az") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(OmniRadius.sm)
                        )
                        OutlinedTextField(
                            value = max,
                            onValueChange = { max = it },
                            label = { Text("En çok") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(OmniRadius.sm)
                        )
                        OutlinedTextField(
                            value = step,
                            onValueChange = { step = it },
                            label = { Text("Adım") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(OmniRadius.sm)
                        )
                    }
                }

                UiComponentType.DROPDOWN -> {
                    Text(
                        text = "Eşleştirme tablosu",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Kullanıcı sol tarafı seçer, dosyaya sağ taraf yazılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(spacing.listItem))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(mappings.entries.toList(), key = { it.key }) { entry ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = entry.key,
                                    onValueChange = {},
                                    label = { Text("Görünen") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    readOnly = true,
                                    colors = pinTextFieldColors(),
                                    shape = RoundedCornerShape(OmniRadius.sm)
                                )
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                OutlinedTextField(
                                    value = entry.value,
                                    onValueChange = { newVal ->
                                        mappings = mappings.toMutableMap().apply {
                                            this[entry.key] = newVal
                                        }
                                    },
                                    label = { Text("Dosyaya") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = pinTextFieldColors(),
                                    shape = RoundedCornerShape(OmniRadius.sm)
                                )
                                IconButton(
                                    onClick = {
                                        mappings = mappings.toMutableMap().apply {
                                            remove(entry.key)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "Seçeneği sil",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.sm))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newOptionLabel,
                            onValueChange = { newOptionLabel = it },
                            placeholder = { Text("Görünen") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(OmniRadius.sm)
                        )
                        OutlinedTextField(
                            value = newOptionValue,
                            onValueChange = { newOptionValue = it },
                            placeholder = { Text("Dosya değeri") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(OmniRadius.sm)
                        )
                        IconButton(
                            onClick = {
                                if (newOptionLabel.isNotBlank() && newOptionValue.isNotBlank()) {
                                    mappings = mappings.toMutableMap().apply {
                                        this[newOptionLabel] = newOptionValue
                                    }
                                    newOptionLabel = ""
                                    newOptionValue = ""
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Seçenek ekle",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                UiComponentType.TOGGLE -> {
                    Text(
                        text = "Aç/kapa: 1 = açık, 0 = kapalı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                UiComponentType.TEXT_INPUT -> {
                    Text(
                        text = "Serbest metin girişi. Kullanıcı istediği değeri yazabilir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            OmniPrimaryButton(
                text = "Değişkeni kaydet",
                onClick = {
                    val variable = PinnedVariable(
                        path = node.path,
                        uiComponentType = selectedType,
                        label = label,
                        description = description,
                        min = min.toFloatOrNull() ?: 0f,
                        max = max.toFloatOrNull() ?: 100f,
                        step = step.toFloatOrNull() ?: 1f,
                        options = mappings.keys.toList(),
                        valueMapping = mappings,
                        defaultValue = node.value
                    )
                    onSave(variable)
                },
                enabled = label.isNotBlank()
            )

            Spacer(modifier = Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun pinTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary
)