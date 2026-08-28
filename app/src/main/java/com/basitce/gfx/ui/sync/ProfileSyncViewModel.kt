package com.basitce.gfx.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.profile.ProfileRepository
import com.basitce.gfx.core.core_engine.profile.UserConfigProfile
import com.basitce.gfx.core.sync.ProfileCloudDto
import com.basitce.gfx.core.sync.ProfileSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSyncUiState(
    val isLoading: Boolean = false,
    val localProfiles: List<UserConfigProfile> = emptyList(),
    val remoteProfiles: List<ProfileCloudDto> = emptyList(),
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileSyncViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val syncManager: ProfileSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSyncUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val localProfiles = profileRepository.getAllProfiles()
                val remoteProfiles = syncManager.refreshRemoteProfiles()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        localProfiles = localProfiles,
                        remoteProfiles = remoteProfiles,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Sync listesi yüklenemedi."
                    )
                }
            }
        }
    }

    fun publishProfile(profile: UserConfigProfile) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val published = syncManager.publishLocalProfile(profile)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Profil publish edildi: ${published.name} v${published.version}"
                    )
                }

                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Profil publish edilemedi."
                    )
                }
            }
        }
    }

    fun importRemoteProfile(remoteProfile: ProfileCloudDto) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val imported = syncManager.importRemoteProfile(remoteProfile.id)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Profil import edildi: ${imported?.name}"
                    )
                }

                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Remote profil import edilemedi."
                    )
                }
            }
        }
    }

    fun deleteRemoteProfile(remoteProfile: ProfileCloudDto) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                syncManager.deleteRemoteProfile(remoteProfile.id)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Remote profil silindi: ${remoteProfile.name}"
                    )
                }

                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Remote profil silinemedi."
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(message = null)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }
}
