package com.basitce.gfx.core.monitoring

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: DebugAnalyticsTracker
    ): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        impl: FileCrashReporter
    ): CrashReporter
}
