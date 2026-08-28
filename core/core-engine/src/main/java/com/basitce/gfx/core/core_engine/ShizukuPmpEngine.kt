package com.basitce.gfx.core.core_engine

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shizuku tabanlı gelişmiş PMP engine.
 *
 * Özellikler:
 * - stat ile uid/gid/mode/SELinux context alma
 * - pull ile remote -> cache
 * - push ile güvenli temp + cat fallback
 * - pushAtomic ile temp + metadata + mv atomic replace
 * - copyRemote ile cihaz içinde remote backup
 * - chown/chmod/chcon ile metadata geri yükleme
 */
@Singleton
class ShizukuPmpEngine @Inject constructor() : PmpEngine {

    override val supportsAtomicPush: Boolean
        get() = true

    override val supportsRemoteCopy: Boolean
        get() = true

    override val supportsSelinux: Boolean
        get() = true

    private val shell = "/system/bin/sh"

    override suspend fun stat(remotePath: String): FileMetadata? {
        if (remotePath.isBlank()) return null

        val quotedPath = shellQuote(remotePath)

        // Önce SELinux context dahil almaya çalış.
        val contextResult = executeCapture(
            "stat -c '%u %g %a %C' $quotedPath"
        )

        if (contextResult.exitCode == 0) {
            parseStat(contextResult.stdout)?.let { return it }
        }

        // Fallback: sadece uid/gid/mode
        val simpleResult = executeCapture(
            "stat -c '%u %g %a' $quotedPath"
        )

        if (simpleResult.exitCode == 0) {
            parseStat(simpleResult.stdout)?.let { return it }
        }

        Log.w(
            TAG,
            "stat alınamadı. path=$remotePath stderr=${simpleResult.stderr}"
        )

        return null
    }

