package com.basitce.gfx.core.core_engine.shizuku

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shizuku bağlantı durumu.
 * Tüm ekranlar bu enum üzerinden durumu okur.
 */
enum class ShizukuConnectionState {
    /** Henüz kontrol edilmedi */
    UNKNOWN,
    /** Shizuku servisi çalışıyor ve izin verildi */
    CONNECTED,
    /** Shizuku çalışıyor ama izin verilmedi */
    PERMISSION_REQUIRED,
    /** Shizuku servisi çalışmıyor */
    NOT_RUNNING,
    /** Shizuku yüklü değil */
    NOT_INSTALLED
}

/**
 * Shizuku UI state'i.
 * Her ekran bu state'i collect eder.
 */
data class ShizukuUiState(
    val connectionState: ShizukuConnectionState = ShizukuConnectionState.UNKNOWN,
    val privilegeLevel: ShizukuPrivilegeLevel? = null,
    val isChecking: Boolean = false,
    val lastCheckedAt: Long = 0L,
    val errorMessage: String? = null
) {
    val isReady: Boolean
        get() = connectionState == ShizukuConnectionState.CONNECTED

    val needsAttention: Boolean
        get() = connectionState == ShizukuConnectionState.PERMISSION_REQUIRED ||
            connectionState == ShizukuConnectionState.NOT_RUNNING
}

/**
 * Merkezi Shizuku durum yöneticisi.
 *
 * Bu sınıf uygulama boyunca TEK Shizuku durum kaynağıdır.
 * Hiçbir ViewModel veya Screen doğrudan Shizuku API'sine erişmez.
 *
 * Sorumlulukları:
 * - Binder received/dead listener'larını yönetmek
 * - Permission request/result akışını yönetmek
 * - Capability (root/adb) seviyesini tespit etmek
 * - Tüm ekranlara StateFlow üzerinden durumu yayınlamak
 */
@Singleton
class ShizukuStateManager @Inject constructor(
    private val capabilityChecker: ShizukuCapabilityChecker
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(ShizukuUiState())
    val uiState: StateFlow<ShizukuUiState> = _uiState.asStateFlow()

    /** Permission sonucu callback'i için tek seferlik listener */
    private var pendingPermissionCallback: ((Boolean) -> Unit)? = null

    // ─── Shizuku Listener'ları ──────────────────────────
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received.")
        refreshStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "Shizuku binder dead.")
        _uiState.update {
            it.copy(
                connectionState = ShizukuConnectionState.NOT_RUNNING,
                privilegeLevel = null,
                errorMessage = null
            )
        }
    }

    private val permissionResultListener =
        object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(
                requestCode: Int,
                grantResult: Int
            ) {
                if (requestCode != PERMISSION_REQUEST_CODE) return
                val granted = grantResult == PackageManager.PERMISSION_GRANTED
                Log.d(TAG, "Shizuku permission result: granted=$granted")
                pendingPermissionCallback?.invoke(granted)
                pendingPermissionCallback = null
                refreshStatus()
            }
        }

    init {
        registerListeners()
        refreshStatus()
    }

    // ─── Public API ─────────────────────────────────────

    /**
     * Shizuku durumunu yeniden kontrol eder.
     * Her ekran açıldığında veya işlem öncesi çağrılabilir.
     */
    fun refreshStatus() {
        scope.launch {
            _uiState.update { it.copy(isChecking = true) }

            val connectionState = checkConnection()
            val privilegeLevel = if (connectionState == ShizukuConnectionState.CONNECTED) {
                checkCapability()
            } else {
                null
            }

            _uiState.update {
                it.copy(
                    connectionState = connectionState,
                    privilegeLevel = privilegeLevel,
                    isChecking = false,
                    lastCheckedAt = System.currentTimeMillis(),
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Shizuku izni ister.
     * Sonuç callback ile döner.
     */
    fun requestPermission(onResult: ((Boolean) -> Unit)? = null) {
        try {
            if (!Shizuku.pingBinder()) {
                onResult?.invoke(false)
                return
            }
            pendingPermissionCallback = onResult
            Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku permission request failed.", e)
            pendingPermissionCallback = null
            onResult?.invoke(false)
            _uiState.update {
                it.copy(
                    connectionState = ShizukuConnectionState.NOT_RUNNING,
                    errorMessage = "Shizuku izni istenemedi: ${e.message}"
                )
            }
        }
    }

    /**
     * Shizuku'nun o an kullanılabilir olup olmadığını senkron kontrol eder.
     * Ağır işlem değildir, sadece pingBinder + permission check.
     */
    fun isAvailableNow(): Boolean {
        return try {
            Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Listener'ları temizler.
     * Uygulama kapatılırken çağrılabilir (genellikle gerekmez).
     */
    fun dispose() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku listener cleanup failed.", e)
        }
    }

    // ─── Private ────────────────────────────────────────

    private fun registerListeners() {
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku listener registration failed.", e)
        }
    }

    private fun checkConnection(): ShizukuConnectionState {
        return try {
            val binderAlive = Shizuku.pingBinder()
            if (!binderAlive) {
                return ShizukuConnectionState.NOT_RUNNING
            }
            val permissionGranted =
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                ShizukuConnectionState.CONNECTED
            } else {
                ShizukuConnectionState.PERMISSION_REQUIRED
            }
        } catch (e: IllegalStateException) {
            ShizukuConnectionState.NOT_RUNNING
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku connection check failed.", e)
            ShizukuConnectionState.NOT_RUNNING
        }
    }

    private suspend fun checkCapability(): ShizukuPrivilegeLevel? {
        return try {
            val capability = capabilityChecker.check()
            if (capability.available) capability.privilegeLevel else null
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku capability check failed.", e)
            null
        }
    }

    companion object {
        private const val TAG = "ShizukuStateManager"
        const val PERMISSION_REQUEST_CODE = 1001
    }
}
