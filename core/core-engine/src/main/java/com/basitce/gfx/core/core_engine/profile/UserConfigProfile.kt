package com.basitce.gfx.core.core_engine.profile

import java.util.UUID

enum class ConfigFormatHint {
    AUTO,
    JSON,
    XML,
    INI
}

enum class PatchValueType {
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    RAW
}

data class ProfilePatch(
    val path: String,
    val value: String? = null,
    val valueType: PatchValueType = PatchValueType.STRING
)

data class ProfileOptions(
    val dryRun: Boolean = false,

    val backupLocal: Boolean = true,
    val backupRemote: Boolean = true,

    val atomicReplace: Boolean = true,
    val restoreSelinux: Boolean = false,
    val abortIfBackupFails: Boolean = true,

    val forceStopBeforeApply: Boolean = false,
    val launchAfterApply: Boolean = false,

    val expertMode: Boolean = true,
    val allowUnsafeSystemPaths: Boolean = false,

    // Yeni verification seçenekleri
    val verifyAfterApply: Boolean = true,
    val requireHashVerification: Boolean = true,
    val requireSizeVerification: Boolean = false,
    val verifyMetadata: Boolean = false,
    val verifySelinux: Boolean = false,

    // Yeni rollback seçenekleri
    val autoRollbackOnVerificationFailure: Boolean = true,
    val autoRollbackOnApplyFailure: Boolean = false,

    // Backup retention
    // null -> temizlik yok
    // 3 -> son 3 backup kalır
    val backupRetentionCount: Int? = 3
)

/**
 * Kullanıcı tanımlı evrensel config profili.
 *
 * Oyun sınırı yoktur.
 * Kullanıcı paket adı, hedef path ve patch'leri kendisi tanımlar.
 */
data class UserConfigProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String? = null,
    val targetPathTemplate: String,
    val format: ConfigFormatHint = ConfigFormatHint.AUTO,
    val patches: List<ProfilePatch>,
    val options: ProfileOptions = ProfileOptions()
)
