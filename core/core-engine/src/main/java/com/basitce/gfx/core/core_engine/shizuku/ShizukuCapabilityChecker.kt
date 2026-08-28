package com.basitce.gfx.core.core_engine.shizuku

import javax.inject.Inject
import javax.inject.Singleton

enum class ShizukuPrivilegeLevel {
    NOT_AVAILABLE,
    ADB_SHELL,
    ROOT,
    UNKNOWN
}

data class ShizukuCapability(
    val available: Boolean,
    val uid: Int?,
    val privilegeLevel: ShizukuPrivilegeLevel,
    val rawOutput: String?
)

/**
 * Shizuku'nun o anki yetki seviyesini tespit eder.
 *
 * uid=0   -> root
 * uid=2000 -> adb shell
 */
@Singleton
class ShizukuCapabilityChecker @Inject constructor(
    private val remoteShell: RemoteShell
) {

    suspend fun check(): ShizukuCapability {
        val result = remoteShell.execute("id -u")

        if (!result.isSuccess) {
            return ShizukuCapability(
                available = false,
                uid = null,
                privilegeLevel = ShizukuPrivilegeLevel.NOT_AVAILABLE,
                rawOutput = result.stderr.ifBlank { null }
            )
        }

        val uidText = result.stdout.trim()
        val uid = uidText.toIntOrNull()

        val privilegeLevel = when (uid) {
            0 -> ShizukuPrivilegeLevel.ROOT
            2000 -> ShizukuPrivilegeLevel.ADB_SHELL
            null -> ShizukuPrivilegeLevel.UNKNOWN
            else -> ShizukuPrivilegeLevel.UNKNOWN
        }

        return ShizukuCapability(
            available = true,
            uid = uid,
            privilegeLevel = privilegeLevel,
            rawOutput = uidText
        )
    }
}
