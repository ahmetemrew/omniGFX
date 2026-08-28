package com.basitce.gfx.ui.diff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.diff.DiffLine
import com.basitce.gfx.core.core_engine.diff.LineDiffEngine
import com.basitce.gfx.core.core_engine.profile.ProfileApplyResult
import com.basitce.gfx.core.core_engine.profile.ProfileEngine
import com.basitce.gfx.core.core_engine.profile.UserConfigProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigDiffUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profileName: String? = null,
    val originalContent: String? = null,
    val updatedContent: String? = null,
    val diffLines: List<DiffLine> = emptyList()
)

@HiltViewModel
class ConfigDiffPreviewViewModel @Inject constructor(
    private val profileEngine: ProfileEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigDiffUiState())
    val uiState = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun preview(profile: UserConfigProfile) {
        activeJob?.cancel()

        activeJob = viewModelScope.launch {
            _uiState.value = ConfigDiffUiState(
                isLoading = true,
                profileName = profile.name
            )

            try {
                val dryProfile = profile.copy(
                    options = profile.options.copy(
                        dryRun = true,
                        forceStopBeforeApply = false,
                        launchAfterApply = false
                    )
                )

                val result = profileEngine.applyProfile(dryProfile)

                when (result) {
                    is ProfileApplyResult.Success -> {
                        val original = result.originalContent.orEmpty()
                        val updated = result.updatedContent.orEmpty()

                        val diffLines = LineDiffEngine.diff(
                            oldText = original,
                            newText = updated
                        )

                        _uiState.update {
                            ConfigDiffUiState(
                                isLoading = false,
                                profileName = profile.name,
                                originalContent = original,
                                updatedContent = updated,
                                diffLines = diffLines
                            )
                        }
                    }

                    is ProfileApplyResult.Failure -> {
                        _uiState.update {
                            ConfigDiffUiState(
                                isLoading = false,
                                profileName = profile.name,
                                error = buildString {
                                    append(result.message)
                                    append("\n")
                                    append(result.warnings.joinToString("\n"))
                                }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    ConfigDiffUiState(
                        isLoading = false,
                        profileName = profile.name,
                        error = e.message ?: "Diff preview sırasında hata oluştu."
                    )
                }
            }
        }
    }

    fun reset() {
        activeJob?.cancel()
        _uiState.value = ConfigDiffUiState()
    }
}
