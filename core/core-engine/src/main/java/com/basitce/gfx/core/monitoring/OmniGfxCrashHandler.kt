package com.basitce.gfx.core.monitoring

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmniGfxCrashHandler @Inject constructor(
    private val crashReporter: CrashReporter
) : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun install() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable
    ) {
        crashReporter.logFatal(throwable)

        defaultHandler?.uncaughtException(thread, throwable)
    }
}
