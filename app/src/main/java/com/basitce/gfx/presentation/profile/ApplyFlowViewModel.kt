package com.basitce.gfx.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.profile.ProfileApplyResult
import com.basitce.gfx.core.core_engine.profile.ProfileEngine
import com.basitce.gfx.core.core_engine.profile.ProfileEngineEvent
import com.basitce.gfx.core.core_engine.profile.ProfileRepository
import com.basitce.gfx.core.core_engine.profile.UserConfigProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ApplyStep {
    IDLE,
    LOADING_PROFILE,
    APPLYING,
    COMPLETED
}

data class ApplyFlowUiState(
    val isApplying: Boolean = false,
    val step: ApplyStep = ApplyStep.IDLE,
    val result: ProfileApplyResult? = null,
    val error: String? = null,
    val lastEvent: ProfileEngineEvent? = null,
    val events: List<ProfileEngineEvent> = emptyList()
)

@HiltViewModel
class ApplyFlowViewModel @Inject constructor(
    private val profileEngine: ProfileEngine,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplyFlowUiState())
    val uiState = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun applyProfileById(
        profileId: String,
        dryRunOverride: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ApplyFlowUiState(
                isApplying = true,
                step = ApplyStep.LOADING_PROFILE
            )

            try {
                val profile = profileRepository.getProfileById(profileId)

                if (profile == null) {
                    _uiState.update {
                        ApplyFlowUiState(
                            isApplying = false,
                            step = ApplyStep.COMPLETED,
                            error = "Profil bulunamadı."
                        )
                    }
                    return@launch
                }

                applyProfile(
                    profile = profile,
                    dryRunOverride = dryRunOverride
                )
            } catch (e: Exception) {
                _uiState.update {
                    ApplyFlowUiState(
                        isApplying = false,
                        step = ApplyStep.COMPLETED,
                        error = e.message ?: "Profil yüklenemedi."
                    )
                }
            }
        }
    }

    fun applyProfile(
        profile: UserConfigProfile,
        dryRunOverride: Boolean? = null
    ) {
        activeJob?.cancel()

        activeJob = viewModelScope.launch {
            _uiState.value = ApplyFlowUiState(
                isApplying = true,
                step = ApplyStep.APPLYING,
                events = emptyList()
            )

            val effectiveProfile = if (dryRunOverride != null) {
                profile.copy(
                    options = profile.options.copy(
                        dryRun = dryRunOverride
                    )
                )
            } else {
                profile
            }

            try {
                profileEngine.applyProfileWithEvents(effectiveProfile)
                    .collect { event ->
                        _uiState.update { state ->
                            state.copy(
                                lastEvent = event,
                                events = state.events + event,
                                step = if (event is ProfileEngineEvent.Completed) {
                                    ApplyStep.COMPLETED
                                } else {
                                    ApplyStep.APPLYING
                                }
                            )
                        }

                        if (event is ProfileEngineEvent.Completed) {
                            _uiState.update {
                                it.copy(
                                    isApplying = false,
                                    result = event.result,
                                    error = null
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    ApplyFlowUiState(
                        isApplying = false,
                        step = ApplyStep.COMPLETED,
                        error = e.message ?: "Apply sırasında hata oluştu."
                    )
                }
            }
        }
    }

    fun applyCurrentEditorProfile(
        editorViewModel: ProfileEditorViewModel,
        dryRunOverride: Boolean? = null
    ) {
        val profile = editorViewModel.buildProfile()

        applyProfile(
            profile = profile,
            dryRunOverride = dryRunOverride
        )
    }

    fun reset() {
        activeJob?.cancel()
        _uiState.value = ApplyFlowUiState()
    }
}
