package com.basitce.gfx.core.core_database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 4 → 5:
 * - profiles tablosuna targetFilePath kolonu eklendi.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `profiles` ADD COLUMN `targetFilePath` TEXT NOT NULL DEFAULT ''"
        )
    }
}
