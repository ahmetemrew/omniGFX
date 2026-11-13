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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
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
    private val writeMutex = Mutex()

    fun execute(request: PmpRequest): Flow<PmpEvent> = flow {
        writeMutex.withLock { executeLocked(request).collect { emit(it) } }
    }

    private fun executeLocked(request: PmpRequest): Flow<PmpEvent> = flow {
        emit(PmpEvent.Started)
        val warnings = mutableListOf<String>()
        val stamp = UUID.randomUUID().toString()
        val cacheDir = File(context.cacheDir, "omnigfx_pmp").apply { mkdirs() }
        val safeName = request.remotePath
            .substringAfterLast('/')
            .ifBlank { "config" }
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(64)
            .ifBlank { "config" }
        val pulledFile = File(cacheDir, "pull_${stamp}_$safeName")
        val modifiedFile = File(cacheDir, "modified_${stamp}_$safeName")
        var writeAttempted = false
        var originalForRollback: String? = null
        var metadataForRollback: FileMetadata? = null
        var remoteBackupPath: String? = null
        try {
            require(request.remotePath.startsWith("/") && !request.remotePath.contains('\u0000')) { "Mutlak bir hedef dosya yolu gerekli." }
            // ═══ AŞAMA 1: PULL ═══
            emit(PmpEvent.Pulling)
            val metadata = safeStat(request.remotePath, warnings)
            metadataForRollback = metadata

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

            originalForRollback = originalContent
            if (request.expectedOriginalContent != null && request.expectedOriginalContent != originalContent) {
                emit(PmpEvent.Failed("Dosya düzenleme sırasında değişti. Dosyayı yeniden çekin."))
                return@flow
            }
            emit(PmpEvent.Modifying)
            val updatedContent: String = request.replacementContent ?: when (request.patchMode) {
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

            if (request.options.createRemoteBackup) {
                val path = createRemoteBackupPath(request.remotePath)
                val backedUp = pmpEngine.supportsRemoteCopy && pmpEngine.copyRemote(request.remotePath, path)
                if (backedUp) remoteBackupPath = path
                else if (request.options.abortIfBackupFails) {
                    emit(PmpEvent.Failed("Yedek oluşturulamadığı için dosya değiştirilmedi."))
                    return@flow
                } else warnings.add("Uzak yedek oluşturulamadı.")
            }
            if (request.options.createLocalBackup) {
                withContext(Dispatchers.IO) {
                    val backupDir = File(context.filesDir, "omnigfx/backups").apply { mkdirs() }
                    pulledFile.copyTo(File(backupDir, "${stamp}_$safeName"))
                }
            }
            emit(PmpEvent.Encoding)
            withContext(Dispatchers.IO) {
                modifiedFile.writeText(updatedContent, Charsets.UTF_8)
            }

            // ═══ AŞAMA 3: PUSH + VERIFY ═══
            emit(PmpEvent.Pushing)
            writeAttempted = true
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
                val restored = rollback(request, pulledFile, originalContent, metadata)
                writeAttempted = false
                emit(PmpEvent.Failed("Dosya yazılamadı. $restored"))
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
                if (verifyResult?.success != true) {
                    warnings.add("Push doğrulanamadı: ${verifyResult?.message}")
                    val restored = rollback(request, pulledFile, originalContent, metadata)
                    writeAttempted = false
                    emit(PmpEvent.Failed(
                        "Push doğrulanamadı. $restored",
                        RuntimeException("Verification failed")
                    ))
                    return@flow
                }
            }

            writeAttempted = false
            emit(PmpEvent.Completed(
                PmpResult.Success(
                    originalContent = originalContent,
                    updatedContent = updatedContent,
                    remoteBackupPath = remoteBackupPath,
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
            withContext(NonCancellable + Dispatchers.IO) {
                if (writeAttempted && originalForRollback != null) {
                    rollback(request, pulledFile, originalForRollback, metadataForRollback)
                }
                pulledFile.delete()
                modifiedFile.delete()
            }
        }
    }

    private suspend fun rollback(
        request: PmpRequest,
        originalFile: File,
        originalContent: String,
        metadata: FileMetadata?
    ): String = withContext(NonCancellable + Dispatchers.IO) {
        try {
            val restored = if (pmpEngine.supportsAtomicPush) {
                pmpEngine.pushAtomic(originalFile, request.remotePath, metadata, request.options.restoreSelinux)
            } else pmpEngine.push(originalFile, request.remotePath)
            if (restored && verifier.verifyContent(originalContent, request.remotePath, metadata).success) {
                "Önceki içerik geri yüklendi ve doğrulandı."
            } else "Geri yükleme doğrulanamadı; yedekler ekranını kullanın."
        } catch (e: Exception) {
            "Geri yükleme tamamlanamadı: ${e.message}"
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
                error("Regex patch eksik: ${patch.path}")
            }
            try {
                val userValue = when (val v = patch.value) {
                    is Boolean -> if (v) "1" else "0"
                    else -> v.toString()
                }
                val replacement = template.replace("{{value}}", userValue)
                val regex = Regex(pattern)
                require(regex.containsMatchIn(modified)) { "Eşleşme bulunamadı: ${patch.path}" }
                // Preserve capture references in the template, but treat the user's value literally.
                modified = modified.replace(regex, template.replace("{{value}}", Regex.escapeReplacement(userValue)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw IllegalArgumentException("Regex patch başarısız: ${patch.path} | ${e.message}", e)
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
