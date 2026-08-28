package com.basitce.gfx.core.core_database.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val name: String,
    val iconUri: String,
    val isCustom: Boolean,
    val createdAt: Long
)
