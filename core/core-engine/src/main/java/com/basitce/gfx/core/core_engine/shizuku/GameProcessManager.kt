package com.basitce.gfx.core.core_engine.shizuku

import javax.inject.Inject
import javax.inject.Singleton

interface GameProcessManager {

    suspend fun isRunning(packageName: String): Boolean

    suspend fun forceStop(packageName: String): Boolean

    suspend fun launch(packageName: String): Boolean
}

/**
 * Shizuku üzerinden oyun sürecini yönetir.
 *
 * am force-stop
 * pidof / pgrep
 * monkey launch
 */
@Singleton
class ShizukuGameProcessManager @Inject constructor(
    private val remoteShell: RemoteShell
) : GameProcessManager {

    override suspend fun isRunning(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false

        val pidofResult = remoteShell.execute("pidof $packageName")
        if (pidofResult.isSuccess) return true

        val pgrepResult = remoteShell.execute("pgrep -f $packageName")
        return pgrepResult.isSuccess
    }

    override suspend fun forceStop(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false

        val result = remoteShell.execute("am force-stop $packageName")
        return result.isSuccess
    }

    override suspend fun launch(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false

        val result = remoteShell.execute(
            "monkey -p $packageName -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1"
        )

        return result.isSuccess
    }

    private fun isValidPackageName(packageName: String): Boolean {
        return packageName.matches(PACKAGE_NAME_REGEX)
    }

    companion object {
        private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*$")
    }
}
