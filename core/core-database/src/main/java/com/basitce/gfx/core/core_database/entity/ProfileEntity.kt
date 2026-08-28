package com.basitce.gfx.core.core_database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val schemaId: String,
    val name: String,
    val userValuesJson: String,
    val isManual: Boolean = false,
    val rawContent: String? = null,
    val targetFilePath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
