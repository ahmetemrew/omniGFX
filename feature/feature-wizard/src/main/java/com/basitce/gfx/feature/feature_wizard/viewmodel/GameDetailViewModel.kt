package com.basitce.gfx.feature.feature_wizard.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_database.dao.GameDao
import com.basitce.gfx.core.core_database.dao.ProfileDao
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.entity.GameEntity
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameDao: GameDao,
    private val schemaDao: SchemaDao,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val gameId: String = savedStateHandle["gameId"] ?: ""

    private val _game = MutableStateFlow<GameEntity?>(null)
    val game: StateFlow<GameEntity?> = _game.asStateFlow()

    private val _profiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
    val profiles: StateFlow<List<ProfileEntity>> = _profiles.asStateFlow()

    private val _packageName = MutableStateFlow<String?>(null)
    val packageName: StateFlow<String?> = _packageName.asStateFlow()

    private val _profileToDelete = MutableStateFlow<ProfileEntity?>(null)
    val profileToDelete: StateFlow<ProfileEntity?> = _profileToDelete.asStateFlow()

    private val _profileToApply = MutableStateFlow<ProfileEntity?>(null)
    val profileToApply: StateFlow<ProfileEntity?> = _profileToApply.asStateFlow()

    init {
        loadGame()
        loadProfiles()
    }

    private fun loadGame() {
        viewModelScope.launch {
            _game.value = gameDao.getGameById(gameId)
            _packageName.value = _game.value?.packageName
        }
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                val schemas = schemaDao.getSchemasForGame(gameId).firstOrNull()
                if (schemas.isNullOrEmpty()) {
                    _profiles.value = emptyList()
                    return@launch
                }

                val allProfiles = mutableListOf<ProfileEntity>()
                schemas.forEach { schema ->
                    val pList = profileDao.getProfilesForSchema(schema.id).firstOrNull()
                    if (!pList.isNullOrEmpty()) {
                        allProfiles.addAll(pList)
                    }
                }
                _profiles.value = allProfiles.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                _profiles.value = emptyList()
            }
        }
    }

    fun requestDelete(profile: ProfileEntity) {
        _profileToDelete.value = profile
    }

    fun cancelDelete() {
        _profileToDelete.value = null
    }

    fun confirmDelete() {
        val profile = _profileToDelete.value ?: return
        viewModelScope.launch {
            try {
                profileDao.deleteProfile(profile.id)
                _profileToDelete.value = null
                loadProfiles()
            } catch (e: Exception) {
                _profileToDelete.value = null
            }
        }
    }

    fun requestApply(profile: ProfileEntity) {
        _profileToApply.value = profile
    }

    fun dismissApply() {
        _profileToApply.value = null
    }
}
