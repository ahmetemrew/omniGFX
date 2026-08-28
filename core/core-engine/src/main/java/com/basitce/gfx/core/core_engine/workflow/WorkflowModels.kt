package com.basitce.gfx.core.core_engine.workflow

import com.basitce.gfx.core.core_engine.FileMetadata

/**
 * Workflow adım durumu.
 * Kullanıcı her adımı manuel tetikler.
 */
enum class WorkflowStep {
    IDLE,
    FILE_SELECTED,
    PULLING,
    PULLED,
    ANALYZING,
    ANALYZED,
    EDITING,
    PUSHING,
    PUSHED,
    ERROR
}

/**
 * Algılanan dosya formatı.
 */
enum class DetectedFormat(val label: String) {
    INI("INI / Properties"),
    JSON("JSON"),
    XML("XML"),
    PLAIN_TEXT("Düz Metin"),
    UNKNOWN("Bilinmiyor")
}

/**
 * Çekilen dosyanın local draft kaydı.
 * Uygulama kapatılsa bile kalır.
 */
data class WorkflowDraft(
    val id: String,
    val remotePath: String,
    val fileName: String,
    val localFileName: String,
    val detectedFormat: DetectedFormat,
    val originalContent: String,
    val modifiedContent: String?,
    val metadata: FileMetadata?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val hasChanges: Boolean
        get() = modifiedContent != null && modifiedContent != originalContent
}

/**
 * Workflow UI durumu.
 */
data class WorkflowUiState(
    val step: WorkflowStep = WorkflowStep.IDLE,
    val remotePath: String = "",
    val fileName: String = "",
    val detectedFormat: DetectedFormat = DetectedFormat.UNKNOWN,
    val originalContent: String = "",
    val editedContent: String = "",
    val hasChanges: Boolean = false,
    val metadata: FileMetadata? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val draftId: String? = null
)
