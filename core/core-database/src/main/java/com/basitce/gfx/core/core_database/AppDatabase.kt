package com.basitce.gfx.core.core_database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.basitce.gfx.core.core_database.dao.GameDao
import com.basitce.gfx.core.core_database.dao.ProfileDao
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.entity.GameEntity
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_database.entity.SchemaEntity

@Database(
    entities = [
        GameEntity::class,
        SchemaEntity::class,
        ProfileEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun schemaDao(): SchemaDao
    abstract fun profileDao(): ProfileDao
}
