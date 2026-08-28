package com.basitce.gfx.feature.feature_config.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_database.dao.GameDao
import com.basitce.gfx.core.core_database.dao.ProfileDao
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_database.entity.SchemaEntity
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.pipeline.PmpEvent
import com.basitce.gfx.core.core_engine.pipeline.PmpPatch
import com.basitce.gfx.core.core_engine.pipeline.PmpPatchMode
import com.basitce.gfx.core.core_engine.pipeline.PmpPipeline
import com.basitce.gfx.core.core_engine.pipeline.PmpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val schemaDao: SchemaDao,
    private val profileDao: ProfileDao,
    private val gameDao: GameDao,
    private val pmpPipeline: PmpPipeline,
    private val pmpEngine: PmpEngine
) : ViewModel() {

    private val _schema = MutableStateFlow<SchemaEntity?>(null)
    val schema: StateFlow<SchemaEntity?> = _schema

    private val _userValues = MutableStateFlow<Map<String, Any>>(emptyMap())
    val userValues: StateFlow<Map<String, Any>> = _userValues

    private val _profiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
    val profiles: StateFlow<List<ProfileEntity>> = _profiles

    private val _applyStatus = MutableStateFlow<String>("")
    val applyStatus: StateFlow<String> = _applyStatus

    private val _launchGameEvent = MutableSharedFlow<String>(replay = 1)
    val launchGameEvent: SharedFlow<String> = _launchGameEvent

    private val _parsedComponents = MutableStateFlow<List<UiComponentModel>>(emptyList())
    val parsedComponents: StateFlow<List<UiComponentModel>> = _parsedComponents

    private val _parserType = MutableStateFlow("regex")
    val parserType: StateFlow<String> = _parserType

    private val _targetFile = MutableStateFlow("")
    val targetFile: StateFlow<String> = _targetFile

    private val _isManualProfile = MutableStateFlow(false)
    val isManualProfile: StateFlow<Boolean> = _isManualProfile.asStateFlow()

    private val _rawContent = MutableStateFlow<String?>(null)
    val rawContent: StateFlow<String?> = _rawContent.asStateFlow()

    fun loadSchemaForGame(gameId: String, profileId: String? = null) {
        viewModelScope.launch {
            try {
                val schemaList = schemaDao.getSchemasForGame(gameId).firstOrNull()
                if (!schemaList.isNullOrEmpty()) {
                    val schema = schemaList.first()
                    _schema.value = schema
                    parseSchema(schema)
                    loadProfiles()
                    
                    profileId?.let { id ->
                        val profile = profileDao.getProfileById(id)
                        if (profile != null) {
                            loadProfile(profile, silent = true)
                            _isManualProfile.value = profile.isManual
                            _rawContent.value = profile.rawContent
                        }
                    }
                }
            } catch (e: Exception) {
                _applyStatus.value = "Şema yüklenemedi: ${e.message}"
            }
        }
    }

    private fun parseSchema(schema: SchemaEntity) {
        try {
            val schemaObj = JSONObject(schema.jsonSchema)
            _targetFile.value = schemaObj.optString("targetFile", "")
            _parserType.value = schemaObj.optString("parser", "regex")

            val componentsArray = schemaObj.optJSONArray("uiComponents")
            val components = mutableListOf<UiComponentModel>()

            if (componentsArray != null) {
                for (i in 0 until componentsArray.length()) {
                    val comp = componentsArray.getJSONObject(i)
                    val valueLabelsArray = comp.optJSONArray("valueLabels")
                    val optionsArray = comp.optJSONArray("options")
                    val optionValuesArray = comp.optJSONArray("optionValues")

                    components.add(
                        UiComponentModel(
                            id = comp.getString("id"),
                            type = comp.getString("type"),
                            label = comp.optString("label", comp.getString("id")),
                            description = comp.optString("description", ""),
                            min = comp.optDouble("min", 0.0).toFloat(),
                            max = comp.optDouble("max", 100.0).toFloat(),
                            step = comp.optInt("step", 0),
                            valueLabels = valueLabelsArray?.let { arr ->
                                (0 until arr.length()).map { arr.optString(it, "") }
                            },
                            options = optionsArray?.let { arr ->
                                (0 until arr.length()).map { arr.optString(it, "") }
                            },
                            optionValues = optionValuesArray?.let { arr ->
                                (0 until arr.length()).mapNotNull { arr.opt(it) }
                            }
                        )
                    )
                }
            }
            _parsedComponents.value = components
        } catch (e: Exception) {
            _parsedComponents.value = emptyList()
        }
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                val schemaId = _schema.value?.id ?: return@launch
                profileDao.getProfilesForSchema(schemaId).collect { profileList ->
                    _profiles.value = profileList
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updateValue(key: String, value: Any) {
        val current = _userValues.value.toMutableMap()
        current[key] = value
        _userValues.value = current
    }

    fun loadProfile(profile: ProfileEntity, silent: Boolean = false) {
        try {
            _isManualProfile.value = profile.isManual
            _rawContent.value = profile.rawContent

            if (!profile.isManual) {
                val json = JSONObject(profile.userValuesJson)
                val values = mutableMapOf<String, Any>()
                json.keys().forEach { key ->
                    values[key] = json.get(key)
                }
                _userValues.value = values
            }
            if (!silent) {
                _applyStatus.value = "Profil '${profile.name}' yüklendi."
            }
        } catch (e: Exception) {
            _applyStatus.value = "Profil yüklenirken hata: ${e.message}"
        }
    }

    fun saveProfile(name: String) {
        viewModelScope.launch {
            try {
                val schemaId = _schema.value?.id ?: return@launch
                val valuesJson = JSONObject(_userValues.value).toString()
                val profile = ProfileEntity(
                    id = UUID.randomUUID().toString(),
                    schemaId = schemaId,
                    name = name,
                    userValuesJson = valuesJson
                )
                profileDao.insertProfile(profile)
                _applyStatus.value = "Profil '$name' kaydedildi!"
            } catch (e: Exception) {
                _applyStatus.value = "Profil kaydedilemedi: ${e.message}"
            }
        }
    }

    fun applyManualProfile() {
        val content = _rawContent.value ?: return
        val targetFile = _targetFile.value
        if (targetFile.isBlank()) {
            _applyStatus.value = "Hata: Hedef dosya yolu bilinmiyor."
            return
        }

        viewModelScope.launch {
            _applyStatus.value = "Ham dosya Shizuku ile yazılıyor..."
            try {
                val cacheFile = File(context.cacheDir, "manual_push_${System.currentTimeMillis()}")
                withContext(Dispatchers.IO) { cacheFile.writeText(content, Charsets.UTF_8) }

                val metadata = pmpEngine.stat(targetFile)
                val success = if (pmpEngine.supportsAtomicPush) {
                    pmpEngine.pushAtomic(cacheFile, targetFile, metadata, true)
                } else {
                    pmpEngine.push(cacheFile, targetFile)
                }

                withContext(Dispatchers.IO) { cacheFile.delete() }

                if (success) {
                    _applyStatus.value = "Başarıyla uygulandı! (Ham Dosya)"
                    launchGame(_schema.value?.gameId ?: "")
                } else {
                    _applyStatus.value = "Hata: Dosya hedefe yazılamadı."
                }
            } catch (e: Exception) {
                _applyStatus.value = "Hata: ${e.message}"
            }
        }
    }

    fun applyPatch() {
        val currentSchema = _schema.value ?: return
        viewModelScope.launch {
            _applyStatus.value = "Uygulanıyor..."
            try {
                val request = buildPmpRequest(currentSchema)
                if (request == null) {
                    _applyStatus.value = "Hata: Şema parse edilemedi."
                    return@launch
                }

                pmpPipeline.execute(request).collect { event ->
                    when (event) {
                        is PmpEvent.Completed -> {
                            when (event.result) {
                                is com.basitce.gfx.core.core_engine.pipeline.PmpResult.Success -> {
                                    _applyStatus.value = "Başarıyla uygulandı!"
                                    launchGame(currentSchema.gameId)
                                }
                                is com.basitce.gfx.core.core_engine.pipeline.PmpResult.NoChange -> {
                                    _applyStatus.value = "Değişiklik yok — dosya zaten güncel."
                                }
                            }
                        }
                        is PmpEvent.Failed -> {
                            _applyStatus.value = "Hata: ${event.message}"
                        }
                        is PmpEvent.Pulling -> _applyStatus.value = "Dosya çekiliyor..."
                        is PmpEvent.Decoding -> _applyStatus.value = "Config ayrıştırılıyor..."
                        is PmpEvent.Modifying -> _applyStatus.value = "Değişiklikler uygulanıyor..."
                        is PmpEvent.Pushing -> _applyStatus.value = "Dosya yazılıyor..."
                        is PmpEvent.Verifying -> _applyStatus.value = "Doğrulanıyor..."
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                _applyStatus.value = "Hata: ${e.message}"
            }
        }
    }

    private fun launchGame(gameId: String) {
        viewModelScope.launch {
            try {
                val game = gameDao.getGameById(gameId)
                game?.packageName?.let { packageName ->
                    _launchGameEvent.emit(packageName)
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun buildPmpRequest(schemaEntity: SchemaEntity): PmpRequest? {
        return try {
            val schemaObj = JSONObject(schemaEntity.jsonSchema)
            val targetFile = schemaObj.getString("targetFile")
            val parserType = schemaObj.optString("parser", "regex")
            val components = schemaObj.getJSONArray("uiComponents")
            val currentValues = _userValues.value

            val patches = mutableListOf<PmpPatch>()

            for (i in 0 until components.length()) {
                val comp = components.getJSONObject(i)
                val id = comp.getString("id")
                val userValue = currentValues[id] ?: continue

                when (parserType.lowercase()) {
                    "regex" -> {
                        if (comp.has("injectionRule")) {
                            val rule = comp.getJSONObject("injectionRule")
                            patches.add(
                                PmpPatch(
                                    path = id,
                                    value = userValue,
                                    regexPattern = rule.getString("pattern"),
                                    regexReplacementTemplate = rule.getString("replacement")
                                )
                            )
                        }
                    }
                    "ini", "properties" -> {
                        val path = if (comp.has("iniKey")) comp.getString("iniKey") else comp.optString("path", id)
                        patches.add(
                            PmpPatch(
                                path = path,
                                value = userValue
                            )
                        )
                    }
                    "json", "jsonpath" -> {
                        val path = if (comp.has("jsonPath")) comp.getString("jsonPath") else comp.optString("path", id)
                        patches.add(
                            PmpPatch(
                                path = path,
                                value = userValue
                            )
                        )
                    }
                    else -> {
                        val path = comp.optString("path", id)
                        patches.add(
                            PmpPatch(
                                path = path,
                                value = userValue
                            )
                        )
                    }
                }
            }

            if (patches.isEmpty()) return null

            val patchMode = when (parserType.lowercase()) {
                "regex" -> PmpPatchMode.REGEX
                else -> PmpPatchMode.CONFIG_PARSER
            }

            PmpRequest(
                remotePath = targetFile,
                patches = patches,
                patchMode = patchMode
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class UiComponentModel(
    val id: String,
    val type: String,
    val label: String,
    val description: String,
    val min: Float,
    val max: Float,
    val step: Int,
    val valueLabels: List<String>?,
    val options: List<String>?,
    val optionValues: List<Any>?
)
