package com.basitce.gfx.core.core_engine.shizuku

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shizuku tabanlı RemoteShell implementasyonu.
 *
 * Bu sınıf mümkün olduğunca kısa komutlar için tasarlandı:
 * id -u
 * test -r path
 * test -w path
 * am force-stop package
 * pidof package
 *
 * Büyük dosya transferleri için ShizukuPmpEngine kullanılmaya devam eder.
 */
@Singleton
class ShizukuRemoteShell @Inject constructor() : RemoteShell {

    private val shell = "/system/bin/sh"
    private val defaultTimeoutMs = 30_000L

    override suspend fun execute(command: String): ShellCommandResult = coroutineScope {
        val process = createProcess(command)

        if (process == null) {
            return@coroutineScope ShellCommandResult(
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku process oluşturulamadı."
            )
        }

        try {
            withTimeout(defaultTimeoutMs) {
                val stderrDeferred = async(Dispatchers.IO) {
                    readTextSafely(process.errorStream)
                }

                val stdout = withContext(Dispatchers.IO) {
                    readTextSafely(process.inputStream)
                }

                val exitCode = runInterruptible(Dispatchers.IO) {
                    process.waitFor()
                }

                val stderr = stderrDeferred.await()

                ShellCommandResult(
                    exitCode = exitCode,
                    stdout = stdout,
                    stderr = stderr
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Shizuku command timeout. command=$command")
            ShellCommandResult(
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku command timeout."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku command failed. command=$command", e)
            ShellCommandResult(
                exitCode = -1,
                stdout = "",
                stderr = e.message.orEmpty()
            )
        } finally {
            destroyProcess(process)
        }
    }

    private fun createProcess(command: String): ShizukuRemoteProcess? {
        return try {
            Shizuku.newProcess(
                arrayOf(shell, "-c", command),
                null,
                null
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(TAG, "Shizuku process oluşturulamadı. command=$command", e)
            null
        }
    }

    private fun destroyProcess(process: ShizukuRemoteProcess?) {
        try {
            process?.destroy()
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku process destroy edilemedi.", e)
        }
    }

    private fun readTextSafely(stream: InputStream?): String {
        return try {
            stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val TAG = "ShizukuRemoteShell"
    }
}
