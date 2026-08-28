package com.basitce.gfx.core.sync

data class ProfileCloudDto(
    val id: String,
    val name: String,
    val version: Long,
    val updatedAt: Long,
    val payloadJson: String
)
