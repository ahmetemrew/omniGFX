package com.basitce.gfx.core.core_engine

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

class ShizukuPmpEngineTest {

    @Test
    fun `pull times out, returns false and destroys the remote process`() = runBlocking {
        val destroyed = AtomicBoolean(false)
        // Fake remote process whose stdout stalls longer than the watchdog, then EOFs
        // so the blocked copy can unwind. The timeout still decides the result.
        val fakeProcess = object : Process() {
            override fun getOutputStream(): OutputStream = ByteArrayOutputStream()
            override fun getInputStream(): InputStream = object : InputStream() {
                override fun read(): Int {
                    Thread.sleep(400)
                    return -1
                }
            }
            override fun getErrorStream(): InputStream = ByteArrayInputStream(ByteArray(0))
            override fun waitFor(): Int = 0
            override fun exitValue(): Int = 0
            override fun destroy() {
                destroyed.set(true)
            }
        }
        val engine = object : ShizukuPmpEngine(bulkTimeoutMs = 150L) {
            override fun createProcess(command: String): Process = fakeProcess
        }
        val destination = File.createTempFile("pull_timeout", ".tmp")
        try {
            // Outer guard so a regression (missing watchdog) fails fast instead of hanging.
            val result = withTimeout(5_000L) { engine.pull("/sdcard/game/config.ini", destination) }
            assertFalse(result)
            assertTrue(destroyed.get())
        } finally {
            destination.delete()
        }
    }
}