    override suspend fun pull(
        remotePath: String,
        destination: File
    ): Boolean = coroutineScope {
        if (remotePath.isBlank()) {
            return@coroutineScope false
        }

        try {
            destination.parentFile?.mkdirs()

            val process = createProcess("cat ${shellQuote(remotePath)}")
            if (process == null) {
                Log.e(TAG, "pull için Shizuku process oluşturulamadı.")
                return@coroutineScope false
            }

            val stderrDeferred = async(Dispatchers.IO) {
                readTextSafely(process.errorStream)
            }

            try {
                withContext(Dispatchers.IO) {
                    destination.outputStream().use { outputStream ->
                        process.inputStream.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                val exitCode = waitForExit(process)
                val stderr = stderrDeferred.await()

                if (exitCode != 0) {
                    Log.w(
                        TAG,
                        "pull başarısız. path=$remotePath exit=$exitCode stderr=$stderr"
                    )
                    return@coroutineScope false
                }

                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "pull sırasında hata oluştu.", e)
                false
            } finally {
                destroyProcess(process)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "pull dış hata.", e)
            false
        }
    }

    /**
     * Basit push.
     *
     * İçeriği stdin üzerinden hedefe yazar.
     * Base64 echo kullanılmaz — büyük dosyalarda ARG_MAX aşımı olmaz.
     * Hedef dosyanın inode'u korunur.
     */
    override suspend fun push(
        source: File,
        remotePath: String
    ): Boolean = coroutineScope {
        if (!source.exists() || !source.isFile) {
            Log.w(TAG, "push kaynak dosyası geçersiz: ${source.absolutePath}")
            return@coroutineScope false
        }
        if (remotePath.isBlank()) {
            return@coroutineScope false
        }

        val metadata = stat(remotePath)
        val quotedRemote = shellQuote(remotePath)

        // stdin üzerinden güvenli yazma
        val writeSucceeded = writeFileViaStdin(
            source = source,
            command = "cat > $quotedRemote"
        )

        if (!writeSucceeded) {
            Log.w(TAG, "push hedefe yazılamadı. remote=$remotePath")
            return@coroutineScope false
        }

        // Metadata'yı (sahip, izin, SELinux) geri yükle.
        applyMetadataToPath(
            path = remotePath,
            metadata = metadata,
            restoreSelinux = true
        )
        true
    }

    /**
     * Atomic push.
     *
     * 1. Aynı dizinde temp dosya oluştur.
     * 2. İçeriği temp dosyaya yaz.
     * 3. uid/gid/mode/SELinux context değerlerini temp dosyaya uygula.
     * 4. mv -f ile hedefi atomik değiştir.
     * 5. mv başarısız olursa inode koruyan fallback'e geç.
     */
    override suspend fun pushAtomic(
        source: File,
        remotePath: String,
        metadata: FileMetadata?,
        restoreSelinux: Boolean
    ): Boolean = coroutineScope {
        if (!source.exists() || !source.isFile) {
            Log.w(TAG, "pushAtomic kaynak dosyası geçersiz: ${source.absolutePath}")
            return@coroutineScope false
        }

        if (remotePath.isBlank()) {
            return@coroutineScope false
        }

        val effectiveMetadata = metadata ?: stat(remotePath)

        val tempRemotePath = createTempRemotePath(remotePath)
        val quotedTemp = shellQuote(tempRemotePath)
        val quotedRemote = shellQuote(remotePath)

        // 1) Temp dosyaya yaz.
        val tempWriteSucceeded = writeFileViaStdin(
            source = source,
            command = "cat > $quotedTemp"
        )

        if (!tempWriteSucceeded) {
            Log.w(TAG, "pushAtomic temp dosyaya yazılamadı. temp=$tempRemotePath")
            executeCapture("rm -f $quotedTemp")
            return@coroutineScope false
        }

        // 2) Metadata'yı rename öncesinde temp dosyaya uygula.
        applyMetadataToPath(
            path = tempRemotePath,
            metadata = effectiveMetadata,
            restoreSelinux = restoreSelinux
        )

        // 3) Atomic rename.
        val renameResult = executeCapture("mv -f $quotedTemp $quotedRemote")

        if (renameResult.exitCode == 0) {
            // Rename sonrası ekstra güvenlik olarak metadata'yı tekrar uygula.
            applyMetadataToPath(
                path = remotePath,
                metadata = effectiveMetadata,
                restoreSelinux = restoreSelinux
            )

            return@coroutineScope true
        }

        Log.w(
            TAG,
            "pushAtomic mv başarısız, fallback'e geçiliyor. " +
                "temp=$tempRemotePath remote=$remotePath stderr=${renameResult.stderr}"
        )

        // 4) Fallback: inode koruyan cat yöntemi.
        val fallbackCopyResult = executeCapture("cat $quotedTemp > $quotedRemote")

        // 5) Temp temizle.
        executeCapture("rm -f $quotedTemp")

        if (fallbackCopyResult.exitCode != 0) {
            Log.w(
                TAG,
                "pushAtomic fallback başarısız. remote=$remotePath stderr=${fallbackCopyResult.stderr}"
            )
            return@coroutineScope false
        }

        // 6) Fallback sonrası metadata'yı uygula.
        applyMetadataToPath(
            path = remotePath,
            metadata = effectiveMetadata,
            restoreSelinux = restoreSelinux
        )

        true
    }

    override suspend fun chown(
        remotePath: String,
        uid: Int,
        gid: Int
    ): Boolean {
        if (remotePath.isBlank()) return false
        if (uid < 0 || gid < 0) return false

        val command = "chown $uid:$gid ${shellQuote(remotePath)}"
        val result = executeCapture(command)

        if (result.exitCode != 0) {
            Log.w(
                TAG,
                "chown başarısız. path=$remotePath uid=$uid gid=$gid stderr=${result.stderr}"
            )
        }

        return result.exitCode == 0
    }

    override suspend fun chmod(
        remotePath: String,
        mode: String
    ): Boolean {
        if (remotePath.isBlank()) return false

        if (!mode.matches(Regex("^[0-7]{3,4}$"))) {
            Log.w(TAG, "chmod geçersiz mode: $mode")
            return false
        }

        val command = "chmod $mode ${shellQuote(remotePath)}"
        val result = executeCapture(command)

        if (result.exitCode != 0) {
            Log.w(
                TAG,
                "chmod başarısız. path=$remotePath mode=$mode stderr=${result.stderr}"
            )
        }

        return result.exitCode == 0
    }

    override suspend fun chcon(
        remotePath: String,
        context: String
    ): Boolean {
        if (remotePath.isBlank() || context.isBlank()) return false

        val command = "chcon ${shellQuote(context)} ${shellQuote(remotePath)}"
        val result = executeCapture(command)

        if (result.exitCode != 0) {
            Log.w(
                TAG,
                "chcon başarısız. path=$remotePath context=$context stderr=${result.stderr}"
            )
        }

        return result.exitCode == 0
    }

    /**
     * Remote backup için kullanılır.
     *
     * Önce cp -p -f dener.
     * Başarısız olursa cat ile fallback yapar.
     */
    override suspend fun copyRemote(
        sourceRemotePath: String,
        destinationRemotePath: String
    ): Boolean {
        if (sourceRemotePath.isBlank() || destinationRemotePath.isBlank()) {
            return false
        }

        if (sourceRemotePath == destinationRemotePath) {
            return true
        }

        val metadata = stat(sourceRemotePath)

        val quotedSource = shellQuote(sourceRemotePath)
        val quotedDestination = shellQuote(destinationRemotePath)

        // 1) cp ile hızlı copy + metadata koruma.
        val copyResult = executeCapture("cp -p -f $quotedSource $quotedDestination")

        if (copyResult.exitCode == 0) {
            applyMetadataToPath(
                path = destinationRemotePath,
                metadata = metadata,
                restoreSelinux = true
            )

            return true
        }

        // 2) Fallback: cat source > destination
        val fallbackResult = executeCapture("cat $quotedSource > $quotedDestination")

        if (fallbackResult.exitCode != 0) {
            Log.w(
                TAG,
                "copyRemote başarısız. source=$sourceRemotePath " +
                    "dest=$destinationRemotePath stderr=${fallbackResult.stderr}"
            )
            return false
        }

        applyMetadataToPath(
            path = destinationRemotePath,
            metadata = metadata,
            restoreSelinux = true
        )

        return true
    }

    // region Private helpers

    private suspend fun writeFileViaStdin(
        source: File,
        command: String
    ): Boolean = coroutineScope {
        val process = createProcess(command)
        if (process == null) {
            Log.e(TAG, "writeFileViaStdin için process oluşturulamadı.")
            return@coroutineScope false
        }

        val stderrDeferred = async(Dispatchers.IO) {
            readTextSafely(process.errorStream)
        }

        try {
            withContext(Dispatchers.IO) {
                process.outputStream.use { outputStream ->
                    source.inputStream().use { inputStream ->
                        val copied = inputStream.copyTo(outputStream)
                        outputStream.flush()
                        Log.d(TAG, "writeFileViaStdin: $copied byte kopyalandı.")
                    }
                }
            }

            val exitCode = waitForExit(process)
            val stderr = stderrDeferred.await()

            Log.d(
                TAG,
                "writeFileViaStdin sonuç. command=$command exit=$exitCode stderr=$stderr"
            )

            exitCode == 0
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "writeFileViaStdin sırasında hata oluştu.", e)
            false
        } finally {
            destroyProcess(process)
        }
    }

