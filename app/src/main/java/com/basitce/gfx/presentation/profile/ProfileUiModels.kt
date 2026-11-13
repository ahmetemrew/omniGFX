package com.basitce.gfx.presentation.profile

import com.basitce.gfx.core.core_database.model.ProfileWithSchema

/**
 * Profile listesi ekran durumu.
 */
data class ProfileListUiState(
    val isLoading: Boolean = false,
    val profiles: List<ProfileWithSchema> = emptyList(),
    val error: String? = null
)
