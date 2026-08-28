package com.basitce.gfx.feature.feature_wizard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.pipeline.PmpEvent
import com.basitce.gfx.core.core_engine.pipeline.PmpPatch
import com.basitce.gfx.core.core_engine.pipeline.PmpPatchMode
import com.basitce.gfx.core.core_engine.pipeline.PmpPipeline
import com.basitce.gfx.core.core_engine.pipeline.PmpRequest
import com.basitce.gfx.core.core_engine.pipeline.PmpResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

data class ProfileApplyUiState(
    val isApplying: Boolean = false,
    val progressMessage: String = "",
    val isSuccess: Boolean? = null,
    val message: String? = null
)

@HiltViewModel
class ProfileApplyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pmpPipeline: PmpPipeline,
    private val pmpEngine: PmpEngine,
    private val schemaDao: SchemaDao
) : ViewModel() {

    private val _applyState = MutableStateFlow(ProfileApplyUiState())
    val applyState: StateFlow<ProfileApplyUiState> = _applyState.asStateFlow()

    fun applyProfile(profile: ProfileEntity) {
        if (profile.isManual) {
            applyManualProfile(profile)
        } else {
            applyDynamicProfile(profile)
        }
    }

    private fun applyManualProfile(profile: ProfileEntity) {
        val content = profile.rawContent
        if (content.isNullOrBlank()) {
            _applyState.update {
                it.copy(
                    isApplying = false,
                    isSuccess = false,
                    message = "Ham profil içeriği boş."
                )
            }
            return
        }
        if (profile.targetFilePath.isBlank()) {
            _applyState.update {
                it.copy(
                    isApplying = false,
                    isSuccess = false,
                    message = "Hedef dosya yolu bilinmiyor."
                )
            }
            return
        }
        viewModelScope.launch {
            _applyState.update {
                ProfileApplyUiState(
                    isApplying = true,
                    progressMessage = "Ham dosya Shizuku ile yazılıyor..."
                )
            }
            try {
                val cacheFile = File(
                    context.cacheDir,
                    "manual_apply_${System.currentTimeMillis()}"
                )
                withContext(Dispatchers.IO) {
                    cacheFile.writeText(content, Charsets.UTF_8)
                }
                val metadata = try {
                    pmpEngine.stat(profile.targetFilePath)
                } catch (e: Exception) {
                    null
                }
                val success = if (pmpEngine.supportsAtomicPush) {
                    pmpEngine.pushAtomic(cacheFile, profile.targetFilePath, metadata, true)
                } else {
                    pmpEngine.push(cacheFile, profile.targetFilePath)
                }
                withContext(Dispatchers.IO) { cacheFile.delete() }
                if (success) {
                    _applyState.update {
                        it.copy(
                            isApplying = false,
                            isSuccess = true,
                            message = "Ham dosya başarıyla hedefe yazıldı."
                        )
                    }
                } else {
                    _applyState.update {
                        it.copy(
                            isApplying = false,
                            isSuccess = false,
                            message = "Dosya hedefe yazılamadı. Shizuku iznini kontrol et."
                        )
                    }
                }
            } catch (e: Exception) {
                _applyState.update {
                    it.copy(
                        isApplying = false,
                        isSuccess = false,
                        message = "Uygulama hatası: ${e.message}"
                    )
                }
            }
        }
    }

    private fun applyDynamicProfile(profile: ProfileEntity) {
        if (profile.targetFilePath.isBlank()) {
            _applyState.update {
                it.copy(
                    isApplying = false,
                    isSuccess = false,
                    message = "Hedef dosya yolu bilinmiyor."
                )
            }
            return
        }
        viewModelScope.launch {
            _applyState.update {
                ProfileApplyUiState(
                    isApplying = true,
                    progressMessage = "Schema ve patch'ler hazırlanıyor..."
                )
            }
            try {
                val schemas = schemaDao.getSchemasForGame("").firstOrNull()
                val schema = schemas?.firstOrNull { it.id == profile.schemaId }
                val regexRules = parseRegexRulesFromSchema(schema?.jsonSchema)

                val patches = buildPatchesFromProfile(profile, regexRules)
                if (patches.isEmpty()) {
                    _applyState.update {
                        it.copy(
                            isApplying = false,
                            isSuccess = false,
                            message = "Uygulanacak değişken bulunamadı."
                        )
                    }
                    return@launch
                }

                val patchMode = if (regexRules.isNotEmpty()) {
                    PmpPatchMode.REGEX
                } else {
                    PmpPatchMode.CONFIG_PARSER
                }

                _applyState.update {
                    it.copy(progressMessage = "PMP Pipeline başlatılıyor...")
                }
                val request = PmpRequest(
                    remotePath = profile.targetFilePath,
                    patches = patches,
                    patchMode = patchMode
                )
                pmpPipeline.execute(request).collect { event ->
                    when (event) {
                        is PmpEvent.Started -> {
                            _applyState.update {
                                it.copy(progressMessage = "Başlatılıyor...")
                            }
                        }
                        is PmpEvent.Pulling -> {
                            _applyState.update {
                                it.copy(progressMessage = "Dosya çekiliyor...")
                            }
                        }
                        is PmpEvent.Decoding -> {
                            _applyState.update {
                                it.copy(progressMessage = "Config çözümleniyor...")
                            }
                        }
                        is PmpEvent.Modifying -> {
                            _applyState.update {
                                it.copy(progressMessage = "Değişiklikler uygulanıyor...")
                            }
                        }
                        is PmpEvent.Encoding -> {
                            _applyState.update {
                                it.copy(progressMessage = "Config serialize ediliyor...")
                            }
                        }
                        is PmpEvent.Pushing -> {
                            _applyState.update {
                                it.copy(progressMessage = "Dosya yazılıyor...")
                            }
                        }
                        is PmpEvent.Verifying -> {
                            _applyState.update {
                                it.copy(progressMessage = "Doğrulanıyor...")
                            }
                        }
                        is PmpEvent.Completed -> {
                            when (event.result) {
                                is PmpResult.Success -> {
                                    _applyState.update {
                                        it.copy(
                                            isApplying = false,
                                            isSuccess = true,
                                            message = "Profil başarıyla uygulandı."
                                        )
                                    }
                                }
                                is PmpResult.NoChange -> {
                                    _applyState.update {
                                        it.copy(
                                            isApplying = false,
                                            isSuccess = true,
                                            message = "Değişiklik yok — dosya zaten güncel."
                                        )
                                    }
                                }
                            }
                        }
                        is PmpEvent.Failed -> {
                            _applyState.update {
                                it.copy(
                                    isApplying = false,
                                    isSuccess = false,
                                    message = "Hata: ${event.message}"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _applyState.update {
                    it.copy(
                        isApplying = false,
                        isSuccess = false,
                        message = "Uygulama hatası: ${e.message}"
                    )
                }
            }
        }
    }

    private fun parseRegexRulesFromSchema(
        schemaJson: String?
    ): Map<String, Pair<String, String>> {
        if (schemaJson.isNullOrBlank()) return emptyMap()
        return try {
            val schemaObj = JSONObject(schemaJson)
            val parserType = schemaObj.optString("parser", "regex")
            if (parserType.lowercase() != "regex") return emptyMap()
            val components = schemaObj.optJSONArray("uiComponents") ?: return emptyMap()
            val rules = mutableMapOf<String, Pair<String, String>>()
            for (i in 0 until components.length()) {
                val comp = components.getJSONObject(i)
                if (comp.has("injectionRule")) {
                    val rule = comp.getJSONObject("injectionRule")
                    val pattern = rule.optString("pattern", "")
                    val replacement = rule.optString("replacement", "")
                    if (pattern.isNotBlank() && replacement.isNotBlank()) {
                        rules[comp.optString("id", "")] = pattern to replacement
                    }
                }
            }
            rules
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun buildPatchesFromProfile(
        profile: ProfileEntity,
        regexRules: Map<String, Pair<String, String>>
    ): List<PmpPatch> {
        return try {
            val json = JSONObject(profile.userValuesJson)
            val patches = mutableListOf<PmpPatch>()
            json.keys().forEach { key ->
                val value = json.get(key)
                val regexRule = regexRules[key]
                patches.add(
                    PmpPatch(
                        path = key,
                        value = value,
                        regexPattern = regexRule?.first,
                        regexReplacementTemplate = regexRule?.second
                    )
                )
            }
            patches
        } catch (e: Exception) {
            emptyList()
        }
    }
}
