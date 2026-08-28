package com.basitce.gfx.feature.feature_wizard.viewmodel

import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_engine.workflow.DetectedFormat
import java.util.UUID

enum class SetupWizardStep(val order: Int, val title: String) {
    SELECT_FILE(1, "Hedef Dosya"),
    PULL_ANALYZE(2, "Çek & Analiz"),
    EDIT(3, "Düzenle"),
    PIN_VARIABLES(4, "Değişkenler & Profil"),
    PUSH_APPLY(5, "Uygula"),
    DONE(6, "Tamamlandı");

    val isFirst: Boolean get() = this == SELECT_FILE
    val isLast: Boolean get() = this == DONE

    fun next(): SetupWizardStep {
        val values = entries
        val currentIndex = values.indexOf(this)
        return if (currentIndex < values.lastIndex) values[currentIndex + 1] else DONE
    }

    fun previous(): SetupWizardStep {
        val values = entries
        val currentIndex = values.indexOf(this)
        return if (currentIndex > 0) values[currentIndex - 1] else SELECT_FILE
    }
}

enum class MoveDirection {
    UP, DOWN
}

data class ConfigNode(
    val id: String = UUID.randomUUID().toString(),
    val lineNumber: Int = 1,
    val type: NodeType = NodeType.KEY_VALUE,
    val path: String = "",
    val key: String = "",
    val value: String = "",
    val rawLine: String = "",
    val isPinned: Boolean = false,
    val pinnedVariableId: String? = null,
    val isModified: Boolean = false,
    val section: String = "",
    val children: List<ConfigNode> = emptyList()
)

enum class NodeType {
    KEY_VALUE,
    SECTION_HEADER,
    COMMENT,
    BLANK,
    UNKNOWN
}

data class PinnedVariable(
    val id: String = UUID.randomUUID().toString(),
    val path: String,
    val uiComponentType: UiComponentType = UiComponentType.SLIDER,
    val label: String,
    val description: String = "",
    val min: Float = 0f,
    val max: Float = 100f,
    val step: Float = 1f,
    val options: List<String> = emptyList(),
    val valueMapping: Map<String, String> = emptyMap(),
    val defaultValue: String? = null,
    val applyCondition: String? = null
)

enum class UiComponentType {
    SLIDER,
    DROPDOWN,
    TOGGLE,
    TEXT_INPUT
}

enum class ProfileType {
    DYNAMIC,
    MANUAL
}

data class PushProgressStep(
    val label: String,
    val status: PushStepStatus = PushStepStatus.PENDING
)

enum class PushStepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class SetupWizardState(
    val currentStep: SetupWizardStep = SetupWizardStep.SELECT_FILE,
    val packageName: String = "",
    val gameName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,

    val targetFilePath: String = "",
    val isBrowsing: Boolean = false,
    val browserError: String? = null,

    val isPulling: Boolean = false,
    val fileContent: String = "",
    val detectedFormat: DetectedFormat = DetectedFormat.UNKNOWN,
    val fileMetadata: FileMetadata? = null,
    val fileSizeBytes: Long = 0L,
    val pullProgressMessage: String = "",
    val pathProbeMessages: List<String> = emptyList(),
    val isFileTooLarge: Boolean = false,
    val draftId: String? = null,

    val configNodes: List<ConfigNode> = emptyList(),
    val hasUnsavedChanges: Boolean = false,

    val pinnedVariables: Map<String, PinnedVariable> = emptyMap(),
    val profileName: String = "",
    val profileType: ProfileType = ProfileType.DYNAMIC,

    val isPushing: Boolean = false,
    val pushSuccess: Boolean? = null,
    val pushMessage: String? = null,
    val pushProgressSteps: List<PushProgressStep> = emptyList(),
    val currentPushStep: Int = 0,
    val verificationPassed: Boolean? = null,
    val rollbackInfo: String? = null,
    val showGameRunningWarning: Boolean = false,

    val isGameRunning: Boolean = false
) {
    val canProceedToFileSelect: Boolean
        get() = targetFilePath.isNotBlank()

    val canProceedToEdit: Boolean
        get() = fileContent.isNotEmpty() && detectedFormat != DetectedFormat.UNKNOWN

    val canPush: Boolean
        get() = targetFilePath.isNotBlank() && fileContent.isNotEmpty()

    val hasAnyChanges: Boolean
        get() = configNodes.any { it.isModified } || hasUnsavedChanges
}
