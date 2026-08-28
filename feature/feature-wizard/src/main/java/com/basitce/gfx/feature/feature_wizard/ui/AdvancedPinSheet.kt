package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basitce.gfx.core.core_ui.theme.OmniError
import com.basitce.gfx.core.core_ui.theme.OmniOnPrimary
import com.basitce.gfx.core.core_ui.theme.OmniOnSurface
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniOutline
import com.basitce.gfx.core.core_ui.theme.OmniPrimary
import com.basitce.gfx.core.core_ui.theme.OmniSurface
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
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
        containerColor = OmniSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Değişken Yapılandır",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnSurface
                    )
                    Text(
                        text = node.path,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = OmniOnSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (currentVariable != null) {
                    IconButton(onClick = onRemovePin) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Pini Kaldır",
                            tint = OmniError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Kullanıcı Etiketi") },
                placeholder = { Text("örn: FPS Sınırı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = pinTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                placeholder = { Text("örn: Oyun içi maksimum kare hızı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = pinTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedType.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Arayüz Bileşeni") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = pinTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    UiComponentType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName()) },
                            onClick = {
                                selectedType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedType) {
                UiComponentType.SLIDER -> {
                    Text(
                        text = "Slider Aralığı",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = min,
                            onValueChange = { min = it },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = max,
                            onValueChange = { max = it },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = step,
                            onValueChange = { step = it },
                            label = { Text("Step") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                UiComponentType.DROPDOWN -> {
                    Text(
                        text = "Eşleştirme Tablosu",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OmniOnSurface
                    )
                    Text(
                        text = "Kullanıcı sol tarafı seçer, dosyaya sağ taraf yazılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OmniOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(mappings.entries.toList(), key = { it.key }) { entry ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = OmniOnSurfaceVariant,
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
                                    shape = RoundedCornerShape(8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        mappings = mappings.toMutableMap().apply {
                                            remove(entry.key)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Sil",
                                        tint = OmniError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newOptionLabel,
                            onValueChange = { newOptionLabel = it },
                            placeholder = { Text("Görünen") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = newOptionValue,
                            onValueChange = { newOptionValue = it },
                            placeholder = { Text("Dosya değeri") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = pinTextFieldColors(),
                            shape = RoundedCornerShape(8.dp)
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
                                Icons.Default.Add,
                                contentDescription = "Ekle",
                                tint = OmniPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                UiComponentType.TOGGLE -> {
                    Text(
                        text = "Aç/Kapa: 1 = Açık, 0 = Kapalı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OmniOnSurfaceVariant
                    )
                }

                UiComponentType.TEXT_INPUT -> {
                    Text(
                        text = "Serbest metin girişi. Kullanıcı istediği değeri yazabilir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OmniOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
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
                enabled = label.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OmniPrimary)
            ) {
                Text(
                    text = "Değişkeni Kaydet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = OmniOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun UiComponentType.displayName(): String {
    return when (this) {
        UiComponentType.SLIDER -> "Slider (Kaydırıcı)"
        UiComponentType.DROPDOWN -> "Dropdown (Açılır Liste)"
        UiComponentType.TOGGLE -> "Toggle (Aç/Kapa)"
        UiComponentType.TEXT_INPUT -> "Metin Girişi"
    }
}

@Composable
private fun pinTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = OmniSurfaceElevated,
    unfocusedContainerColor = OmniSurfaceElevated,
    focusedBorderColor = OmniPrimary,
    unfocusedBorderColor = OmniOutline,
    focusedTextColor = OmniOnSurface,
    unfocusedTextColor = OmniOnSurface,
    focusedLabelColor = OmniPrimary,
    unfocusedLabelColor = OmniOnSurfaceVariant,
    cursorColor = OmniPrimary
)
