package com.basitce.gfx.core.core_engine

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShizukuPmpModule {

    @Binds
    @Singleton
    abstract fun bindPmpEngine(
        shizukuPmpEngine: ShizukuPmpEngine
    ): PmpEngine
}
