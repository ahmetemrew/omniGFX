package com.basitce.gfx.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.profile.ProfilePathResolver
import com.basitce.gfx.core.core_engine.profile.ProfileRepository
import com.basitce.gfx.core.core_engine.profile.UserConfigProfile
import com.basitce.gfx.core.core_engine.rollback.BackupEntry
import com.basitce.gfx.core.core_engine.rollback.ConfigRollbackManager
import com.basitce.gfx.core.core_engine.rollback.RollbackResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupItemUi(
    val targetPath: String,
    val entry: BackupEntry
)

data class BackupManagerUiState(
    val isLoading: Boolean = false,
    val profiles: List<UserConfigProfile> = emptyList(),
    val selectedProfileId: String? = null,
    val backups: List<BackupItemUi> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class BackupManagerViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val rollbackManager: ConfigRollbackManager,
    private val pathResolver: ProfilePathResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupManagerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val profiles = profileRepository.getAllProfiles()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profiles = profiles,
                        selectedProfileId = profiles.firstOrNull()?.id,
                        message = null
                    )
                }

                if (profiles.isNotEmpty()) {
                    loadBackupsForSelectedProfile()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Profiller yüklenemedi."
                    )
                }
            }
        }
    }

    fun selectProfile(profileId: String) {
        _uiState.update {
            it.copy(selectedProfileId = profileId)
        }

        loadBackupsForSelectedProfile()
    }

    fun loadBackupsForSelectedProfile() {
        val state = _uiState.value

        val profile = state.profiles.firstOrNull {
            it.id == state.selectedProfileId
        }

        if (profile == null) {
            _uiState.update {
                it.copy(message = "Profil bulunamadı.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    backups = emptyList(),
                    message = null
                )
            }

            try {
                val candidates = pathResolver.resolveCandidates(profile)

                val backupItems = mutableListOf<BackupItemUi>()

                candidates.forEach { candidate ->
                    val backupList = rollbackManager.listBackupsForTarget(candidate)

                    backupList.backups.forEach { backup ->
                        backupItems.add(
                            BackupItemUi(
                                targetPath = candidate,
                                entry = backup
                            )
                        )
                    }
                }

                val distinctBackups = backupItems.distinctBy { it.entry.path }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        backups = distinctBackups,
                        message = if (distinctBackups.isEmpty()) {
                            "Bu profil için backup bulunamadı."
                        } else {
                            null
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Backup listesi yüklenemedi."
                    )
                }
            }
        }
    }

    fun restoreBackup(item: BackupItemUi) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = rollbackManager.restoreBackup(
                    backupPath = item.entry.path,
                    targetPath = item.targetPath
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = rollbackMessage(result)
                    )
                }

                loadBackupsForSelectedProfile()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Backup geri yüklenemedi."
                    )
                }
            }
        }
    }

    fun deleteBackup(item: BackupItemUi) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = rollbackManager.deleteBackup(item.entry.path)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = rollbackMessage(result)
                    )
                }

                loadBackupsForSelectedProfile()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Backup silinemedi."
                    )
                }
            }
        }
    }

    fun cleanOldBackups(keep: Int = 3) {
        val state = _uiState.value

        val profile = state.profiles.firstOrNull {
            it.id == state.selectedProfileId
        }

        if (profile == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val candidates = pathResolver.resolveCandidates(profile)

                val messages = mutableListOf<String>()

                candidates.forEach { candidate ->
                    val result = rollbackManager.cleanOldBackups(
                        targetPath = candidate,
                        keep = keep
                    )

                    messages.add(rollbackMessage(result))
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = messages.joinToString("\n")
                    )
                }

                loadBackupsForSelectedProfile()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Backup temizliği başarısız oldu."
                    )
                }
            }
        }
    }

    private fun rollbackMessage(result: RollbackResult): String {
        return when (result) {
            is RollbackResult.Success -> result.message
            is RollbackResult.Failure -> result.message
        }
    }
}
