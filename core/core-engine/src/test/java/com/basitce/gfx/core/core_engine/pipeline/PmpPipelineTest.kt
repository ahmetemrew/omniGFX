package com.basitce.gfx.core.core_engine.pipeline

import android.content.Context
import com.basitce.gfx.core.core_engine.ConfigParser
import com.basitce.gfx.core.core_engine.ConfigParserFactory
import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.verification.PmpVerifier
import com.basitce.gfx.core.core_engine.verification.VerifyResult
import io.mockk.coVerify
import kotlinx.coroutines.flow.toList
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import app.cash.turbine.test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PmpPipelineTest {

    private lateinit var context: Context
    private lateinit var pmpEngine: PmpEngine
    private lateinit var parserFactory: ConfigParserFactory
    private lateinit var verifier: PmpVerifier
    private lateinit var pipeline: PmpPipeline

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        pmpEngine = mockk(relaxed = true)
        parserFactory = mockk(relaxed = true)
        verifier = mockk(relaxed = true)
        pipeline = PmpPipeline(context, pmpEngine, parserFactory, verifier)

        val cacheDir = File(System.getProperty("java.io.tmpdir"), "test_pmp")
        cacheDir.mkdirs()
        every { context.cacheDir } returns cacheDir
        every { context.filesDir } returns File(cacheDir, "files").apply { mkdirs() }
    }

    @Test
    fun `execute emits Started event first`() = runTest {
        coEvery { pmpEngine.pull(any(), any()) } returns false

        val request = PmpRequest(
            remotePath = "/data/data/com.test/config.ini",
            patches = listOf(PmpPatch("Section/Key", "value"))
        )

        pipeline.execute(request).test {
            val first = awaitItem()
            assertTrue(first is PmpEvent.Started)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `execute emits Failed when pull fails`() = runTest {
        coEvery { pmpEngine.stat(any()) } returns null
        coEvery { pmpEngine.pull(any(), any()) } returns false

        val request = PmpRequest(
            remotePath = "/data/data/com.test/config.ini",
            patches = listOf(PmpPatch("Section/Key", "value")),
            options = PmpPipelineOptions(createRemoteBackup = false)
        )

        pipeline.execute(request).test {
            awaitItem() // Started
            awaitItem() // Pulling
            val failed = awaitItem()
            assertTrue(failed is PmpEvent.Failed)
            assertTrue((failed as PmpEvent.Failed).message.contains("çekilemedi"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `execute emits NoChange when content unchanged`() = runTest {
        val content = "[Section]\nKey=original\n"
        val parser = mockk<ConfigParser>()
        every { parser.parse(content) } returns Unit
        every { parser.updateValue("Section/Key", "original") } returns Unit
        every { parser.serialize() } returns content

        coEvery { pmpEngine.stat(any()) } returns null
        coEvery { pmpEngine.pull(any(), any()) } answers {
            val dest = secondArg<File>()
            dest.writeText(content)
            true
        }
        every { parserFactory.create(any(), any()) } returns parser

        val request = PmpRequest(
            remotePath = "/data/data/com.test/config.ini",
            patches = listOf(PmpPatch("Section/Key", "original")),
            options = PmpPipelineOptions(createRemoteBackup = false)
        )

        pipeline.execute(request).test {
            awaitItem() // Started
            awaitItem() // Pulling
            awaitItem() // Decoding
            awaitItem() // Modifying
            val completed = awaitItem()
            assertTrue(completed is PmpEvent.Completed)
            val result = (completed as PmpEvent.Completed).result
            assertTrue(result is PmpResult.NoChange)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `regex mode applies pattern replacement`() = runTest {
        val content = "r.PUBGDeviceFPSLow=30\nr.PUBGDeviceFPSHigh=60\n"
        val expectedContent = "r.PUBGDeviceFPSLow=60\nr.PUBGDeviceFPSHigh=60\n"

        coEvery { pmpEngine.stat(any()) } returns null
        coEvery { pmpEngine.pull(any(), any()) } answers {
            val dest = secondArg<File>()
            dest.writeText(content)
            true
        }
        coEvery { pmpEngine.pushAtomic(any(), any(), any(), any()) } returns true
        coEvery { pmpEngine.supportsAtomicPush } returns true
        coEvery { verifier.verifyContent(any(), any(), any(), any()) } returns VerifyResult(
            success = true,
            message = "OK",
            hashMatch = true,
            sizeMatch = null,
            metadataMatch = null,
            warnings = emptyList()
        )

        val request = PmpRequest(
            remotePath = "/data/data/com.test/UserCustom.ini",
            patches = listOf(
                PmpPatch(
                    path = "fps_rule",
                    value = 60,
                    regexPattern = "r\\.PUBGDeviceFPSLow=(\\d+)",
                    regexReplacementTemplate = "r.PUBGDeviceFPSLow={{value}}"
                )
            ),
            patchMode = PmpPatchMode.REGEX,
            options = PmpPipelineOptions(createRemoteBackup = false)
        )

        pipeline.execute(request).test {
            awaitItem() // Started
            awaitItem() // Pulling
            awaitItem() // Decoding
            awaitItem() // Modifying
            awaitItem() // Encoding
            awaitItem() // Pushing
            awaitItem() // Verifying
            val completed = awaitItem()
            assertTrue(completed is PmpEvent.Completed)
            val result = (completed as PmpEvent.Completed).result
            assertTrue(result is PmpResult.Success)
            val success = result as PmpResult.Success
            assertEquals(expectedContent, success.updatedContent)
            cancelAndIgnoreRemainingEvents()
        }
    }
    private fun prepareWrite() {
        coEvery { pmpEngine.stat(any()) } returns null
        every { pmpEngine.supportsAtomicPush } returns true
        every { pmpEngine.supportsRemoteCopy } returns true
        coEvery { pmpEngine.copyRemote(any(), any()) } returns true
        coEvery { pmpEngine.pull(any(), any()) } answers {
            secondArg<File>().writeText("old")
            true
        }
        coEvery { pmpEngine.pushAtomic(any(), any(), any(), any()) } returns true
        coEvery { verifier.verifyContent(any(), any(), any(), any()) } returns VerifyResult(
            true, "OK", true, null, null, emptyList())
    }

    @Test
    fun `raw replacement is backed up and verified`() = runTest {
        prepareWrite()
        val events = pipeline.execute(PmpRequest("/sdcard/config.ini", emptyList(), replacementContent = "new")).toList()
        val success = (events.last() as PmpEvent.Completed).result as PmpResult.Success
        assertTrue(success.remoteBackupPath!!.contains(".omnigfx."))
        coVerify(exactly = 1) { pmpEngine.copyRemote("/sdcard/config.ini", any()) }
        coVerify(exactly = 1) { verifier.verifyContent("new", any(), any(), any()) }
    }

    @Test
    fun `verification exception fails and restores original`() = runTest {
        prepareWrite()
        coEvery { verifier.verifyContent("new", any(), any(), any()) } throws IllegalStateException("Disconnected")
        val events = pipeline.execute(PmpRequest("/sdcard/config.ini", emptyList(), replacementContent = "new")).toList()
        assertTrue(events.last() is PmpEvent.Failed)
        assertTrue((events.last() as PmpEvent.Failed).message.contains("geri yüklendi"))
        coVerify(exactly = 2) { pmpEngine.pushAtomic(any(), any(), any(), any()) }
        coVerify { verifier.verifyContent("old", any(), any(), any()) }
    }

    @Test
    fun `changed original aborts before any write`() = runTest {
        prepareWrite()
        val events = pipeline.execute(PmpRequest("/sdcard/config.ini", emptyList(), replacementContent = "new", expectedOriginalContent = "older")).toList()
        assertTrue(events.last() is PmpEvent.Failed)
        coVerify(exactly = 0) { pmpEngine.pushAtomic(any(), any(), any(), any()) }
    }

    @Test
    fun `backup failure aborts before write`() = runTest {
        prepareWrite()
        coEvery { pmpEngine.copyRemote(any(), any()) } returns false
        val events = pipeline.execute(PmpRequest("/sdcard/config.ini", emptyList(), replacementContent = "new")).toList()
        assertTrue(events.last() is PmpEvent.Failed)
        coVerify(exactly = 0) { pmpEngine.pushAtomic(any(), any(), any(), any()) }
    }

    @Test
    fun `invalid regex aborts instead of applying partial patches`() = runTest {
        prepareWrite()
        val events = pipeline.execute(PmpRequest("/sdcard/config.ini", listOf(
            PmpPatch("first", "changed", "old", "{{value}}"),
            PmpPatch("second", "value", "[", "{{value}}")
        ), patchMode = PmpPatchMode.REGEX)).toList()
        assertTrue(events.last() is PmpEvent.Failed)
        coVerify(exactly = 0) { pmpEngine.pushAtomic(any(), any(), any(), any()) }
    }

}
