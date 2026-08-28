package com.basitce.gfx.ui.shizuku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.shizuku.ShizukuConnectionState
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPrivilegeLevel
import com.basitce.gfx.core.core_engine.shizuku.ShizukuStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShizukuSetupUiState(
    val isChecking: Boolean = false,
    val permissionGranted: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val privilegeLevel: ShizukuPrivilegeLevel? = null,
    val canContinue: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ShizukuSetupViewModel @Inject constructor(
    private val shizukuStateManager: ShizukuStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShizukuSetupUiState())
    val uiState: StateFlow<ShizukuSetupUiState> = _uiState

    init {
        observeState()
        check()
    }

    private fun observeState() {
        viewModelScope.launch {
            shizukuStateManager.uiState.collect { state ->
                val connectionState = state.connectionState
                val permissionGranted =
                    connectionState == ShizukuConnectionState.CONNECTED
                val shizukuAvailable =
                    connectionState == ShizukuConnectionState.CONNECTED ||
                    connectionState == ShizukuConnectionState.PERMISSION_REQUIRED

                val canContinue =
                    connectionState == ShizukuConnectionState.CONNECTED

                val message = when (connectionState) {
                    ShizukuConnectionState.CONNECTED ->
                        "Shizuku hazır."
                    ShizukuConnectionState.PERMISSION_REQUIRED ->
                        "Shizuku çalışıyor ama izin verilmedi. İzin iste."
                    ShizukuConnectionState.NOT_RUNNING ->
                        "Shizuku servisi çalışmıyor. Shizuku uygulamasını başlat."
                    ShizukuConnectionState.NOT_INSTALLED ->
                        "Shizuku yüklü değil. Lütfen Shizuku'yu indir ve kur."
                    ShizukuConnectionState.UNKNOWN ->
                        "Shizuku durumu kontrol ediliyor..."
                }

                _uiState.value = ShizukuSetupUiState(
                    isChecking = state.isChecking,
                    permissionGranted = permissionGranted,
                    shizukuAvailable = shizukuAvailable,
                    privilegeLevel = state.privilegeLevel,
                    canContinue = canContinue,
                    message = message
                )
            }
        }
    }

    fun check() {
        shizukuStateManager.refreshStatus()
    }

    fun requestPermission() {
        viewModelScope.launch {
            shizukuStateManager.requestPermission { granted ->
                check()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1001
    }
}
