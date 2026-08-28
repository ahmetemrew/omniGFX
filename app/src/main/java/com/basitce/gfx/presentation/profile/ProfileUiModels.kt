package com.basitce.gfx.presentation.profile

import com.basitce.gfx.core.core_database.model.ProfileWithSchema
import com.basitce.gfx.core.core_engine.profile.ConfigFormatHint
import com.basitce.gfx.core.core_engine.profile.PatchValueType
import com.basitce.gfx.core.core_engine.profile.ProfileOptions
import java.util.UUID

/**
 * Profile listesi ekran durumu.
 */
data class ProfileListUiState(
    val isLoading: Boolean = false,
    val profiles: List<ProfileWithSchema> = emptyList(),
    val error: String? = null
)

/**
 * Editörde düzenlenebilir patch modeli.
 */
data class ProfilePatchUi(
    val id: String = UUID.randomUUID().toString(),
    val path: String = "",
    val value: String = "",
    val valueType: PatchValueType = PatchValueType.STRING
)

/**
 * Path test sonucu için UI modeli.
 */
data class PathProbeUi(
    val path: String,
    val exists: Boolean,
    val readable: Boolean,
    val writable: Boolean,
    val parentWritable: Boolean,
    val canStat: Boolean,
    val messages: List<String>
)

/**
 * Path test ekran durumu.
 */
data class PathTestUiState(
    val isTesting: Boolean = false,
    val error: String? = null,
    val capabilityMessage: String? = null,
    val securityWarnings: List<String> = emptyList(),
    val securityErrors: List<String> = emptyList(),
    val candidates: List<PathProbeUi> = emptyList()
)

/**
 * Profile editor ekran durumu.
 */
data class ProfileEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,

    val profileId: String? = null,
    val name: String = "",
    val packageName: String = "",
    val targetPathTemplate: String = "",
    val format: ConfigFormatHint = ConfigFormatHint.AUTO,

    val patches: List<ProfilePatchUi> = emptyList(),
    val options: ProfileOptions = ProfileOptions()
)

