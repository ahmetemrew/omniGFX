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
        val pm = context.packageManager
        val resolvedName = if (gameName.isNotBlank()) {
            gameName
        } else {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }
        }

        _state.update {
            SetupWizardState(
                packageName = packageName,
                gameName = resolvedName
            )
        }

        val startPath = "/data/data/$packageName"
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
        val path = _state.value.targetFilePath
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
                    error = null,
                    pullProgressMessage = "Shizuku bağlantısı kontrol ediliyor...",
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
                    it.copy(pullProgressMessage = "Dosya erişilebilirliği kontrol ediliyor...")
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
                    it.copy(pullProgressMessage = "Dosya bilgileri alınıyor (stat)...")
                }
                val metadata = try {
                    pmpEngine.stat(remotePath)
                } catch (e: Exception) {
                    null
                }

                _state.update {
                    it.copy(pullProgressMessage = "Dosya Shizuku üzerinden çekiliyor...")
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
                    it.copy(pullProgressMessage = "İçerik okunuyor...")
                }
                val content = withContext(Dispatchers.IO) {
                    cacheFile.readText(Charsets.UTF_8)
                }

                _state.update {
                    it.copy(pullProgressMessage = "Dosya formatı algılanıyor...")
                }
                val format = detectFormat(remotePath, content)

                _state.update {
                    it.copy(pullProgressMessage = "Config yapısı çözümleniyor...")
                }
                val nodes = withContext(Dispatchers.IO) {
                    ConfigTreeBuilder.build(content, format)
                }

                _state.update {
                    it.copy(pullProgressMessage = "Taslak kaydediliyor...")
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
                undoStack.add(nodes)

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
                        isGameRunning = gameRunning,
                        draftId = draftId,
                        pullProgressMessage = "",
                        error = if (isLargeFile) {
                            "⚠️ Dosya büyük (${fileSize / 1024} KB). Düzenleme yavaş olabilir."
                        } else null
                    )
                }
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
        saveToUndoStack()
        _state.update { state ->
            state.copy(
                configNodes = state.configNodes.filter { it.id != nodeId },
                hasUnsavedChanges = true
            )
        }
    }

    fun addNodeAfter(targetNodeId: String) {
        saveToUndoStack()
        _state.update { state ->
            val nodes = state.configNodes.toMutableList()
            val index = nodes.indexOfFirst { it.id == targetNodeId }
            if (index == -1) return@update state

            val newNode = ConfigNode(
                lineNumber = nodes[index].lineNumber + 1,
                type = NodeType.KEY_VALUE,
                path = "new_key_${System.currentTimeMillis()}",
                key = "YeniAnahtar",
                value = "YeniDeger",
                rawLine = "YeniAnahtar=YeniDeger",
                isModified = true,
                section = nodes[index].section
            )
            nodes.add(index + 1, newNode)
            state.copy(configNodes = nodes, hasUnsavedChanges = true)
        }
    }

    fun moveNode(nodeId: String, direction: MoveDirection) {
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
        if (undoStack.size <= 1) return
        val current = undoStack.removeLast()
        redoStack.add(current)
        val previous = undoStack.last()
        _state.update { it.copy(configNodes = previous, hasUnsavedChanges = true) }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        undoStack.add(next)
        _state.update { it.copy(configNodes = next, hasUnsavedChanges = true) }
    }

    val canUndo: Boolean get() = undoStack.size > 1
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun getSerializedContent(): String {
        val state = _state.value
        return try {
            if (state.profileType == ProfileType.MANUAL) {
                state.fileContent
            } else {
                serializeWithEngine(
                    state.fileContent,
                    state.targetFilePath,
                    state.configNodes
                )
            }
        } catch (e: Exception) {
            state.fileContent
        }
    }

    fun proceedToStep4() {
        _state.update {
            it.copy(currentStep = SetupWizardStep.PIN_VARIABLES, error = null)
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
        _state.update { it.copy(profileType = type) }
    }

    fun saveProfileAndProceed() {
        val state = _state.value

        if (state.profileName.isBlank()) {
            _state.update { it.copy(error = "Profil adı boş olamaz.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
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
                val isManual = state.profileType == ProfileType.MANUAL
                val profile = ProfileEntity(
                    id = profileId,
                    schemaId = schemaId,
                    name = state.profileName,
                    userValuesJson = if (isManual) "{}" else buildUserValuesJson(state),
                    isManual = isManual,
                    rawContent = if (isManual) getSerializedContent() else null,
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
        val state = _state.value
        if (!state.canPush) {
            _state.update { it.copy(error = "Push için dosya ve içerik gerekli.") }
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
                // ═══ ADIM 1: SERIALIZE ═══
                updatePushStep(0, PushStepStatus.RUNNING)
                val serializedContent = withContext(Dispatchers.IO) {
                    getSerializedContent()
                }

                if (serializedContent == state.fileContent &&
                    state.profileType != ProfileType.MANUAL
                ) {
                    updatePushStep(0, PushStepStatus.COMPLETED)
                    markRemainingStepsSkipped(1)
                    _state.update {
                        it.copy(
                            isPushing = false,
                            pushSuccess = true,
                            pushMessage = "Değişiklik yok — dosya zaten güncel.",
                            currentStep = SetupWizardStep.DONE
                        )
                    }
                    return@launch
                }
                updatePushStep(0, PushStepStatus.COMPLETED)

                // ═══ ADIM 2: GEÇİCİ DOSYA ═══
                updatePushStep(1, PushStepStatus.RUNNING)
                val cacheFile = File(
                    context.cacheDir,
                    "wizard_push_${System.currentTimeMillis()}"
                )
                withContext(Dispatchers.IO) {
                    cacheFile.writeText(serializedContent, Charsets.UTF_8)
                }
                updatePushStep(1, PushStepStatus.COMPLETED)

                // ═══ ADIM 3: SHIZUKU PUSH ═══
                updatePushStep(2, PushStepStatus.RUNNING)
                val metadata = state.fileMetadata
                var pushSuccess = false

                if (pmpEngine.supportsAtomicPush) {
                    pushSuccess = pmpEngine.pushAtomic(
                        source = cacheFile,
                        remotePath = state.targetFilePath,
                        metadata = metadata,
                        restoreSelinux = true
                    )
                }

                if (!pushSuccess) {
                    pushSuccess = pmpEngine.push(cacheFile, state.targetFilePath)
                    if (pushSuccess) {
                        restoreMetadataManually(state.targetFilePath, metadata)
                    }
                }

                withContext(Dispatchers.IO) { cacheFile.delete() }

                if (!pushSuccess) {
                    updatePushStep(2, PushStepStatus.FAILED)
                    _state.update {
                        it.copy(
                            isPushing = false,
                            pushSuccess = false,
                            error = "Dosya hedefe yazılamadı. Shizuku iznini kontrol et."
                        )
                    }
                    return@launch
                }
                updatePushStep(2, PushStepStatus.COMPLETED)

                // ═══ ADIM 4: METADATA RESTORE ═══
                updatePushStep(3, PushStepStatus.RUNNING)
                if (metadata != null) {
                    restoreMetadataManually(state.targetFilePath, metadata)
                }
                updatePushStep(3, PushStepStatus.COMPLETED)

                // ═══ ADIM 5: DOĞRULAMA ═══
                updatePushStep(4, PushStepStatus.RUNNING)
                val verifyResult = try {
                    pmpVerifier.verifyContent(
                        expectedContent = serializedContent,
                        remotePath = state.targetFilePath,
                        expectedMetadata = metadata,
                        options = VerifyOptions(
                            requireHash = true,
                            requireSize = false,
                            requireMetadata = false,
                            requireSelinux = false
                        )
                    )
                } catch (_: Exception) {
                    null
                }

                val verified = verifyResult?.success == true
                if (!verified) {
                    updatePushStep(4, PushStepStatus.FAILED)
                    val rollbackResult = try {
                        rollbackManager.restoreLatest(state.targetFilePath)
                    } catch (_: Exception) {
                        null
                    }
                    val rollbackMsg = when {
                        rollbackResult == null ->
                            "Doğrulama başarısız, rollback de yapılamadı."
                        rollbackResult is RollbackResult.Success ->
                            "Doğrulama başarısız. Otomatik rollback yapıldı: ${rollbackResult.message}"
                        else ->
                            "Doğrulama başarısız. Rollback denendi ama tamamlanamadı."
                    }
                    _state.update {
                        it.copy(
                            isPushing = false,
                            pushSuccess = false,
                            verificationPassed = false,
                            rollbackInfo = rollbackMsg,
                            error = rollbackMsg
                        )
                    }
                    return@launch
                }
                updatePushStep(4, PushStepStatus.COMPLETED)

                _state.update {
                    it.copy(
                        isPushing = false,
                        pushSuccess = true,
                        verificationPassed = true,
                        pushMessage = "Dosya başarıyla hedefe yazıldı ve doğrulandı.",
                        currentStep = SetupWizardStep.DONE
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isPushing = false,
                        pushSuccess = false,
                        error = "Push hatası: ${e.message}"
                    )
                }
            }
        }
    }

    private fun buildPushSteps(state: SetupWizardState): List<PushProgressStep> {
        return listOf(
            PushProgressStep("Config Serialize Ediliyor"),
            PushProgressStep("Geçici Dosya Oluşturuluyor"),
            PushProgressStep("Shizuku ile Hedefe Yazılıyor"),
            PushProgressStep("Metadata (UID/SELinux) Korunuyor"),
            PushProgressStep("Hash Doğrulaması Yapılıyor")
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
        if (current.isFirst || current.isLast) return
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
            undoStack.removeFirst()
        }
        redoStack.clear()
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
            trimmed.startsWith("{") || trimmed.startsWith("[") -> DetectedFormat.JSON
            trimmed.startsWith("<?xml", ignoreCase = true) -> DetectedFormat.XML
            trimmed.lineSequence().any { line ->
                val t = line.trim()
                (t.startsWith("[") && t.contains("]")) || t.contains("=")
            } -> DetectedFormat.INI
            trimmed.isEmpty() -> DetectedFormat.PLAIN_TEXT
            else -> DetectedFormat.PLAIN_TEXT
        }
    }

    private fun serializeWithEngine(
        originalContent: String,
        targetPath: String,
        nodes: List<ConfigNode>
    ): String {
        val fileName = targetPath.substringAfterLast('/')
        val parser = parserFactory.create(fileName, originalContent)
        parser.parse(originalContent)
        nodes.forEach { node ->
            if (node.type == NodeType.KEY_VALUE && node.path.isNotBlank()) {
                try {
                    parser.updateValue(node.path, node.value)
                } catch (_: Exception) {
                }
            }
        }
        return parser.serialize()
    }

    private suspend fun restoreMetadataManually(
        path: String,
        metadata: FileMetadata?
    ) {
        if (metadata == null) return
        try {
            metadata.uid?.let { uid ->
                metadata.gid?.let { gid ->
                    pmpEngine.chown(path, uid, gid)
                }
            }
            metadata.mode?.let { pmpEngine.chmod(path, it) }
            if (pmpEngine.supportsSelinux) {
                metadata.seContext?.let { pmpEngine.chcon(path, it) }
            }
        } catch (_: Exception) {
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
                    comp.put("checkedValue", "1")
                    comp.put("uncheckedValue", "0")
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
