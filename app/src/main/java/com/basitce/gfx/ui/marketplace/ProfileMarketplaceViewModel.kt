package com.basitce.gfx.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.profile.ProfileRepository
import com.basitce.gfx.core.core_engine.profile.ProfileTemplateAnalysis
import com.basitce.gfx.core.core_engine.profile.ProfileTemplateAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileMarketplaceUiState(
    val jsonInput: String = "",
    val isAnalyzing: Boolean = false,
    val isImporting: Boolean = false,
    val analysis: ProfileTemplateAnalysis? = null,
    val importedProfileId: String? = null,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ProfileMarketplaceViewModel @Inject constructor(
    private val analyzer: ProfileTemplateAnalyzer,
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileMarketplaceUiState())
    val uiState = _uiState.asStateFlow()

    fun onJsonInputChange(newValue: String) {
        _uiState.update {
            it.copy(
                jsonInput = newValue,
                analysis = null,
                error = null,
                message = null
            )
        }
    }

    fun analyze() {
        val json = _uiState.value.jsonInput.trim()

        if (json.isBlank()) {
            _uiState.update {
                it.copy(error = "JSON boş olamaz.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    error = null,
                    message = null
                )
            }

            try {
                val analysis = analyzer.analyzeJson(json)

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysis = analysis
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = e.message ?: "Profil analiz edilemedi."
                    )
                }
            }
        }
    }

    fun importProfile() {
        val state = _uiState.value

        val analysis = state.analysis

        if (analysis == null) {
            _uiState.update {
                it.copy(error = "Önce analiz yapmalısın.")
            }
            return
        }

        if (!analysis.canImport) {
            _uiState.update {
                it.copy(error = "Bu profil güvenli bulunmadığı için import edilemez.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImporting = true,
                    error = null,
                    message = null
                )
            }

            try {
                val profile = repository.importProfileFromString(state.jsonInput.trim())
                repository.saveProfile(profile)

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importedProfileId = profile.id,
                        message = "Profil başarıyla import edildi: ${profile.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = e.message ?: "Profil import edilemedi."
                    )
                }
            }
        }
    }
}
