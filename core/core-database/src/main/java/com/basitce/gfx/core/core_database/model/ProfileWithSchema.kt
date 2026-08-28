package com.basitce.gfx.core.core_database.model

import androidx.room.Embedded
import com.basitce.gfx.core.core_database.entity.ProfileEntity

data class ProfileWithSchema(
    @Embedded val profile: ProfileEntity,
    val gameId: String,
    val gameName: String
)
