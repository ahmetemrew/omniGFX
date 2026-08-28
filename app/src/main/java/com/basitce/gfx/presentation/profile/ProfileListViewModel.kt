package com.basitce.gfx.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_database.dao.ProfileDao
import com.basitce.gfx.core.core_database.model.ProfileWithSchema
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileListViewModel @Inject constructor(
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                profileDao.getAllProfilesWithSchema().collect { profiles ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profiles = profiles,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Profiller yüklenemedi."
                    )
                }
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            try {
                profileDao.deleteProfile(profileId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Profil silinemedi."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }
}
