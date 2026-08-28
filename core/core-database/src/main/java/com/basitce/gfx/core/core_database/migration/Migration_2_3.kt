package com.basitce.gfx.core.core_database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 2 → 3:
 * - backups tablosu kaldırıldı.
 *   BackupEntity artık kullanılmıyor.
 *   PMP remote backup sistemi (ConfigRollbackManager) Room'a ihtiyaç duymuyor.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `backups`")
    }
}
