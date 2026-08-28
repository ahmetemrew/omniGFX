package com.basitce.gfx.core.core_engine.pipeline

/**
 * Patch uygulama modu.
 */
enum class PmpPatchMode {
    /** ConfigParser tabanlı: JSON, XML, INI path navigasyonu */
    CONFIG_PARSER,
    /** Regex tabanlı: pattern/replacement ile metin değiştirme */
    REGEX
}

/**
 * Uygulanacak tek bir config değişikliği.
 */
data class PmpPatch(
    val path: String,
    val value: Any,
    /** REGEX modunda: aranacak pattern */
    val regexPattern: String? = null,
    /** REGEX modunda: replacement şablonu ({{value}} placeholder içerir) */
    val regexReplacementTemplate: String? = null
)

/**
 * Pipeline'a verilen istek.
 */
data class PmpRequest(
    val remotePath: String,
    val patches: List<PmpPatch>,
    val patchMode: PmpPatchMode = PmpPatchMode.CONFIG_PARSER,
    val options: PmpPipelineOptions = PmpPipelineOptions()
)

/**
 * Pipeline davranış seçenekleri.
 */
data class PmpPipelineOptions(
    val createRemoteBackup: Boolean = true,
    val createLocalBackup: Boolean = true,
    val atomicReplace: Boolean = true,
    val restoreSelinux: Boolean = true,
    val abortIfBackupFails: Boolean = true,
    val verifyAfterPush: Boolean = true,
    val requireHashVerification: Boolean = true
)

/**
 * Pipeline adım event'leri.
 */
sealed class PmpEvent {
    data object Started : PmpEvent()
    data object Pulling : PmpEvent()
    data object Decoding : PmpEvent()
    data object Modifying : PmpEvent()
    data object Encoding : PmpEvent()
    data object Pushing : PmpEvent()
    data object Verifying : PmpEvent()
    data class Completed(val result: PmpResult) : PmpEvent()
    data class Failed(
        val message: String,
        val cause: Throwable? = null
    ) : PmpEvent()
}

/**
 * Pipeline sonuç modeli.
 */
sealed class PmpResult {
    data object NoChange : PmpResult()
    data class Success(
        val originalContent: String,
        val updatedContent: String,
        val remoteBackupPath: String?,
        val warnings: List<String>
    ) : PmpResult()
}
