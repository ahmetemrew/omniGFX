package com.basitce.gfx.core.core_engine.shizuku

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RemoteFileBrowserTest {
    @Test fun `Android toybox ISO dates list real game directories`() = runTest {
        val shell = object : RemoteShell {
            override suspend fun execute(command: String) = ShellCommandResult(0, """
                total 16
                drwxrwx--x  4 u0_a246 sdcard_rw 4096 2026-08-10 08:38 .
                drwxrwx--x 13 root    sdcard_rw 4096 2026-10-08 09:32 ..
                drwxrwx--x  3 u0_a246 sdcard_rw 4096 2026-08-10 08:49 cache
                drwxrwx--x 13 u0_a246 sdcard_rw 4096 2026-08-10 08:51 files
                -rw-rw---- 1 u0_a246 sdcard_rw 88 2026-11-07 13:31 User Settings.ini
            """.trimIndent(), "")
        }
        val result = RemoteFileBrowser(shell).list("/sdcard/Android/data/com.tencent.ig")
        assertEquals(listOf("cache", "files", "User Settings.ini"), result.entries.map { it.name })
        assertTrue(result.entries[1].isDirectory)
        assertEquals(88L, result.entries.last().sizeBytes)
        assertEquals("2026-08-10 08:51", result.entries[1].modifiedDate)
    }

    @Test fun `traditional ls dates remain supported`() = runTest {
        val shell = object : RemoteShell {
            override suspend fun execute(command: String) = ShellCommandResult(0,
                "-rw-r--r-- 1 shell shell 30 Nov 7 13:00 config.ini", "")
        }
        val entry = RemoteFileBrowser(shell).list("/sdcard").entries.single()
        assertEquals("config.ini", entry.name)
        assertEquals("Nov 7 13:00", entry.modifiedDate)
    }

    @Test fun `unknown listing format falls back instead of hiding entries`() = runTest {
        val shell = object : RemoteShell {
            override suspend fun execute(command: String) = ShellCommandResult(0,
                if (command.startsWith("ls -la")) "unknown date format" else "./\n../\nfiles/\nconfig.ini\n", "")
        }
        val entries = RemoteFileBrowser(shell).list("/sdcard").entries
        assertEquals(listOf("files", "config.ini"), entries.map { it.name })
        assertTrue(entries.first().isDirectory)
    }
}
