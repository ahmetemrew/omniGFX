package com.basitce.gfx.core.monitoring

interface CrashReporter {

    fun log(message: String)

    fun recordException(
        throwable: Throwable,
        attributes: Map<String, String> = emptyMap()
    )

    fun logFatal(throwable: Throwable)
}
