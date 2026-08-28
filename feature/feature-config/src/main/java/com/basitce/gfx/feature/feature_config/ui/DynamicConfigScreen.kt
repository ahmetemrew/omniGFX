package com.basitce.gfx.feature.feature_config.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_ui.components.OmniCard
import com.basitce.gfx.core.core_ui.components.OmniDropdown
import com.basitce.gfx.core.core_ui.components.OmniEmptyState
import com.basitce.gfx.core.core_ui.components.OmniPrimaryButton
import com.basitce.gfx.core.core_ui.components.OmniSecondaryButton
import com.basitce.gfx.core.core_ui.components.OmniSlider
import com.basitce.gfx.core.core_ui.components.OmniSwitch
import com.basitce.gfx.core.core_ui.theme.OmniOnSurfaceVariant
import com.basitce.gfx.core.core_ui.theme.OmniSurfaceElevated
import com.basitce.gfx.core.core_ui.theme.OmniWarning
import com.basitce.gfx.feature.feature_config.viewmodel.ConfigViewModel
import com.basitce.gfx.feature.feature_config.viewmodel.UiComponentModel

@Composable
fun DynamicConfigScreen(
    gameId: String,
    onBack: () -> Unit,
    profileId: String? = null,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    LaunchedEffect(gameId, profileId) {
        viewModel.loadSchemaForGame(gameId, profileId)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.launchGameEvent.collect { packageName ->
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            }
        }
    }

    val schema by viewModel.schema.collectAsStateWithLifecycle()
    val userValues by viewModel.userValues.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val applyStatus by viewModel.applyStatus.collectAsStateWithLifecycle()
    val isManual by viewModel.isManualProfile.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }

    val parsedComponents by viewModel.parsedComponents.collectAsStateWithLifecycle()
    val parserType by viewModel.parserType.collectAsStateWithLifecycle()
    val targetFile by viewModel.targetFile.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.basitce.gfx.core.core_ui.components.OmniTopBar(
                title = schema?.let { "${it.gameId} Ayarları" } ?: "Ayarlar",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (schema != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    if (isManual) {
                        OmniPrimaryButton(
                            text = "Ham Dosyayı Cihaza Yaz",
                            onClick = { viewModel.applyManualProfile() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OmniSecondaryButton(
                                text = "Profil Kaydet",
                                onClick = { showSaveDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            OmniPrimaryButton(
                                text = "Uygula",
                                onClick = { viewModel.applyPatch() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            when {
                schema == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                isManual -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileChipRow(
                        profiles = profiles,
                        onLoadProfile = { viewModel.loadProfile(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OmniCard {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = OmniWarning,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ham Dosya Profili",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bu profil dosyanın tam yedeğini saklar. Slider/Dropdown ayarları yoktur. Aşağıdaki butona bastığınızda dosya Shizuku üzerinden hedefe doğrudan atomik olarak yazılır.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OmniOnSurfaceVariant
                        )
                    }

                    if (applyStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = applyStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (applyStatus.startsWith("Hata")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(140.dp))
                }

                parsedComponents.isEmpty() -> {
                    OmniEmptyState(
                        title = "Şema boş",
                        description = "Bu oyun için tanımlanmış ayar bileşeni yok. Önce sihirbazı kullanarak şema oluştur.",
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }

                else -> {
                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileChipRow(
                        profiles = profiles,
                        onLoadProfile = { viewModel.loadProfile(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (targetFile.isNotBlank()) {
                        OmniCard {
                            Text(
                                text = "Hedef Dosya",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = targetFile,
                                style = MaterialTheme.typography.bodySmall,
                                color = OmniOnSurfaceVariant
                            )
                            Text(
                                text = "Parser: ${parserType.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OmniCard {
                        Text(
                            text = "Ayarlar",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        parsedComponents.forEachIndexed { index, comp ->
                            val id = comp.id
                            val type = comp.type

                            when (type) {
                                "slider" -> {
                                    val min = comp.min
                                    val max = comp.max
                                    val step = comp.step
                                    val valueLabels = comp.valueLabels
                                    val currentValue = (userValues[id] as? Number)?.toFloat() ?: min

                                    val isDiscrete = step > 0
                                    val displayValue = if (valueLabels != null && currentValue.toInt() in valueLabels.indices) {
                                        valueLabels[currentValue.toInt()]
                                    } else if (isDiscrete) {
                                        String.format("%.0f", currentValue)
                                    } else {
                                        String.format("%.1f", currentValue)
                                    }

                                    OmniSlider(
                                        label = comp.label,
                                        value = currentValue,
                                        onValueChange = {
                                            val stored = if (isDiscrete) it.toInt() else it
                                            viewModel.updateValue(id, stored)
                                        },
                                        valueRange = min..max,
                                        steps = if (isDiscrete) ((max - min) / step).toInt() - 1 else 0,
                                        valueLabel = displayValue,
                                        description = comp.description
                                    )
                                    if (index < parsedComponents.lastIndex) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                "dropdown" -> {
                                    val options = comp.options ?: listOf(comp.label)
                                    val optionValues = comp.optionValues
                                    val currentValue = userValues[id]
                                    val selectedIndex = if (optionValues != null && currentValue != null) {
                                        optionValues.indexOfFirst { it == currentValue }
                                            .coerceAtLeast(0)
                                    } else {
                                        (currentValue as? Number)?.toInt()?.coerceIn(0, options.size - 1) ?: 0
                                    }

                                    OmniDropdown(
                                        label = comp.label,
                                        options = options,
                                        selectedOption = options.getOrElse(selectedIndex) { options.first() },
                                        onOptionSelected = { selectedOption ->
                                            val optIndex = options.indexOf(selectedOption)
                                            val valueToStore = if (optionValues != null && optIndex in optionValues.indices) {
                                                optionValues[optIndex]
                                            } else {
                                                optIndex
                                            }
                                            viewModel.updateValue(id, valueToStore)
                                        }
                                    )
                                    if (index < parsedComponents.lastIndex) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                "toggle" -> {
                                    val currentValue = (userValues[id] as? Boolean) ?: false
                                    OmniSwitch(
                                        label = comp.label,
                                        checked = currentValue,
                                        onCheckedChange = { viewModel.updateValue(id, it) },
                                        description = comp.description
                                    )
                                }
                            }
                        }
                    }

                    if (applyStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = applyStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (applyStatus.startsWith("Hata")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(140.dp))
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Profil Kaydet") },
            text = {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profil Adı") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (profileName.isNotBlank()) {
                            viewModel.saveProfile(profileName)
                            showSaveDialog = false
                            profileName = ""
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun ProfileChipRow(
    profiles: List<ProfileEntity>,
    onLoadProfile: (ProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (profiles.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Kayıtlı Profiller",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(profiles) { profile ->
                SuggestionChip(
                    onClick = { onLoadProfile(profile) },
                    label = {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = OmniSurfaceElevated
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
