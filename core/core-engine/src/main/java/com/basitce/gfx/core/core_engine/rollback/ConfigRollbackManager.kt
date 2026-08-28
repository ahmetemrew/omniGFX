package com.basitce.gfx.core.core_engine.rollback

import android.content.Context
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.shizuku.RemoteShell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class BackupEntry(
    val path: String,
    val fileName: String,
    val timestamp: Long?,
    val size: Long?
)

data class BackupList(
    val backups: List<BackupEntry>,
    val warnings: List<String>
)

sealed class RollbackResult {

    data class Success(
        val message: String,
        val warnings: List<String> = emptyList()
    ) : RollbackResult()

    data class Failure(
        val message: String,
        val warnings: List<String> = emptyList(),
        val cause: Throwable? = null
    ) : RollbackResult()
}

/**
 * ConfigEngine tarafından oluşturulan remote backup'ları yönetir.
 *
 * Backup isim formatı ConfigEngine'de şu şekildedir:
 * .<safeFileName>.omnigfx.<timestamp>.bak
 */
@Singleton
class ConfigRollbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pmpEngine: PmpEngine,
    private val remoteShell: RemoteShell
) {

    suspend fun listBackupsForTarget(targetPath: String): BackupList {
        val warnings = mutableListOf<String>()

        if (targetPath.isBlank()) {
            return BackupList(
                backups = emptyList(),
                warnings = warnings + "Target path boş."
            )
        }

        val parentDir = parentPath(targetPath)
        val quotedParent = shellQuote(parentDir)

        val lsResult = remoteShell.execute("ls -1 -a $quotedParent")

        if (!lsResult.isSuccess) {
            warnings.add("ls başarısız: ${lsResult.stderr}")

            return BackupList(
                backups = emptyList(),
                warnings = warnings
            )
        }

        val targetFileName = targetPath
            .substringAfterLast('/')
            .ifBlank { "config" }

        val safeName = safeFileName(targetFileName)
        val backupPrefix = ".$safeName.omnigfx."

        val backupEntries = lsResult.stdout
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { it != "." && it != ".." }
            .filter { it.startsWith(backupPrefix) && it.endsWith(".bak") }
            .map { fileName ->
                val timestampText = fileName
                    .removePrefix(backupPrefix)
                    .removeSuffix(".bak")

                BackupEntry(
                    path = joinPath(parentDir, fileName),
                    timestamp = timestampText.toLongOrNull(),
                    size = null,
                    fileName = fileName
                )
            }
            .sortedByDescending { it.timestamp ?: 0L }

        return BackupList(
            backups = backupEntries,
            warnings = warnings
        )
    }

    suspend fun restoreLatest(targetPath: String): RollbackResult {
        val list = listBackupsForTarget(targetPath)

        val latestBackup = list.backups.firstOrNull()

        if (latestBackup == null) {
            return RollbackResult.Failure(
                message = "Hedef dosya için backup bulunamadı.",
                warnings = list.warnings
            )
        }

        return restoreBackup(
            backupPath = latestBackup.path,
            targetPath = targetPath
        )
    }

    suspend fun restoreBackup(
        backupPath: String,
        targetPath: String
    ): RollbackResult {
        val warnings = mutableListOf<String>()

        if (!isLikelyBackupPath(backupPath)) {
            return RollbackResult.Failure(
                message = "Path backup dosyasına benzemiyor: $backupPath",
                warnings = warnings
            )
        }

        // Restore öncesi mümkünse hedef metadata'sını al.
        // Hedef yoksa backup metadata'sını kullan.
        val metadata = try {
            pmpEngine.stat(targetPath) ?: pmpEngine.stat(backupPath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Metadata alınamadı: ${e.message}")
            null
        }

        // 1) PmpEngine remote copy destekliyorsa önce onu dene.
        val remoteCopySucceeded = if (pmpEngine.supportsRemoteCopy) {
            try {
                pmpEngine.copyRemote(
                    sourceRemotePath = backupPath,
                    destinationRemotePath = targetPath
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                warnings.add("copyRemote sırasında hata: ${e.message}")
                false
            }
        } else {
            false
        }

        if (remoteCopySucceeded) {
            applyMetadata(
                path = targetPath,
                metadata = metadata,
                warnings = warnings
            )

            return RollbackResult.Success(
                message = "Backup remote copy ile geri yüklendi.",
                warnings = warnings
            )
        }

        // 2) Shell cat fallback
        val quotedBackup = shellQuote(backupPath)
        val quotedTarget = shellQuote(targetPath)

        val catResult = remoteShell.execute("cat $quotedBackup > $quotedTarget")

        if (catResult.isSuccess) {
            applyMetadata(
                path = targetPath,
                metadata = metadata,
                warnings = warnings
            )

            return RollbackResult.Success(
                message = "Backup shell cat ile geri yüklendi.",
                warnings = warnings
            )
        }

        warnings.add("Shell cat fallback başarısız: ${catResult.stderr}")

        // 3) Pull -> push fallback
        val tempFile = File(
            context.cacheDir,
            "omnigfx_restore_${System.currentTimeMillis()}"
        )

        return try {
            val pulled = pmpEngine.pull(
                remotePath = backupPath,
                destination = tempFile
            )

            if (!pulled || !tempFile.exists()) {
                return RollbackResult.Failure(
                    message = "Backup cache'e çekilemedi.",
                    warnings = warnings
                )
            }

            val pushed = pmpEngine.push(
                source = tempFile,
                remotePath = targetPath
            )

            if (!pushed) {
                return RollbackResult.Failure(
                    message = "Backup hedefe push edilemedi.",
                    warnings = warnings
                )
            }

            applyMetadata(
                path = targetPath,
                metadata = metadata,
                warnings = warnings
            )

            RollbackResult.Success(
                message = "Backup pull/push fallback ile geri yüklendi.",
                warnings = warnings
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RollbackResult.Failure(
                message = "Restore sırasında hata oluştu.",
                warnings = warnings,
                cause = e
            )
        } finally {
            tempFile.delete()
        }
    }

    suspend fun deleteBackup(backupPath: String): RollbackResult {
        if (!isLikelyBackupPath(backupPath)) {
            return RollbackResult.Failure(
                message = "Path backup dosyasına benzemiyor: $backupPath"
            )
        }

        val quotedPath = shellQuote(backupPath)
        val result = remoteShell.execute("rm -f $quotedPath")

        return if (result.isSuccess) {
            RollbackResult.Success(
                message = "Backup silindi: $backupPath"
            )
        } else {
            RollbackResult.Failure(
                message = "Backup silinemedi: $backupPath",
                warnings = listOf(result.stderr)
            )
        }
    }

    suspend fun cleanOldBackups(
        targetPath: String,
        keep: Int
    ): RollbackResult {
        if (keep < 1) {
            return RollbackResult.Failure(
                message = "keep değeri en az 1 olmalıdır."
            )
        }

        val list = listBackupsForTarget(targetPath)

        if (list.backups.size <= keep) {
            return RollbackResult.Success(
                message = "Temizlenecek eski backup yok.",
                warnings = list.warnings
            )
        }

        val toDelete = list.backups.drop(keep)
        val warnings = list.warnings.toMutableList()

        var deletedCount = 0

        for (backup in toDelete) {
            when (val deleteResult = deleteBackup(backup.path)) {
                is RollbackResult.Success -> deletedCount++

                is RollbackResult.Failure -> {
                    warnings.add("Backup silinemedi: ${backup.path}")
                    warnings.addAll(deleteResult.warnings)
                }
            }
        }

        return RollbackResult.Success(
            message = "$deletedCount eski backup silindi.",
            warnings = warnings
        )
    }

    private suspend fun applyMetadata(
        path: String,
        metadata: com.basitce.gfx.core.core_engine.FileMetadata?,
        warnings: MutableList<String>
    ) {
        if (metadata == null) return

        try {
            val uid = metadata.uid
            val gid = metadata.gid

            if (uid != null && gid != null) {
                if (!pmpEngine.chown(path, uid, gid)) {
                    warnings.add("Restore sonrası chown başarısız: $path")
                }
            }

            metadata.mode?.let { mode ->
                if (!pmpEngine.chmod(path, mode)) {
                    warnings.add("Restore sonrası chmod başarısız: $path")
                }
            }

            if (pmpEngine.supportsSelinux) {
                metadata.seContext?.let { context ->
                    if (!pmpEngine.chcon(path, context)) {
                        warnings.add("Restore sonrası chcon başarısız: $path")
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Restore metadata sırasında hata: ${e.message}")
        }
    }

    private fun isLikelyBackupPath(path: String): Boolean {
        return path.contains(".omnigfx.") && path.endsWith(".bak")
    }

    private fun parentPath(path: String): String {
        if (!path.contains("/")) return "."

        val parent = path.substringBeforeLast('/')

        return if (parent.isBlank()) "/" else parent
    }

    private fun joinPath(parent: String, child: String): String {
        if (parent.endsWith("/")) {
            return parent + child
        }

        return "$parent/$child"
    }

    private fun safeFileName(fileName: String): String {
        return fileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(64)
            .ifBlank { "config" }
    }

    private fun shellQuote(value: String): String {
        val escaped = value.replace("'", "'\\''")
        return "'$escaped'"
    }
}
