package com.basitce.gfx.core.core_engine.pipeline

import android.content.Context
import com.basitce.gfx.core.core_engine.ConfigParserFactory
import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.verification.PmpVerifier
import com.basitce.gfx.core.core_engine.verification.VerifyOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PmpPipeline @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pmpEngine: PmpEngine,
    private val parserFactory: ConfigParserFactory,
    private val verifier: PmpVerifier
) {
    fun execute(request: PmpRequest): Flow<PmpEvent> = flow {
        emit(PmpEvent.Started)
        val warnings = mutableListOf<String>()
        val stamp = System.currentTimeMillis()
        val cacheDir = File(context.cacheDir, "omnigfx_pmp").apply { mkdirs() }
        val safeName = request.remotePath
            .substringAfterLast('/')
            .ifBlank { "config" }
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(64)
            .ifBlank { "config" }
        val pulledFile = File(cacheDir, "pull_${stamp}_$safeName")
        val modifiedFile = File(cacheDir, "modified_${stamp}_$safeName")
        try {
            // ═══ AŞAMA 1: PULL ═══
            emit(PmpEvent.Pulling)
            val metadata = safeStat(request.remotePath, warnings)

            if (request.options.createRemoteBackup) {
                val backupResult = createRemoteBackup(request, warnings)
                if (backupResult == BackupOutcome.FAILED_ABORT) {
                    emit(PmpEvent.Failed(
                        "Remote backup oluşturulamadığı için işlem iptal edildi."
                    ))
                    return@flow
                }
            }

            val pullSucceeded = try {
                pmpEngine.pull(request.remotePath, pulledFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                warnings.add("Pull sırasında hata: ${e.message}")
                false
            }
            if (!pullSucceeded || !pulledFile.exists()) {
                emit(PmpEvent.Failed("Dosya çekilemedi: ${request.remotePath}"))
                return@flow
            }

            // ═══ AŞAMA 2: DECODE + MODIFY ═══
            emit(PmpEvent.Decoding)
            val originalContent = withContext(Dispatchers.IO) {
                pulledFile.readText(Charsets.UTF_8)
            }

            emit(PmpEvent.Modifying)
            val updatedContent: String = when (request.patchMode) {
                PmpPatchMode.CONFIG_PARSER -> {
                    modifyWithParser(request, originalContent, warnings) ?: run {
                        emit(PmpEvent.Failed("Config parse/modify başarısız."))
                        return@flow
                    }
                }
                PmpPatchMode.REGEX -> {
                    modifyWithRegex(request, originalContent, warnings)
                }
            }

            if (updatedContent == originalContent) {
                pulledFile.delete()
                emit(PmpEvent.Completed(PmpResult.NoChange))
                return@flow
            }

            emit(PmpEvent.Encoding)
            withContext(Dispatchers.IO) {
                modifiedFile.writeText(updatedContent, Charsets.UTF_8)
            }

            // ═══ AŞAMA 3: PUSH + VERIFY ═══
            emit(PmpEvent.Pushing)
            val pushSucceeded = try {
                if (request.options.atomicReplace && pmpEngine.supportsAtomicPush) {
                    pmpEngine.pushAtomic(
                        source = modifiedFile,
                        remotePath = request.remotePath,
                        metadata = metadata,
                        restoreSelinux = request.options.restoreSelinux
                    )
                } else {
                    pmpEngine.push(
                        source = modifiedFile,
                        remotePath = request.remotePath
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                warnings.add("Push sırasında hata: ${e.message}")
                false
            }
            if (!pushSucceeded) {
                emit(PmpEvent.Failed(
                    "Dosya hedefe push edilemedi: ${request.remotePath}"
                ))
                return@flow
            }

            restoreMetadata(request.remotePath, metadata, request.options, warnings)

            if (request.options.verifyAfterPush) {
                emit(PmpEvent.Verifying)
                val verifyResult = try {
                    verifier.verifyContent(
                        expectedContent = updatedContent,
                        remotePath = request.remotePath,
                        expectedMetadata = metadata,
                        options = VerifyOptions(
                            requireHash = request.options.requireHashVerification,
                            requireSize = false,
                            requireMetadata = false,
                            requireSelinux = false
                        )
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    warnings.add("Verify sırasında hata: ${e.message}")
                    null
                }

                // ── KRİTİK DÜZELTME ──
                // Doğrulama başarısızsa işlemi DURDUR, sadece warning ekleme.
                if (verifyResult != null && !verifyResult.success) {
                    warnings.add("Push doğrulanamadı: ${verifyResult.message}")
                    emit(PmpEvent.Failed(
                        "Push doğrulanamadı. Dosya bozuk olabilir: ${verifyResult.message}",
                        RuntimeException("Verification failed")
                    ))
                    return@flow
                }
            }

            emit(PmpEvent.Completed(
                PmpResult.Success(
                    originalContent = originalContent,
                    updatedContent = updatedContent,
                    remoteBackupPath = null,
                    warnings = warnings.toList()
                )
            ))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(PmpEvent.Failed(
                e.message ?: "PmpPipeline bilinmeyen hata.", e
            ))
        } finally {
            withContext(Dispatchers.IO) {
                pulledFile.delete()
                modifiedFile.delete()
            }
        }
    }

    private suspend fun modifyWithParser(
        request: PmpRequest,
        originalContent: String,
        warnings: MutableList<String>
    ): String? {
        val parser = try {
            parserFactory.create(request.remotePath, originalContent)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Parser oluşturulamadı: ${e.message}")
            return null
        }
        try {
            parser.parse(originalContent)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Parse başarısız: ${e.message}")
            return null
        }
        val patchErrors = mutableListOf<String>()
        for (patch in request.patches) {
            try {
                parser.updateValue(patch.path, patch.value)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                patchErrors.add("${patch.path}: ${e.message}")
            }
        }
        if (patchErrors.isNotEmpty()) {
            warnings.addAll(patchErrors)
            return null
        }
        return try {
            parser.serialize()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Serialize başarısız: ${e.message}")
            null
        }
    }

    private fun modifyWithRegex(
        request: PmpRequest,
        originalContent: String,
        warnings: MutableList<String>
    ): String {
        var modified = originalContent
        for (patch in request.patches) {
            val pattern = patch.regexPattern
            val template = patch.regexReplacementTemplate
            if (pattern == null || template == null) {
                warnings.add("Regex patch eksik: ${patch.path}")
                continue
            }
            try {
                val userValue = when (val v = patch.value) {
                    is Boolean -> if (v) "1" else "0"
                    else -> v.toString()
                }
                val replacement = template.replace("{{value}}", userValue)
                val regex = Regex(pattern)
                modified = modified.replace(regex, replacement)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                warnings.add("Regex patch başarısız: ${patch.path} | ${e.message}")
            }
        }
        return modified
    }

    private suspend fun safeStat(
        path: String,
        warnings: MutableList<String>
    ): FileMetadata? {
        return try {
            pmpEngine.stat(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("stat alınamadı: ${e.message}")
            null
        }
    }

    private enum class BackupOutcome {
        SUCCESS,
        FAILED_CONTINUE,
        FAILED_ABORT
    }

    private suspend fun createRemoteBackup(
        request: PmpRequest,
        warnings: MutableList<String>
    ): BackupOutcome {
        if (!pmpEngine.supportsRemoteCopy) {
            warnings.add("PmpEngine remote copy desteklemiyor.")
            return if (request.options.abortIfBackupFails) {
                BackupOutcome.FAILED_ABORT
            } else {
                BackupOutcome.FAILED_CONTINUE
            }
        }
        val backupPath = createRemoteBackupPath(request.remotePath)
        val succeeded = try {
            pmpEngine.copyRemote(request.remotePath, backupPath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Remote backup sırasında hata: ${e.message}")
            false
        }
        return if (succeeded) {
            BackupOutcome.SUCCESS
        } else {
            warnings.add("Remote backup oluşturulamadı: $backupPath")
            if (request.options.abortIfBackupFails) {
                BackupOutcome.FAILED_ABORT
            } else {
                BackupOutcome.FAILED_CONTINUE
            }
        }
    }

    private suspend fun restoreMetadata(
        remotePath: String,
        metadata: FileMetadata?,
        options: PmpPipelineOptions,
        warnings: MutableList<String>
    ) {
        if (metadata == null) return
        try {
            val uid = metadata.uid
            val gid = metadata.gid
            if (uid != null && gid != null) {
                if (!pmpEngine.chown(remotePath, uid, gid)) {
                    warnings.add("chown geri yüklenemedi: $remotePath")
                }
            }
            metadata.mode?.let { mode ->
                if (!pmpEngine.chmod(remotePath, mode)) {
                    warnings.add("chmod geri yüklenemedi: $remotePath")
                }
            }
            if (options.restoreSelinux && pmpEngine.supportsSelinux) {
                metadata.seContext?.let { ctx ->
                    if (!pmpEngine.chcon(remotePath, ctx)) {
                        warnings.add("chcon geri yüklenemedi: $remotePath")
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Metadata restore sırasında hata: ${e.message}")
        }
    }

    private fun createRemoteBackupPath(remotePath: String): String {
        val parent = remotePath.substringBeforeLast('/', "")
        val fileName = remotePath
            .substringAfterLast('/')
            .ifBlank { "config" }
        val safeFileName = fileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(64)
            .ifBlank { "config" }
        val backupName = ".$safeFileName.omnigfx.${System.currentTimeMillis()}.bak"
        return if (parent.isEmpty()) "/$backupName" else "$parent/$backupName"
    }
}
