package com.basitce.gfx.core.core_engine.shizuku

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShizukuStateModule {

    @Binds
    @Singleton
    abstract fun bindRemoteShell(
        impl: ShizukuRemoteShell
    ): RemoteShell

    @Binds
    @Singleton
    abstract fun bindGameProcessManager(
        impl: ShizukuGameProcessManager
    ): GameProcessManager
}
