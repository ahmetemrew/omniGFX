package com.basitce.gfx.core.core_database.di

import android.content.Context
import androidx.room.Room
import com.basitce.gfx.core.core_database.AppDatabase
import com.basitce.gfx.core.core_database.dao.GameDao
import com.basitce.gfx.core.core_database.dao.ProfileDao
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.migration.MIGRATION_2_3
import com.basitce.gfx.core.core_database.migration.MIGRATION_3_4
import com.basitce.gfx.core.core_database.migration.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "omnigfx_database"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao = database.gameDao()

    @Provides
    fun provideSchemaDao(database: AppDatabase): SchemaDao = database.schemaDao()

    @Provides
    fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()
}
