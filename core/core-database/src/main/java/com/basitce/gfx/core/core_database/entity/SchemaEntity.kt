package com.basitce.gfx.core.core_database.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schemas")
data class SchemaEntity(
    @PrimaryKey val id: String,
    val gameId: String,
    val version: Int,
    val jsonSchema: String
)
