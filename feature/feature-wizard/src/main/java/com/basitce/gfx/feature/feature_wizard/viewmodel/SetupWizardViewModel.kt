package com.basitce.gfx.feature.feature_wizard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_database.dao.GameDao
import com.basitce.gfx.core.core_database.dao.ProfileDao
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.entity.GameEntity
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_database.entity.SchemaEntity
import com.basitce.gfx.core.core_engine.ConfigParserFactory
import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.rollback.ConfigRollbackManager
import com.basitce.gfx.core.core_engine.rollback.RollbackResult
import com.basitce.gfx.core.core_engine.shizuku.GameProcessManager
import com.basitce.gfx.core.core_engine.shizuku.RemoteFileBrowser
import com.basitce.gfx.core.core_engine.shizuku.RemoteFileSystemEntry
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPathProbe
import com.basitce.gfx.core.core_engine.shizuku.ShizukuStateManager
import com.basitce.gfx.core.core_engine.verification.PmpVerifier
import com.basitce.gfx.core.core_engine.verification.VerifyOptions
import com.basitce.gfx.core.core_engine.workflow.DetectedFormat
import com.basitce.gfx.core.core_engine.workflow.WorkflowCache
import com.basitce.gfx.core.core_engine.workflow.WorkflowDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import com.basitce.gfx.core.core_engine.pipeline.PmpPipeline
import com.basitce.gfx.core.core_engine.pipeline.PmpRequest
import com.basitce.gfx.core.core_engine.pipeline.PmpEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SetupWizardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pmpEngine: PmpEngine,
    private val pmpPipeline: PmpPipeline,
    private val parserFactory: ConfigParserFactory,
    private val remoteFileBrowser: RemoteFileBrowser,
    private val gameProcessManager: GameProcessManager,
    private val shizukuStateManager: ShizukuStateManager,
    private val pathProbe: ShizukuPathProbe,
    private val workflowCache: WorkflowCache,
    private val pmpVerifier: PmpVerifier,
    private val rollbackManager: ConfigRollbackManager,
    private val gameDao: GameDao,
    private val schemaDao: SchemaDao,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _state = MutableStateFlow(SetupWizardState())
    val state: StateFlow<SetupWizardState> = _state.asStateFlow()

    private val _browserPath = MutableStateFlow("/")
    val browserPath: StateFlow<String> = _browserPath.asStateFlow()

    private val _browserEntries = MutableStateFlow<List<RemoteFileSystemEntry>>(emptyList())
    val browserEntries: StateFlow<List<RemoteFileSystemEntry>> = _browserEntries.asStateFlow()

    val shizukuState = shizukuStateManager.uiState

    private val undoStack = mutableListOf<List<ConfigNode>>()
    private val redoStack = mutableListOf<List<ConfigNode>>()

    companion object {
        private const val MAX_UNDO_DEPTH = 50
        private const val MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024
        private const val WARN_FILE_SIZE_BYTES = 1L * 1024 * 1024
    }

    fun init(packageName: String, gameName: String = "") {
        if (_state.value.packageName == packageName) return
        val pm = context.packageManager
        val resolvedName = if (gameName.isNotBlank()) {
            gameName
        } else {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                packageName
            }
        }

        undoStack.clear()
        redoStack.clear()
        _state.update {
            SetupWizardState(
                packageName = packageName,
                gameName = resolvedName
            )
        }

        val startPath = "/sdcard/Android/data/$packageName"
        _browserPath.value = startPath
        browseTo(startPath)

        shizukuStateManager.refreshStatus()
    }

    // ─── ADIM 1: HEDEF DOSYA SEÇİMİ ─────────────────────
    fun browseTo(path: String) {
        viewModelScope.launch {
            _state.update { it.copy(isBrowsing = true, browserError = null) }
            _browserPath.value = path
            try {
                val result = remoteFileBrowser.list(path)
                if (result.error != null) {
                    _state.update {
                        it.copy(
                            isBrowsing = false,
                            browserError = "Dizin okunamadı: ${result.error}"
                        )
                    }
                    _browserEntries.value = emptyList()
                } else {
                    _browserEntries.value = result.entries
                    _state.update { it.copy(isBrowsing = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isBrowsing = false,
                        browserError = "Shizuku bağlantı hatası: ${e.message}"
                    )
                }
                _browserEntries.value = emptyList()
            }
        }
    }

    fun browseUp() {
        val parent = remoteFileBrowser.parentPath(_browserPath.value)
        if (parent != null) browseTo(parent)
    }

    fun selectFile(path: String) {
        _state.update { it.copy(targetFilePath = path) }
    }

    fun onManualPathChange(path: String) {
        _state.update { it.copy(targetFilePath = path) }
    }

    fun proceedToStep2() {
        val path = _state.value.targetFilePath.trim()
        _state.update { it.copy(targetFilePath = path) }
        if (path.isBlank()) {
            _state.update { it.copy(error = "Önce bir dosya seçmelisin.") }
            return
        }
        _state.update { it.copy(currentStep = SetupWizardStep.PULL_ANALYZE, error = null) }
        pullAndAnalyze(path)
    }

    fun getPathPresets(): List<GamePathPreset> {
        return GamePathPresets.getFilePresetsFor(_state.value.packageName)
    }

    fun selectPreset(preset: GamePathPreset) {
        _state.update {
            it.copy(
                targetFilePath = preset.pathTemplate,
                error = null
            )
        }
        proceedToStep2()
    }

    fun openPresetDirectory(preset: GamePathPreset) {
        val path = preset.pathTemplate
        val dir = if (path.contains("/")) path.substringBeforeLast('/') else "/"
        browseTo(dir)
    }

    fun getBreadcrumb(): List<Pair<String, String>> {
        return remoteFileBrowser.breadcrumbParts(_browserPath.value)
    }

    // ─── ADIM 2: DOSYA ÇEKME & ANALİZ ──────────────────
    private fun pullAndAnalyze(remotePath: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isPulling = true,
                    fileContent = "",
                    configNodes = emptyList(),
                    pinnedVariables = emptyMap(),
                    fileMetadata = null,
                    draftId = null,
                    error = null,
                    pullProgressMessage = "Shizuku bağlantısı kontrol ediliyor…",
                    pathProbeMessages = emptyList(),
                    isFileTooLarge = false
                )
            }

            try {
                if (!shizukuStateManager.isAvailableNow()) {
                    _state.update {
                        it.copy(
                            isPulling = false,
                            error = "Shizuku bağlantısı yok. Shizuku'nun çalıştığından ve izin verildiğinden emin ol."
                        )
                    }
                    return@launch
                }

                _state.update {
                    it.copy(pullProgressMessage = "Dosya erişilebilirliği kontrol ediliyor…")
                }
                val probeResult = pathProbe.probe(remotePath)
                _state.update {
                    it.copy(pathProbeMessages = probeResult.messages)
                }

                if (!probeResult.exists) {
                    _state.update {
                        it.copy(
                            isPulling = false,
                            error = "Dosya bulunamadı: $remotePath\n\n" +
                                "Olası sebepler:\n" +
                                "• Dosya yolu yanlış\n" +
                                "• Oyun henüz hiç açılmamış (config dosyası oluşmamış)\n" +
                                "• Shizuku adb shell seviyesinde, bu dizine erişemiyor"
                        )
                    }
                    return@launch
                }

                if (!probeResult.readable) {
                    _state.update {
                        it.copy(
                            isPulling = false,
                            error = "Dosya okunamıyor: $remotePath\n\n" +
                                "Shizuku'nun bu dosyaya okuma izni yok.\n" +
                                "Root ile çalışan Shizuku kullanmayı dene."
                        )
                    }
                    return@launch
                }

                _state.update {
                    it.copy(pullProgressMessage = "Dosya bilgileri alınıyor…")
                }
                val metadata = try {
                    pmpEngine.stat(remotePath)
                } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                    null
                }

                _state.update {
                    it.copy(pullProgressMessage = "Dosya Shizuku üzerinden çekiliyor…")
                }
                val stamp = System.currentTimeMillis()
                val fileName = remotePath.substringAfterLast('/').ifBlank { "config" }
                val safeName = fileName
                    .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                    .take(64)
                    .ifBlank { "config" }
                val cacheFile = File(context.cacheDir, "wizard_pull_${stamp}_$safeName")

                val pullSuccess = pmpEngine.pull(remotePath, cacheFile)

                if (!pullSuccess || !cacheFile.exists()) {
                    cacheFile.delete()
                    _state.update {
                        it.copy(
                            isPulling = false,
                            error = "Dosya çekilemedi: $remotePath\n\n" +
                                "Shizuku iznini ve dosya yolunu kontrol et.\n" +
                                "Shizuku Kurulum ekranından durumu doğrulayabilirsin."
                        )
                    }
                    return@launch
                }

                val fileSize = cacheFile.length()

                if (fileSize > MAX_FILE_SIZE_BYTES) {
                    cacheFile.delete()
                    _state.update {
                        it.copy(
                            isPulling = false,
                            isFileTooLarge = true,
                            error = "Dosya çok büyük: ${fileSize / (1024 * 1024)} MB\n\n" +
                                "Maksimum 10 MB desteklenir.\n" +
                                "Daha küçük bir config dosyası seç."
                        )
                    }
                    return@launch
                }

                val isLargeFile = fileSize > WARN_FILE_SIZE_BYTES

                _state.update {
                    it.copy(pullProgressMessage = "İçerik okunuyor…")
                }
                val content = withContext(Dispatchers.IO) {
                    val bytes = cacheFile.readBytes()
                    require(!bytes.contains(0.toByte())) { "İkili dosyalar desteklenmiyor. UTF-8 metin config dosyası seçin." }
                    Charsets.UTF_8.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                        .decode(java.nio.ByteBuffer.wrap(bytes)).toString()
                }

                _state.update {
                    it.copy(pullProgressMessage = "Dosya formatı algılanıyor…")
                }
                val format = detectFormat(remotePath, content)

                _state.update {
                    it.copy(pullProgressMessage = "Config yapısı çözümleniyor…")
                }
                val nodes = withContext(Dispatchers.IO) {
                    ConfigTreeBuilder.build(content, format)
                }

                _state.update {
                    it.copy(pullProgressMessage = "Taslak kaydediliyor…")
                }
                val draftId = java.util.UUID.randomUUID().toString()
                val localFileName = "wizard_${stamp}_$safeName"
                val draft = WorkflowDraft(
                    id = draftId,
                    remotePath = remotePath,
                    fileName = fileName,
                    localFileName = localFileName,
                    detectedFormat = format,
                    originalContent = content,
                    modifiedContent = null,
                    metadata = metadata,
                    createdAt = stamp,
                    updatedAt = stamp
                )
                try {
                    workflowCache.saveDraft(draft)
                } catch (_: Exception) {
                }

                val gameRunning = try {
                    gameProcessManager.isRunning(_state.value.packageName)
                } catch (_: Exception) {
                    false
                }

                undoStack.clear()
                redoStack.clear()

                cacheFile.delete()

                _state.update {
                    it.copy(
                        isPulling = false,
                        fileContent = content,
                        detectedFormat = format,
                        fileMetadata = metadata,
                        fileSizeBytes = fileSize,
                        configNodes = nodes,
                        hasUnsavedChanges = false,
                        canUndo = false,
                        canRedo = false,
                        isGameRunning = gameRunning,
                        draftId = draftId,
                        pullProgressMessage = "",
                        error = if (isLargeFile) {
                            "Dosya büyük (${fileSize / 1024} KB). Düzenleme yavaş olabilir."
                        } else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isPulling = false,
                        pullProgressMessage = "",
                        error = "Çekme hatası: ${e.message}"
                    )
                }
            }
        }
    }

    fun proceedToStep3() {
        if (!_state.value.canProceedToEdit) {
            _state.update { it.copy(error = "Dosya henüz çekilmedi.") }
            return
        }
        _state.update { it.copy(currentStep = SetupWizardStep.EDIT, error = null) }
    }

    // ─── ADIM 3: AKILLI DÜZENLEME ──────────────────────
    fun updateNodeValue(nodeId: String, newValue: String) {
        saveToUndoStack()
        _state.update { state ->
            val updatedNodes = state.configNodes.map { node ->
                if (node.id == nodeId) {
                    node.copy(value = newValue, isModified = true)
                } else node
            }
            state.copy(
                configNodes = updatedNodes,
                hasUnsavedChanges = true
            )
        }
    }

    fun deleteNode(nodeId: String) {
        if (_state.value.detectedFormat !in listOf(DetectedFormat.INI, DetectedFormat.PLAIN_TEXT)) return
        saveToUndoStack()
        _state.update { state ->
            state.copy(
                configNodes = state.configNodes.filter { it.id != nodeId },
                hasUnsavedChanges = true
            )
        }
    }

    fun addNodeAfter(targetNodeId: String) {
        if (_state.value.detectedFormat !in listOf(DetectedFormat.INI, DetectedFormat.PLAIN_TEXT)) return
        saveToUndoStack()
        _state.update { state ->
            val nodes = state.configNodes.toMutableList()
            val index = nodes.indexOfFirst { it.id == targetNodeId }
            if (index == -1) return@update state

            val key = "new_key_${System.currentTimeMillis()}"
            val section = nodes[index].section
            val newNode = ConfigNode(
                lineNumber = nodes[index].lineNumber + 1,
                type = NodeType.KEY_VALUE,
                path = if (section.isBlank()) key else "$section/$key",
                key = key,
                value = "YeniDeger",
                rawLine = "$key=YeniDeger",
                isModified = true,
                section = nodes[index].section
            )
            nodes.add(index + 1, newNode)
            state.copy(configNodes = nodes, hasUnsavedChanges = true)
        }
    }

    fun moveNode(nodeId: String, direction: MoveDirection) {
        if (_state.value.detectedFormat !in listOf(DetectedFormat.INI, DetectedFormat.PLAIN_TEXT)) return
        saveToUndoStack()
        _state.update { state ->
            val nodes = state.configNodes.toMutableList()
            val index = nodes.indexOfFirst { it.id == nodeId }
            if (index == -1) return@update state

            val swapIndex = when (direction) {
                MoveDirection.UP -> index - 1
                MoveDirection.DOWN -> index + 1
            }
            if (swapIndex !in nodes.indices) return@update state

            val temp = nodes[index]
            nodes[index] = nodes[swapIndex]
            nodes[swapIndex] = temp
            state.copy(configNodes = nodes, hasUnsavedChanges = true)
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(_state.value.configNodes)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        _state.update {
            it.copy(
                configNodes = previous,
                hasUnsavedChanges = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(_state.value.configNodes)
        _state.update {
            it.copy(
                configNodes = next,
                hasUnsavedChanges = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun getSerializedContent(): String {
        val state = _state.value
        if (!state.hasUnsavedChanges) return state.fileContent
        if (state.detectedFormat in listOf(DetectedFormat.INI, DetectedFormat.PLAIN_TEXT)) {
            val newline = if (state.fileContent.contains("\r\n")) "\r\n" else "\n"
            return state.configNodes.joinToString(newline) { node ->
                if (!node.isModified) node.rawLine
                else if (node.type == NodeType.KEY_VALUE) "${node.key}=${node.value}"
                else node.value
            }
        }
        val parser = parserFactory.create(state.targetFilePath, state.fileContent)
        parser.parse(state.fileContent)
        state.configNodes.filter { it.isModified && it.type == NodeType.KEY_VALUE }.forEach {
            parser.updateValue(it.path, it.value)
        }
        return parser.serialize()
    }

    fun proceedToStep4() {
        try {
            getSerializedContent()
            _state.update { it.copy(currentStep = SetupWizardStep.PIN_VARIABLES, error = null) }
        } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            _state.update { it.copy(error = "İçerik oluşturulamadı: ${e.message}") }
        }
    }

    // ─── ADIM 4: DEĞİŞKEN PINLEME & PROFİL KAYDETME ───
    fun savePinnedVariable(variable: PinnedVariable) {
        _state.update { state ->
            val updatedMap = state.pinnedVariables.toMutableMap()
            updatedMap[variable.path] = variable
            val updatedNodes = state.configNodes.map { node ->
                if (node.path == variable.path) {
                    node.copy(isPinned = true, pinnedVariableId = variable.id)
                } else node
            }
            state.copy(
                pinnedVariables = updatedMap,
                configNodes = updatedNodes
            )
        }
    }

    fun removePin(path: String) {
        _state.update { state ->
            val updatedMap = state.pinnedVariables.toMutableMap()
            updatedMap.remove(path)
            val updatedNodes = state.configNodes.map { node ->
                if (node.path == path) {
                    node.copy(isPinned = false, pinnedVariableId = null)
                } else node
            }
            state.copy(
                pinnedVariables = updatedMap,
                configNodes = updatedNodes
            )
        }
    }

    fun updateProfileName(name: String) {
        _state.update { it.copy(profileName = name) }
    }

    fun setProfileType(type: ProfileType) {
        _state.update { it.copy(profileType = if (it.requiresRawProfile) ProfileType.MANUAL else type) }
    }

    fun saveProfileAndProceed() {
        if (_state.value.isLoading) return
        val state = _state.value

        if (state.profileName.isBlank()) {
            _state.update { it.copy(error = "Profil adı boş olamaz.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val serialized = getSerializedContent()
                val originalNodes = ConfigTreeBuilder.build(state.fileContent, state.detectedFormat)
                val structuralChange = originalNodes.map { it.path to it.rawLine } != state.configNodes.map { it.path to it.rawLine }
                val isManual = state.profileType == ProfileType.MANUAL || state.requiresRawProfile || structuralChange ||
                    state.detectedFormat == DetectedFormat.PLAIN_TEXT
                val gameId = ensureGameExists(state.packageName, state.gameName)

                val schemaId = UUID.randomUUID().toString()
                val schemaJson = buildSchemaJson(state)
                schemaDao.insertSchema(
                    SchemaEntity(
                        id = schemaId,
                        gameId = gameId,
                        version = 2,
                        jsonSchema = schemaJson
                    )
                )

                val profileId = UUID.randomUUID().toString()
                val profile = ProfileEntity(
                    id = profileId,
                    schemaId = schemaId,
                    name = state.profileName,
                    userValuesJson = if (isManual) "{}" else buildUserValuesJson(state),
                    isManual = isManual,
                    rawContent = if (isManual) serialized else null,
                    targetFilePath = state.targetFilePath,
                    createdAt = System.currentTimeMillis()
                )
                profileDao.insertProfile(profile)

                _state.update {
                    it.copy(
                        isLoading = false,
                        currentStep = SetupWizardStep.PUSH_APPLY,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Profil kaydedilemedi: ${e.message}"
                    )
                }
            }
        }
    }

    // ─── ADIM 5: GERİ YAZMA & UYGULAMA (Faz 7) ────────
    fun pushAndApply() {
        if (_state.value.isPushing) return
        val state = _state.value
        if (!state.canPush) {
            _state.update { it.copy(error = "Yazma için dosya ve içerik gerekli.") }
            return
        }

        if (state.isGameRunning && !state.showGameRunningWarning) {
            _state.update { it.copy(showGameRunningWarning = true) }
            return
        }

        viewModelScope.launch {
            val steps = buildPushSteps(state)
            _state.update {
                it.copy(
                    isPushing = true,
                    pushSuccess = null,
                    pushMessage = null,
                    error = null,
                    pushProgressSteps = steps,
                    currentPushStep = 0,
                    verificationPassed = null,
                    rollbackInfo = null,
                    showGameRunningWarning = false
                )
            }

            try {
                updatePushStep(0, PushStepStatus.RUNNING)
                val serialized = withContext(Dispatchers.IO) { getSerializedContent() }
                updatePushStep(0, PushStepStatus.COMPLETED)
                pmpPipeline.execute(PmpRequest(
                    remotePath = state.targetFilePath,
                    patches = emptyList(),
                    replacementContent = serialized,
                    expectedOriginalContent = state.fileContent
                )).collect { event ->
                    when (event) {
                        is PmpEvent.Encoding -> updatePushStep(1, PushStepStatus.COMPLETED)
                        is PmpEvent.Pushing -> updatePushStep(2, PushStepStatus.RUNNING)
                        is PmpEvent.Verifying -> {
                            updatePushStep(2, PushStepStatus.COMPLETED)
                            updatePushStep(3, PushStepStatus.COMPLETED)
                            updatePushStep(4, PushStepStatus.RUNNING)
                        }
                        is PmpEvent.Completed -> {
                            for (index in steps.indices) updatePushStep(index, PushStepStatus.COMPLETED)
                            _state.update { it.copy(isPushing = false, pushSuccess = true,
                                verificationPassed = true, pushMessage = "Dosya uygulandı ve doğrulandı.") }
                        }
                        is PmpEvent.Failed -> {
                            updatePushStep(_state.value.currentPushStep, PushStepStatus.FAILED)
                            _state.update { it.copy(isPushing = false, pushSuccess = false,
                                verificationPassed = false, error = event.message) }
                        }
                        else -> Unit
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isPushing = false, pushSuccess = false, error = "Yazma hatası: ${e.message}") }
            }
        }
    }

    private fun buildPushSteps(state: SetupWizardState): List<PushProgressStep> {
        return listOf(
            PushProgressStep("Config hazırlanıyor"),
            PushProgressStep("Geçici dosya oluşturuluyor"),
            PushProgressStep("Hedefe yazılıyor"),
            PushProgressStep("Dosya izinleri korunuyor"),
            PushProgressStep("İçerik doğrulanıyor")
        )
    }

    private fun updatePushStep(index: Int, status: PushStepStatus) {
        _state.update { state ->
            val steps = state.pushProgressSteps.toMutableList()
            if (index in steps.indices) {
                steps[index] = steps[index].copy(status = status)
            }
            state.copy(
                pushProgressSteps = steps,
                currentPushStep = index
            )
        }
    }

    private fun markRemainingStepsSkipped(fromIndex: Int) {
        _state.update { state ->
            val steps = state.pushProgressSteps.toMutableList()
            for (i in fromIndex until steps.size) {
                steps[i] = steps[i].copy(status = PushStepStatus.SKIPPED)
            }
            state.copy(pushProgressSteps = steps)
        }
    }

    fun confirmGameRunningAndPush() {
        _state.update { it.copy(showGameRunningWarning = true) }
        pushAndApply()
    }

    fun forceStopGameAndPush() {
        viewModelScope.launch {
            try {
                gameProcessManager.forceStop(_state.value.packageName)
            } catch (_: Exception) {
            }
            _state.update { it.copy(showGameRunningWarning = false, isGameRunning = false) }
            pushAndApply()
        }
    }

    fun goBack() {
        val current = _state.value.currentStep
        if (current.isFirst || current.isLast || _state.value.isPulling || _state.value.isLoading) return
        if (current == SetupWizardStep.PUSH_APPLY && _state.value.isPushing) return

        _state.update {
            it.copy(
                currentStep = current.previous(),
                error = null
            )
        }
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        _state.value = SetupWizardState()
    }

    private fun saveToUndoStack() {
        undoStack.add(_state.value.configNodes.toList())
        if (undoStack.size > MAX_UNDO_DEPTH) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        _state.update { it.copy(canUndo = true, canRedo = false) }
    }

    private fun detectFormat(path: String, content: String): DetectedFormat {
        val ext = path.substringAfterLast('.', "").lowercase()
        return when {
            ext == "json" -> DetectedFormat.JSON
            ext == "xml" -> DetectedFormat.XML
            ext in listOf("ini", "cfg", "conf", "properties", "sav") -> DetectedFormat.INI
            else -> detectFromContent(content)
        }
    }

    private fun detectFromContent(content: String): DetectedFormat {
        val trimmed = content.trim()
        return when {
            trimmed.startsWith("{") -> DetectedFormat.JSON
            trimmed.startsWith("[") && runCatching { JSONArray(trimmed) }.isSuccess -> DetectedFormat.JSON
            trimmed.startsWith("<") -> DetectedFormat.XML
            trimmed.lineSequence().any { line ->
                val t = line.trim()
                (t.startsWith("[") && t.contains("]")) || t.contains("=")
            } -> DetectedFormat.INI
            trimmed.isEmpty() -> DetectedFormat.PLAIN_TEXT
            else -> DetectedFormat.PLAIN_TEXT
        }
    }

    private suspend fun ensureGameExists(
        packageName: String,
        gameName: String
    ): String {
        val existing = gameDao.getAllGames().first()
            .find { it.packageName == packageName }
        if (existing != null) return existing.id

        val pm = context.packageManager
        val iconUri = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            if (appInfo.icon != 0) {
                "android.resource://$packageName/${appInfo.icon}"
            } else ""
        } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            ""
        }

        val gameId = UUID.randomUUID().toString()
        gameDao.insertGame(
            GameEntity(
                id = gameId,
                packageName = packageName,
                name = gameName,
                iconUri = iconUri,
                isCustom = true,
                createdAt = System.currentTimeMillis()
            )
        )
        return gameId
    }

    private fun buildSchemaJson(state: SetupWizardState): String {
        val json = JSONObject()
        json.put("targetFile", state.targetFilePath)
        json.put("parser", state.detectedFormat.name)

        val componentsArray = JSONArray()
        state.pinnedVariables.forEach { (path, pinnedVar) ->
            val comp = JSONObject()
            comp.put("id", path)
            comp.put("type", pinnedVar.uiComponentType.name.lowercase())
            comp.put("label", pinnedVar.label)
            comp.put("description", pinnedVar.description)
            comp.put("path", path)

            when (pinnedVar.uiComponentType) {
                UiComponentType.SLIDER -> {
                    comp.put("min", pinnedVar.min)
                    comp.put("max", pinnedVar.max)
                    comp.put("step", pinnedVar.step)
                }
                UiComponentType.DROPDOWN -> {
                    comp.put("options", JSONArray(pinnedVar.options))
                    if (pinnedVar.valueMapping.isNotEmpty()) {
                        val mapJson = JSONObject()
                        pinnedVar.valueMapping.forEach { (k, v) -> mapJson.put(k, v) }
                        comp.put("valueMapping", mapJson)
                    }
                }
                UiComponentType.TOGGLE -> {
                    val original = state.configNodes.firstOrNull { it.path == path }?.value
                    val booleanText = original == "true" || original == "false"
                    comp.put("checkedValue", if (booleanText) "true" else "1")
                    comp.put("uncheckedValue", if (booleanText) "false" else "0")
                }
                UiComponentType.TEXT_INPUT -> {
                }
            }
            componentsArray.put(comp)
        }
        json.put("uiComponents", componentsArray)
        return json.toString(2)
    }

    private fun buildUserValuesJson(state: SetupWizardState): String {
        val json = JSONObject()
        state.configNodes.forEach { node ->
            if (node.isPinned || node.isModified) {
                json.put(node.path, node.value)
            }
        }
        return json.toString()
    }
}
