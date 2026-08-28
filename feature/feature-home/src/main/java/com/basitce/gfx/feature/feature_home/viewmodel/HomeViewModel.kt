package com.basitce.gfx.feature.feature_home.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_database.dao.GameDao
import com.basitce.gfx.core.core_database.entity.GameEntity
import com.basitce.gfx.core.core_engine.shizuku.ShizukuConnectionState
import com.basitce.gfx.core.core_engine.shizuku.ShizukuStateManager
import com.basitce.gfx.core.core_engine.shizuku.ShizukuUiState
import com.basitce.gfx.core.core_ui.components.ShizukuStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
    private val shizukuStateManager: ShizukuStateManager
) : ViewModel() {

    val games: StateFlow<List<GameEntity>> = gameDao.getAllGames().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shizukuState: StateFlow<ShizukuUiState> = shizukuStateManager.uiState

    val shizukuStatus: StateFlow<ShizukuStatus> = shizukuStateManager.uiState
        .map { state ->
            when (state.connectionState) {
                ShizukuConnectionState.CONNECTED ->
                    ShizukuStatus.Connected
                ShizukuConnectionState.PERMISSION_REQUIRED ->
                    ShizukuStatus.PermissionRequired
                ShizukuConnectionState.NOT_RUNNING ->
                    ShizukuStatus.NotRunning
                ShizukuConnectionState.NOT_INSTALLED ->
                    ShizukuStatus.NotRunning
                ShizukuConnectionState.UNKNOWN ->
                    ShizukuStatus.Unknown
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShizukuStatus.Unknown
        )

    private val _showShizukuGuide = MutableStateFlow(false)
    val showShizukuGuide: StateFlow<Boolean> = _showShizukuGuide

    init {
        syncInstalledGames()
        shizukuStateManager.refreshStatus()
    }

    fun onShizukuBarClick() {
        when (shizukuState.value.connectionState) {
            ShizukuConnectionState.CONNECTED -> Unit
            ShizukuConnectionState.PERMISSION_REQUIRED -> {
                shizukuStateManager.requestPermission()
            }
            else -> {
                _showShizukuGuide.value = true
            }
        }
    }

    fun dismissShizukuGuide() {
        _showShizukuGuide.value = false
    }

    fun syncInstalledGames() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val pm = context.packageManager
                    val storedGames = gameDao.getAllGames().first()
                    storedGames.forEach { game ->
                        val isInstalled = try {
                            pm.getApplicationInfo(game.packageName, 0)
                            true
                        } catch (_: PackageManager.NameNotFoundException) {
                            false
                        }
                        if (!isInstalled) {
                            gameDao.deleteGame(game.id)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }
}
