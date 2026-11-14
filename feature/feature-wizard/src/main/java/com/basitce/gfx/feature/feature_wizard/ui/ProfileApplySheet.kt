package com.basitce.gfx.feature.feature_wizard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_ui.components.OmniDropdown
import com.basitce.gfx.core.core_ui.components.OmniErrorCard
import com.basitce.gfx.core.core_ui.components.OmniLoading
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniSlider
import com.basitce.gfx.core.core_ui.components.OmniSwitch
import com.basitce.gfx.core.core_ui.components.OmniValueReadout
import com.basitce.gfx.core.core_ui.theme.OmniRadius
import com.basitce.gfx.core.core_ui.theme.OmniTheme
import com.basitce.gfx.feature.feature_wizard.viewmodel.ProfileApplyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileApplySheet(
    profile: ProfileEntity,
    onDismiss: () -> Unit,
    viewModel: ProfileApplyViewModel = hiltViewModel()
) {
    val state by viewModel.applyState.collectAsStateWithLifecycle()
    val components by viewModel.components.collectAsStateWithLifecycle()
    val values by viewModel.values.collectAsStateWithLifecycle()
    val initialValues by viewModel.initialValues.collectAsStateWithLifecycle()
    val spacing = OmniTheme.spacing

    LaunchedEffect(profile.id) { viewModel.prepare(profile) }

    ModalBottomSheet(
        onDismissRequest = { if (!state.isApplying) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = OmniRadius.lg, topEnd = OmniRadius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profili uygula",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(spacing.xxs))
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(spacing.xxl))

            when {
                state.isApplying -> {
                    OmniLoading(
                        label = state.progressMessage.ifBlank { "Uygulanıyor…" }
                    )
                }

                state.isSuccess == true -> {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(OmniRadius.xl))
                            .background(OmniTheme.semantic.successContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = OmniTheme.semantic.onSuccessContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.lg))
                    Text(
                        text = "Profil uygulandı",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    state.message?.let { msg ->
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.xxl))
                    OmniPrimaryButton(text = "Kapat", onClick = onDismiss)
                }

                state.isSuccess == false -> {
                    OmniErrorCard(
                        title = "Profil uygulanamadı",
                        message = state.message ?: "Beklenmeyen bir sorun oluştu. Tekrar deneyebilirsin."
                    )
                    Spacer(modifier = Modifier.height(spacing.xxl))
                    OmniPrimaryButton(
                        text = "Tekrar dene",
                        onClick = { viewModel.applyProfile(profile) }
                    )
                    Spacer(modifier = Modifier.height(spacing.md))
                    OmniSecondaryButton(text = "Vazgeç", onClick = onDismiss)
                }

                else -> {
                    val typeText = if (profile.isManual) {
                        "Ham dosya profili. Dosya olduğu gibi hedefe yazılacak."
                    } else {
                        "Dinamik profil. Seçtiğin değerler dosyaya tek tek uygulanacak."
                    }
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (profile.targetFilePath.isNotBlank()) {
                        Spacer(modifier = Modifier.height(spacing.md))
                        Text(
                            text = profile.targetFilePath,
                            style = OmniTheme.mono.micro,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!profile.isManual) components.forEach { component ->
                        val path = component.optString("path", component.optString("id"))
                        val label = component.optString("label", path)
                        val value = values[path].orEmpty()
                        Spacer(Modifier.height(spacing.lg))
                        when (component.optString("type")) {
                            "slider" -> {
                                val min = component.optDouble("min", 0.0).toFloat()
                                val max = component.optDouble("max", 100.0).toFloat()
                                val step = component.optDouble("step", 1.0).toFloat().coerceAtLeast(0.001f)
                                if (min.isFinite() && max.isFinite() && max > min) {
                                    val current = (value.toFloatOrNull() ?: min).coerceIn(min, max)
                                    OmniValueReadout(
                                        label = label,
                                        value = value.ifBlank { current.toString() },
                                        previousValue = initialValues[path],
                                        fraction = (current - min) / (max - min),
                                        compact = true
                                    )
                                    Spacer(Modifier.height(spacing.sm))
                                    OmniSlider(
                                        label, current,
                                        { selected ->
                                            val snapped = (min + kotlin.math.round((selected - min) / step) * step).coerceIn(min, max)
                                            viewModel.updateValue(path, if (snapped % 1f == 0f) snapped.toInt().toString() else snapped.toString())
                                        },
                                        valueRange = min..max,
                                        valueLabel = value
                                    )
                                }
                            }
                            "toggle" -> {
                                val checked = component.optString("checkedValue", "1")
                                val unchecked = component.optString("uncheckedValue", "0")
                                OmniSwitch(label, value == checked, { viewModel.updateValue(path, if (it) checked else unchecked) })
                            }
                            "dropdown" -> {
                                val array = component.optJSONArray("options") ?: org.json.JSONArray()
                                val options = (0 until array.length()).map { array.getString(it) }
                                val mapping = component.optJSONObject("valueMapping")
                                val selected = options.firstOrNull { mapping?.optString(it, it) == value } ?: value
                                OmniDropdown(label, options, selected, { viewModel.updateValue(path, mapping?.optString(it, it) ?: it) })
                            }
                            else -> OutlinedTextField(
                                value = value,
                                onValueChange = { viewModel.updateValue(path, it) },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(OmniRadius.sm),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(spacing.xxl))

                    OmniPrimaryButton(
                        text = "Uygula",
                        onClick = { viewModel.applyProfile(profile) },
                        icon = Icons.Outlined.PlayArrow
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))
        }
    }
}