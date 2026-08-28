package com.basitce.gfx.core.core_database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 3 → 4:
 * - Manuel (Ham) profil desteği için profiles tablosuna isManual ve rawContent kolonları eklendi.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `isManual` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `rawContent` TEXT")
    }
}
