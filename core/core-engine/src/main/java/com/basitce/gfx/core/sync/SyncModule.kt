package com.basitce.gfx.core.sync

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindProfileCloudApi(
        impl: InMemoryProfileCloudApi
    ): ProfileCloudApi
}
