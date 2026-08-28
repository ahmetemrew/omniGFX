package com.basitce.gfx.core.monitoring

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileCrashReporter @Inject constructor(
    @ApplicationContext context: Context
) : CrashReporter {

    private val crashDir = File(context.filesDir, "crash_logs").apply {
        mkdirs()
    }

    private val lock = Any()

    override fun log(message: String) {
        appendToFile(
            file = File(crashDir, "app_logs.txt"),
            content = buildLogMessage(message)
        )
    }

    override fun recordException(
        throwable: Throwable,
        attributes: Map<String, String>
    ) {
        appendToFile(
            file = File(crashDir, "exceptions.txt"),
            content = buildThrowableReport(
                throwable = throwable,
                attributes = attributes,
                fatal = false
            )
        )
    }

    override fun logFatal(throwable: Throwable) {
        appendToFile(
            file = File(crashDir, "fatal_crashes.txt"),
            content = buildThrowableReport(
                throwable = throwable,
                attributes = emptyMap(),
                fatal = true
            )
        )
    }

    private fun buildLogMessage(message: String): String {
        return buildString {
            appendLine("-------------------------------")
            appendLine("Time: ${currentTimestamp()}")
            appendLine("Message: $message")
        }
    }

    private fun buildThrowableReport(
        throwable: Throwable,
        attributes: Map<String, String>,
        fatal: Boolean
    ): String {
        return buildString {
            appendLine("-------------------------------")
            appendLine("Time: ${currentTimestamp()}")
            appendLine("Fatal: $fatal")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} SDK ${Build.VERSION.SDK_INT}")
            appendLine("Exception: ${throwable.javaClass.name}")
            appendLine("Message: ${throwable.message}")

            if (attributes.isNotEmpty()) {
                appendLine("Attributes:")

                attributes.forEach { (key, value) ->
                    appendLine("  $key=$value")
                }
            }

            appendLine("StackTrace:")

            throwable.stackTraceToString().lines().forEach { line ->
                appendLine(line)
            }

            throwable.cause?.let { cause ->
                appendLine("Cause: ${cause.javaClass.name}")
                appendLine("Cause message: ${cause.message}")
            }
        }
    }

    private fun currentTimestamp(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }

    private fun appendToFile(file: File, content: String) {
        synchronized(lock) {
            try {
                file.appendText(content + "\n")
            } catch (e: Exception) {
                android.util.Log.e(
                    "FileCrashReporter",
                    "Crash log yazılamadı: ${file.absolutePath}",
                    e
                )
            }
        }
    }
}
