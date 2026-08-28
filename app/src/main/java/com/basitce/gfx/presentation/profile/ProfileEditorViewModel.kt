package com.basitce.gfx.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.profile.ConfigFormatHint
import com.basitce.gfx.core.core_engine.profile.PatchValueType
import com.basitce.gfx.core.core_engine.profile.ProfileOptions
import com.basitce.gfx.core.core_engine.profile.ProfilePatch
import com.basitce.gfx.core.core_engine.profile.ProfilePathResolver
import com.basitce.gfx.core.core_engine.profile.ProfileRepository
import com.basitce.gfx.core.core_engine.profile.ProfileSecurityScanner
import com.basitce.gfx.core.core_engine.profile.UserConfigProfile
import com.basitce.gfx.core.core_engine.shizuku.ShizukuCapabilityChecker
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPathProbe
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPrivilegeLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val pathResolver: ProfilePathResolver,
    private val securityScanner: ProfileSecurityScanner,
    private val capabilityChecker: ShizukuCapabilityChecker,
    private val pathProbe: ShizukuPathProbe
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _pathTestState = MutableStateFlow(PathTestUiState())
    val pathTestState = _pathTestState.asStateFlow()

    fun loadProfile(profileId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val profile = profileRepository.getProfileById(profileId)

                if (profile == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Profil bulunamadı."
                        )
                    }
                    return@launch
                }

                setProfileToUi(profile)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Profil yüklenemedi."
                    )
                }
            }
        }
    }

    fun resetForNewProfile() {
        _uiState.value = ProfileEditorUiState(
            patches = listOf(ProfilePatchUi())
        )

        _pathTestState.value = PathTestUiState()
    }

    private fun setProfileToUi(profile: UserConfigProfile) {
        val patchesUi = profile.patches.map { patch ->
            ProfilePatchUi(
                path = patch.path,
                value = patch.value.orEmpty(),
                valueType = patch.valueType
            )
        }

        _uiState.update {
            ProfileEditorUiState(
                isLoading = false,
                isSaving = false,
                saved = false,
                error = null,
                profileId = profile.id,
                name = profile.name,
                packageName = profile.packageName.orEmpty(),
                targetPathTemplate = profile.targetPathTemplate,
                format = profile.format,
                patches = patchesUi.ifEmpty { listOf(ProfilePatchUi()) },
                options = profile.options
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onPackageNameChange(packageName: String) {
        _uiState.update { it.copy(packageName = packageName) }
    }

    fun onTargetPathTemplateChange(targetPathTemplate: String) {
        _uiState.update { it.copy(targetPathTemplate = targetPathTemplate) }
    }

    fun onFormatChange(format: ConfigFormatHint) {
        _uiState.update { it.copy(format = format) }
    }

    fun addPatch() {
        _uiState.update { state ->
            state.copy(
                patches = state.patches + ProfilePatchUi()
            )
        }
    }

    fun removePatch(patchId: String) {
        _uiState.update { state ->
            state.copy(
                patches = state.patches.filterNot { it.id == patchId }
            )
        }
    }

    fun onPatchPathChange(patchId: String, path: String) {
        updatePatch(patchId) { it.copy(path = path) }
    }

    fun onPatchValueChange(patchId: String, value: String) {
        updatePatch(patchId) { it.copy(value = value) }
    }

    fun onPatchValueTypeChange(patchId: String, valueType: PatchValueType) {
        updatePatch(patchId) { it.copy(valueType = valueType) }
    }

    private fun updatePatch(
        patchId: String,
        transform: (ProfilePatchUi) -> ProfilePatchUi
    ) {
        _uiState.update { state ->
            state.copy(
                patches = state.patches.map { patch ->
                    if (patch.id == patchId) {
                        transform(patch)
                    } else {
                        patch
                    }
                }
            )
        }
    }

    fun updateOptions(transform: (ProfileOptions) -> ProfileOptions) {
        _uiState.update { state ->
            state.copy(
                options = transform(state.options)
            )
        }
    }

    fun saveProfile() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Profil adı boş olamaz.") }
            return
        }

        if (state.targetPathTemplate.isBlank()) {
            _uiState.update { it.copy(error = "Target path boş olamaz.") }
            return
        }

        if (state.patches.isEmpty()) {
            _uiState.update { it.copy(error = "En az bir patch eklemelisin.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saved = false,
                    error = null
                )
            }

            try {
                val profile = buildProfile()

                profileRepository.saveProfile(profile)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saved = true,
                        profileId = profile.id,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saved = false,
                        error = e.message ?: "Profil kaydedilemedi."
                    )
                }
            }
        }
    }

    fun consumeSavedEvent() {
        _uiState.update {
            it.copy(saved = false)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    fun testPath() {
        viewModelScope.launch {
            _pathTestState.value = PathTestUiState(isTesting = true)

            try {
                val profile = buildProfile()

                val candidates = pathResolver.resolveCandidates(profile)

                if (candidates.isEmpty()) {
                    _pathTestState.value = PathTestUiState(
                        isTesting = false,
                        error = "Aday path üretilemedi."
                    )
                    return@launch
                }

                val securityReport = securityScanner.scan(
                    profile = profile,
                    candidatePaths = candidates
                )

                if (!securityReport.allowed) {
                    _pathTestState.value = PathTestUiState(
                        isTesting = false,
                        error = "Path güvenlik kontrolünden geçemedi.",
                        securityErrors = securityReport.errors,
                        securityWarnings = securityReport.warnings
                    )
                    return@launch
                }

                val capability = capabilityChecker.check()

                if (!capability.available) {
                    _pathTestState.value = PathTestUiState(
                        isTesting = false,
                        error = "Shizuku kullanılabilir değil.",
                        securityWarnings = securityReport.warnings
                    )
                    return@launch
                }

                val capabilityMessage = when (capability.privilegeLevel) {
                    ShizukuPrivilegeLevel.ROOT ->
                        "Shizuku root seviyesinde çalışıyor."

                    ShizukuPrivilegeLevel.ADB_SHELL ->
                        "Shizuku adb shell seviyesinde çalışıyor. " +
                            "Bazı private app dosyaları erişilemeyebilir."

                    else ->
                        "Shizuku yetki seviyesi bilinmiyor."
                }

                val probeResults = candidates.map { candidate ->
                    pathProbe.probe(candidate)
                }

                val probeUiResults = probeResults.map { probe ->
                    PathProbeUi(
                        path = probe.path,
                        exists = probe.exists,
                        readable = probe.readable,
                        writable = probe.writable,
                        parentWritable = probe.parentWritable,
                        canStat = probe.canStat,
                        messages = probe.messages
                    )
                }

                _pathTestState.value = PathTestUiState(
                    isTesting = false,
                    error = null,
                    capabilityMessage = capabilityMessage,
                    securityWarnings = securityReport.warnings,
                    securityErrors = emptyList(),
                    candidates = probeUiResults
                )
            } catch (e: Exception) {
                _pathTestState.value = PathTestUiState(
                    isTesting = false,
                    error = e.message ?: "Path test edilemedi."
                )
            }
        }
    }

    fun buildProfile(): UserConfigProfile {
        val state = _uiState.value

        val domainPatches = state.patches.map { patchUi ->
            ProfilePatch(
                path = patchUi.path.trim(),
                value = if (patchUi.valueType == PatchValueType.NULL) {
                    null
                } else {
                    patchUi.value
                },
                valueType = patchUi.valueType
            )
        }

        return UserConfigProfile(
            id = state.profileId ?: java.util.UUID.randomUUID().toString(),
            name = state.name.trim(),
            packageName = state.packageName.trim().takeIf { it.isNotEmpty() },
            targetPathTemplate = state.targetPathTemplate.trim(),
            format = state.format,
            patches = domainPatches,
            options = state.options
        )
    }
}
