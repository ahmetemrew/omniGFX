package com.basitce.gfx.core.core_engine.profile

import android.content.Context
import com.basitce.gfx.core.core_engine.ConfigParserFactory
import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.rollback.ConfigRollbackManager
import com.basitce.gfx.core.core_engine.rollback.RollbackResult
import com.basitce.gfx.core.core_engine.shizuku.GameProcessManager
import com.basitce.gfx.core.core_engine.shizuku.ShizukuCapabilityChecker
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPathProbe
import com.basitce.gfx.core.core_engine.shizuku.ShizukuPrivilegeLevel
import com.basitce.gfx.core.core_engine.verification.PmpVerifier
import com.basitce.gfx.core.core_engine.verification.VerifyOptions
import com.basitce.gfx.core.core_engine.verification.VerifyResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class ProfileApplyResult {

    data class Success(
        val selectedPath: String,
        val changed: Boolean,
        val dryRun: Boolean,
        val originalContent: String?,
        val updatedContent: String?,
        val warnings: List<String>,
        val remoteBackupPath: String?,
        val verification: VerifyResult? = null
    ) : ProfileApplyResult()

    data class Failure(
        val message: String,
        val selectedPath: String? = null,
        val warnings: List<String> = emptyList(),
        val cause: Throwable? = null,
        val verification: VerifyResult? = null,
        val rolledBack: Boolean = false
    ) : ProfileApplyResult()
}

/**
 * Event-driven Profile Engine.
 *
 * Bu sınıf PMP akışını adım adım yönetir ve her adımı event olarak yayınlar.
 */