    private suspend fun executeCapture(command: String): ShellResult = coroutineScope {
        val process = createProcess(command)
        if (process == null) {
            return@coroutineScope ShellResult(
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku process oluşturulamadı."
            )
        }

        val stderrDeferred = async(Dispatchers.IO) {
            readTextSafely(process.errorStream)
        }

        try {
            val stdout = withContext(Dispatchers.IO) {
                readTextSafely(process.inputStream)
            }

            val exitCode = waitForExit(process)

            val stderr = try {
                stderrDeferred.await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ""
            }

            ShellResult(
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ShellResult(
                exitCode = -1,
                stdout = "",
                stderr = e.message.orEmpty()
            )
        } finally {
            destroyProcess(process)
        }
    }

    private suspend fun waitForExit(process: ShizukuRemoteProcess): Int {
        return try {
            runInterruptible(Dispatchers.IO) {
                process.waitFor()
            }
        } catch (e: CancellationException) {
            destroyProcess(process)
            throw e
        } catch (e: InterruptedException) {
            destroyProcess(process)
            -1
        } catch (e: Exception) {
            Log.e(TAG, "waitForExit sırasında hata oluştu.", e)
            -1
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
            Log.w(TAG, "Process destroy edilemedi.", e)
        }
    }

    private fun readTextSafely(stream: InputStream?): String {
        return try {
            stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun applyMetadataToPath(
        path: String,
        metadata: FileMetadata?,
        restoreSelinux: Boolean
    ): Boolean {
        if (metadata == null) return true

        var succeeded = true

        val uid = metadata.uid
        val gid = metadata.gid

        if (uid != null && gid != null) {
            succeeded = chown(path, uid, gid) && succeeded
        }

        metadata.mode?.let { mode ->
            succeeded = chmod(path, mode) && succeeded
        }

        if (restoreSelinux) {
            metadata.seContext?.let { context ->
                succeeded = chcon(path, context) && succeeded
            }
        }

        return succeeded
    }

    private fun parseStat(output: String): FileMetadata? {
        val parts = output.trim().split(Regex("\\s+"))

        if (parts.size < 3) return null

        val uid = parts[0].toIntOrNull() ?: return null
        val gid = parts[1].toIntOrNull() ?: return null

        val mode = parts[2].takeIf {
            it.matches(Regex("^[0-7]{3,4}$"))
        } ?: return null

        val seContext = parts.getOrNull(3)
            ?.takeIf { context ->
                context.isNotBlank() && context != "?" && context != "-"
            }

        return FileMetadata(
            uid = uid,
            gid = gid,
            mode = mode,
            seContext = seContext
        )
    }

    private fun createTempRemotePath(remotePath: String): String {
        val parent = remotePath.substringBeforeLast('/', "")
        val tempName = ".omnigfx_tmp_${System.currentTimeMillis()}"

        return if (parent.isEmpty()) {
            "/$tempName"
        } else {
            "$parent/$tempName"
        }
    }

    /**
     * Shell injection koruması.
     */
    private fun shellQuote(value: String): String {
        val escaped = value.replace("'", "'\\''")
        return "'$escaped'"
    }

    private data class ShellResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    )

    companion object {
        private const val TAG = "ShizukuPmpEngine"
    }

    // endregion
}