@Singleton
class ProfileEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pmpEngine: PmpEngine,
    private val parserFactory: ConfigParserFactory,
    private val capabilityChecker: ShizukuCapabilityChecker,
    private val pathProbe: ShizukuPathProbe,
    private val pathResolver: ProfilePathResolver,
    private val securityScanner: ProfileSecurityScanner,
    private val gameProcessManager: GameProcessManager,
    private val pmpVerifier: PmpVerifier,
    private val rollbackManager: ConfigRollbackManager
) {

    /**
     * Event tabanlı ana akış.
     */
    fun applyProfileWithEvents(
        profile: UserConfigProfile
    ): Flow<ProfileEngineEvent> = flow {
        emit(ProfileEngineEvent.Started)

        val warnings = mutableListOf<String>()
        val stamp = System.currentTimeMillis()

        try {
            emit(ProfileEngineEvent.Validating)

            val validationErrors = validateProfile(profile)

            if (validationErrors.isNotEmpty()) {
                failure(
                    message = "Profil doğrulanamadı.",
                    warnings = warnings + validationErrors
                )
                return@flow
            }

            emit(ProfileEngineEvent.ResolvingPaths)

            val candidatePaths = pathResolver.resolveCandidates(profile)

            if (candidatePaths.isEmpty()) {
                failure(
                    message = "Aday path üretilemedi.",
                    warnings = warnings
                )
                return@flow
            }

            emit(ProfileEngineEvent.SecurityCheck)

            val securityReport = securityScanner.scan(
                profile = profile,
                candidatePaths = candidatePaths
            )

            warnings.addAll(securityReport.warnings)

            if (!securityReport.allowed) {
                failure(
                    message = "Profil güvenlik kontrolünden geçemedi.",
                    warnings = warnings + securityReport.errors
                )
                return@flow
            }

            emit(ProfileEngineEvent.CapabilityCheck)

            val capability = capabilityChecker.check()

            if (!capability.available) {
                failure(
                    message = "Shizuku kullanılabilir değil.",
                    warnings = warnings
                )
                return@flow
            }

            when (capability.privilegeLevel) {
                ShizukuPrivilegeLevel.ROOT -> {
                    emit(ProfileEngineEvent.Log("Shizuku root seviyesinde."))
                }

                ShizukuPrivilegeLevel.ADB_SHELL -> {
                    warnings.add(
                        "Shizuku adb shell seviyesinde. Bazı private dosyalar erişilemeyebilir."
                    )
                }

                else -> {
                    warnings.add("Shizuku yetki seviyesi bilinmiyor.")
                }
            }

            emit(ProfileEngineEvent.PathProbing)

            val probeResults = candidatePaths.map { candidate ->
                pathProbe.probe(candidate)
            }

            val selectedProbe = if (profile.options.dryRun) {
                probeResults.firstOrNull { it.exists && it.readable }
            } else {
                probeResults.firstOrNull { it.exists && it.readable && it.writable }
            }

            if (selectedProbe == null) {
                val probeMessages = probeResults.flatMap { it.messages }

                val readableButNotWritable = probeResults.firstOrNull {
                    it.exists && it.readable && !it.writable
                }

                if (!profile.options.dryRun && readableButNotWritable != null) {
                    failure(
                        message = "Hedef dosya okunabiliyor ama yazılamıyor.",
                        selectedPath = readableButNotWritable.path,
                        warnings = warnings + probeMessages
                    )
                } else {
                    failure(
                        message = "Hedef dosya bulunamadı veya erişilemiyor.",
                        selectedPath = candidatePaths.firstOrNull(),
                        warnings = warnings + probeMessages
                    )
                }

                return@flow
            }

            val selectedPath = selectedProbe.path

            if (!profile.options.dryRun &&
                profile.options.forceStopBeforeApply &&
                profile.packageName != null &&
                isValidPackageName(profile.packageName)
            ) {
                emit(ProfileEngineEvent.ForceStopping)

                val forceStopped = gameProcessManager.forceStop(profile.packageName)

                if (!forceStopped) {
                    warnings.add("Oyun force-stop edilemedi: ${profile.packageName}")
                }
            }

            if (profile.options.dryRun) {
                val dryRunFile = File(
                    context.cacheDir,
                    "omnigfx_dryrun_$stamp"
                )

                try {
                    emit(ProfileEngineEvent.PullingFile)

                    val pulled = pmpEngine.pull(
                        remotePath = selectedPath,
                        destination = dryRunFile
                    )

                    if (!pulled || !dryRunFile.exists()) {
                        failure(
                            message = "Dry-run için dosya çekilemedi.",
                            selectedPath = selectedPath,
                            warnings = warnings
                        )
                        return@flow
                    }

                    emit(ProfileEngineEvent.ParsingConfig)

                    val originalContent = withContext(Dispatchers.IO) {
                        dryRunFile.readText(Charsets.UTF_8)
                    }

                    val parser = parserFactory.create(selectedPath, originalContent)
                    parser.parse(originalContent)

                    emit(ProfileEngineEvent.ApplyingPatches)

                    val patchErrors = mutableListOf<String>()

                    profile.patches.forEach { patch ->
                        try {
                            parser.updateValue(
                                path = patch.path,
                                value = toConfigValue(patch)
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            patchErrors.add("Patch başarısız: ${patch.path} | ${e.message}")
                        }
                    }

                    if (patchErrors.isNotEmpty()) {
                        failure(
                            message = "Dry-run sırasında patch hataları oluştu.",
                            selectedPath = selectedPath,
                            warnings = warnings + patchErrors
                        )
                        return@flow
                    }

                    emit(ProfileEngineEvent.SerializingConfig)

                    val updatedContent = parser.serialize()

                    emit(
                        ProfileEngineEvent.Completed(
                            ProfileApplyResult.Success(
                                selectedPath = selectedPath,
                                changed = updatedContent != originalContent,
                                dryRun = true,
                                originalContent = originalContent,
                                updatedContent = updatedContent,
                                warnings = warnings,
                                remoteBackupPath = null,
                                verification = null
                            )
                        )
                    )
                } finally {
                    dryRunFile.delete()
                }

                return@flow
            }

            emit(ProfileEngineEvent.Preparing)

            val originalMetadata = safeStat(selectedPath)

            var remoteBackupPath: String? = null

            if (profile.options.backupRemote) {
                emit(ProfileEngineEvent.CreatingRemoteBackup)

                if (pmpEngine.supportsRemoteCopy) {
                    val candidateBackupPath = createRemoteBackupPath(selectedPath)

                    val backupSucceeded = try {
                        pmpEngine.copyRemote(
                            sourceRemotePath = selectedPath,
                            destinationRemotePath = candidateBackupPath
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        warnings.add("Remote backup sırasında exception: ${e.message}")
                        false
                    }

                    if (backupSucceeded) {
                        remoteBackupPath = candidateBackupPath
                    } else {
                        warnings.add("Remote backup oluşturulamadı: $candidateBackupPath")

                        if (profile.options.abortIfBackupFails) {
                            failure(
                                message = "Remote backup oluşturulamadığı için işlem iptal edildi.",
                                selectedPath = selectedPath,
                                warnings = warnings
                            )
                            return@flow
                        }
                    }
                } else {
                    warnings.add("PmpEngine remote copy desteklemiyor.")

                    if (profile.options.abortIfBackupFails) {
                        failure(
                            message = "Remote backup desteklenmiyor ve abortIfBackupFails aktif.",
                            selectedPath = selectedPath,
                            warnings = warnings
                        )
                        return@flow
                    }
                }
            }

            val pulledFile = File(
                context.cacheDir,
                "omnigfx_pull_$stamp"
            )

            emit(ProfileEngineEvent.PullingFile)

            val pullSucceeded = try {
                pmpEngine.pull(
                    remotePath = selectedPath,
                    destination = pulledFile
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                warnings.add("Pull sırasında exception: ${e.message}")
                false
            }

            if (!pullSucceeded || !pulledFile.exists()) {
                failure(
                    message = "Dosya çekilemedi: $selectedPath",
                    selectedPath = selectedPath,
                    warnings = warnings
                )
                return@flow
            }

            if (profile.options.backupLocal) {
                emit(ProfileEngineEvent.CreatingLocalBackup)

                val localBackupFile = File(
                    context.cacheDir,
                    "omnigfx_local_backup_$stamp"
                )

                try {
                    pulledFile.copyTo(localBackupFile, overwrite = true)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    warnings.add("Local backup oluşturulamadı: ${e.message}")
                }
            }

            emit(ProfileEngineEvent.ParsingConfig)

            val originalContent = withContext(Dispatchers.IO) {
                pulledFile.readText(Charsets.UTF_8)
            }

            val parser = parserFactory.create(selectedPath, originalContent)

            parser.parse(originalContent)

            emit(ProfileEngineEvent.ApplyingPatches)

            val patchErrors = mutableListOf<String>()

            profile.patches.forEach { patch ->
                try {
                    parser.updateValue(
                        path = patch.path,
                        value = toConfigValue(patch)
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    patchErrors.add("Patch başarısız: ${patch.path} | ${e.message}")
                }
            }

            if (patchErrors.isNotEmpty()) {
                failure(
                    message = "Bir veya birden fazla patch uygulanamadı.",
                    selectedPath = selectedPath,
                    warnings = warnings + patchErrors
                )
                return@flow
            }

            emit(ProfileEngineEvent.SerializingConfig)

            val updatedContent = parser.serialize()

            val changed = updatedContent != originalContent

            if (!changed) {
                pulledFile.delete()

                emit(
                    ProfileEngineEvent.Completed(
                        ProfileApplyResult.Success(
                            selectedPath = selectedPath,
                            changed = false,
                            dryRun = false,
                            originalContent = originalContent,
                            updatedContent = updatedContent,
                            warnings = warnings + "Dosyada değişiklik oluşmadı.",
                            remoteBackupPath = remoteBackupPath,
                            verification = null
                        )
                    )
                )

                return@flow
            }

            val modifiedFile = File(
                context.cacheDir,
                "omnigfx_modified_$stamp"
            )

            withContext(Dispatchers.IO) {
                modifiedFile.writeText(updatedContent, Charsets.UTF_8)
            }

            emit(ProfileEngineEvent.PushingFile)

            val useAtomicPush = profile.options.atomicReplace &&
                pmpEngine.supportsAtomicPush

            val pushSucceeded = try {
                if (useAtomicPush) {
                    pmpEngine.pushAtomic(
                        source = modifiedFile,
                        remotePath = selectedPath,
                        metadata = originalMetadata,
                        restoreSelinux = profile.options.restoreSelinux
                    )
                } else {
                    pmpEngine.push(
                        source = modifiedFile,
                        remotePath = selectedPath
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                warnings.add("Push sırasında exception: ${e.message}")
                false
            }

            if (!pushSucceeded) {
                failure(
                    message = "Dosya hedef konuma push edilemedi: $selectedPath",
                    selectedPath = selectedPath,
                    warnings = warnings
                )
                return@flow
            }

            emit(ProfileEngineEvent.RestoringMetadata)

            restoreMetadata(
                path = selectedPath,
                metadata = originalMetadata,
                warnings = warnings,
                restoreSelinux = profile.options.restoreSelinux
            )

            var verificationResult: VerifyResult? = null

            if (profile.options.verifyAfterApply) {
                emit(ProfileEngineEvent.Verifying)

                val verifyOptions = VerifyOptions(
                    requireHash = profile.options.requireHashVerification,
                    requireSize = profile.options.requireSizeVerification,
                    requireMetadata = profile.options.verifyMetadata,
                    requireSelinux = profile.options.verifySelinux
                )

                verificationResult = try {
                    pmpVerifier.verifyContent(
                        expectedContent = updatedContent,
                        remotePath = selectedPath,
                        expectedMetadata = originalMetadata,
                        options = verifyOptions
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    VerifyResult(
                        success = false,
                        message = "Verify sırasında exception: ${e.message}",
                        hashMatch = null,
                        sizeMatch = null,
                        metadataMatch = null,
                        warnings = emptyList()
                    )
                }

                warnings.addAll(verificationResult.warnings)

                if (!verificationResult.success) {
                    if (!profile.options.autoRollbackOnVerificationFailure) {
                        failure(
                            message = "Push doğrulanamadı.",
                            selectedPath = selectedPath,
                            warnings = warnings,
                            verification = verificationResult,
                            rolledBack = false
                        )
                        return@flow
                    }

                    emit(ProfileEngineEvent.RollingBack)

                    val rollbackResult = safeRestoreLatest(selectedPath)

                    val rollbackSucceeded = rollbackResult is RollbackResult.Success

                    warnings.addAll(rollbackResultMessages(rollbackResult))

                    if (rollbackSucceeded) {
                        val rollbackVerifyOptions = verifyOptions.copy(
                            requireMetadata = false,
                            requireSelinux = false,
                            requireSize = false
                        )

                        val rollbackVerifyResult = try {
                            pmpVerifier.verifyContent(
                                expectedContent = originalContent,
                                remotePath = selectedPath,
                                expectedMetadata = originalMetadata,
                                options = rollbackVerifyOptions
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            VerifyResult(
                                success = false,
                                message = "Rollback verify sırasında exception: ${e.message}",
                                hashMatch = null,
                                sizeMatch = null,
                                metadataMatch = null,
                                warnings = emptyList()
                            )
                        }

                        warnings.addAll(rollbackVerifyResult.warnings)

                        if (!rollbackVerifyResult.success) {
                            warnings.add(
                                "Rollback yapıldı ama rollback sonrası içerik doğrulanamadı."
                            )
                        } else {
                            warnings.add("Rollback sonrası içerik doğrulandı.")
                        }
                    }

                    failure(
                        message = "Push doğrulanamadı ve otomatik rollback tetiklendi.",
                        selectedPath = selectedPath,
                        warnings = warnings,
                        verification = verificationResult,
                        rolledBack = rollbackSucceeded
                    )

                    return@flow
                }
            }

            profile.options.backupRetentionCount?.let { keep ->
                if (keep > 0) {
                    emit(ProfileEngineEvent.CleaningBackups)

                    val cleanResult = safeCleanOldBackups(
                        targetPath = selectedPath,
                        keep = keep
                    )

                    warnings.addAll(rollbackResultMessages(cleanResult))
                }
            }

            if (profile.options.launchAfterApply &&
                profile.packageName != null &&
                isValidPackageName(profile.packageName)
            ) {
                emit(ProfileEngineEvent.LaunchingGame)

                val launched = gameProcessManager.launch(profile.packageName)

                if (!launched) {
                    warnings.add("Oyun başlatılamadı: ${profile.packageName}")
                }
            }

            pulledFile.delete()
            modifiedFile.delete()

            emit(
                ProfileEngineEvent.Completed(
                    ProfileApplyResult.Success(
                        selectedPath = selectedPath,
                        changed = true,
                        dryRun = false,
                        originalContent = originalContent,
                        updatedContent = updatedContent,
                        warnings = warnings,
                        remoteBackupPath = remoteBackupPath,
                        verification = verificationResult
                    )
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failure(
                message = e.message ?: "ProfileEngine bilinmeyen hata.",
                warnings = warnings,
                cause = e
            )
        }
    }

    /**
     * Event toplamak istemeyen eski tarz kullanım için uyumluluk fonksiyonu.
     */
    suspend fun applyProfile(
        profile: UserConfigProfile
    ): ProfileApplyResult {
        return applyProfileWithEvents(profile)
            .filterIsInstance<ProfileEngineEvent.Completed>()
            .first()
            .result
    }

    private suspend fun restoreMetadata(
        path: String,
        metadata: FileMetadata?,
        warnings: MutableList<String>,
        restoreSelinux: Boolean
    ) {
        if (metadata == null) return

        try {
            val uid = metadata.uid
            val gid = metadata.gid

            if (uid != null && gid != null) {
                if (!pmpEngine.chown(path, uid, gid)) {
                    warnings.add("chown geri yüklenemedi: uid=$uid gid=$gid path=$path")
                }
            }

            metadata.mode?.let { mode ->
                if (!pmpEngine.chmod(path, mode)) {
                    warnings.add("chmod geri yüklenemedi: mode=$mode path=$path")
                }
            }

            if (restoreSelinux && pmpEngine.supportsSelinux) {
                metadata.seContext?.let { seContext ->
                    if (!pmpEngine.chcon(path, seContext)) {
                        warnings.add("chcon geri yüklenemedi: context=$seContext path=$path")
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            warnings.add("Metadata restore sırasında exception: ${e.message}")
        }
    }

    private suspend fun safeStat(path: String): FileMetadata? {
        return try {
            pmpEngine.stat(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun safeRestoreLatest(path: String): RollbackResult {
        return try {
            rollbackManager.restoreLatest(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RollbackResult.Failure(
                message = "Rollback sırasında exception: ${e.message}",
                warnings = emptyList(),
                cause = e
            )
        }
    }

    private suspend fun safeCleanOldBackups(
        targetPath: String,
        keep: Int
    ): RollbackResult {
        return try {
            rollbackManager.cleanOldBackups(
                targetPath = targetPath,
                keep = keep
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RollbackResult.Failure(
                message = "Backup cleanup sırasında exception: ${e.message}",
                warnings = emptyList(),
                cause = e
            )
        }
    }

    private fun rollbackResultMessages(result: RollbackResult): List<String> {
        return when (result) {
            is RollbackResult.Success -> listOf(result.message) + result.warnings
            is RollbackResult.Failure -> listOf(result.message) + result.warnings
        }
    }

    private fun validateProfile(profile: UserConfigProfile): List<String> {
        val errors = mutableListOf<String>()

        if (profile.name.isBlank()) {
            errors.add("Profil adı boş olamaz.")
        }

        if (profile.targetPathTemplate.isBlank()) {
            errors.add("Target path template boş olamaz.")
        }

        if (profile.packageName != null && !isValidPackageName(profile.packageName)) {
            errors.add("Geçersiz package name: ${profile.packageName}")
        }

        if (profile.patches.isEmpty()) {
            errors.add("Profilde en az bir patch olmalıdır.")
        }

        profile.patches.forEachIndexed { index, patch ->
            if (patch.path.isBlank()) {
                errors.add("Patch[$index] path boş olamaz.")
            }
        }

        return errors
    }

    private fun toConfigValue(patch: ProfilePatch): Any {
        val rawValue = patch.value.orEmpty()

        return when (patch.valueType) {
            PatchValueType.STRING -> rawValue

            PatchValueType.NUMBER -> {
                rawValue.toLongOrNull()
                    ?: rawValue.toDoubleOrNull()
                    ?: rawValue
            }

            PatchValueType.BOOLEAN -> {
                rawValue.equals("true", ignoreCase = true)
            }

            PatchValueType.NULL -> "null"

            PatchValueType.RAW -> rawValue
        }
    }

    private fun createRemoteBackupPath(remotePath: String): String {
        val parent = remotePath.substringBeforeLast('/', "")

        val fileName = remotePath
            .substringAfterLast('/')
            .ifBlank { "config" }

        val safeFileName = safeFileName(fileName)

        val backupName = ".$safeFileName.omnigfx.${System.currentTimeMillis()}.bak"

        return if (parent.isEmpty()) {
            "/$backupName"
        } else {
            "$parent/$backupName"
        }
    }

    private fun safeFileName(fileName: String): String {
        return fileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(64)
            .ifBlank { "config" }
    }

    private fun isValidPackageName(packageName: String): Boolean {
        return packageName.matches(PACKAGE_NAME_REGEX)
    }

    companion object {
        private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*$")
    }
}

private suspend fun FlowCollector<ProfileEngineEvent>.failure(
    message: String,
    selectedPath: String? = null,
    warnings: List<String> = emptyList(),
    cause: Throwable? = null,
    verification: VerifyResult? = null,
    rolledBack: Boolean = false
) {
    emit(
        ProfileEngineEvent.Completed(
            ProfileApplyResult.Failure(
                message = message,
                selectedPath = selectedPath,
                warnings = warnings,
                cause = cause,
                verification = verification,
                rolledBack = rolledBack
            )
        )
    )
}
