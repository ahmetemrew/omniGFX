package com.basitce.gfx.feature.feature_wizard.viewmodel

import android.content.Context
import com.basitce.gfx.core.core_database.dao.SchemaDao
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.pipeline.PmpEvent
import com.basitce.gfx.core.core_engine.pipeline.PmpPipeline
import com.basitce.gfx.core.core_engine.pipeline.PmpResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileApplyViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun manualProfile() = ProfileEntity(
        id = "p1",
        schemaId = "s1",
        name = "Manual",
        userValuesJson = "{}",
        isManual = true,
        rawContent = "fps=60",
        targetFilePath = "/sdcard/game/config.ini"
    )

    private fun viewModel(pipeline: PmpPipeline): ProfileApplyViewModel =
        ProfileApplyViewModel(
            context = mockk<Context>(relaxed = true),
            pmpPipeline = pipeline,
            pmpEngine = mockk<PmpEngine>(relaxed = true),
            schemaDao = mockk<SchemaDao>(relaxed = true)
        )

    @Test
    fun `manual apply pipeline throw surfaces failure state instead of crashing`() = runTest {
        val pipeline = mockk<PmpPipeline>()
        every { pipeline.execute(any()) } throws RuntimeException("boom")
        val vm = viewModel(pipeline)

        vm.applyProfile(manualProfile())

        val state = vm.applyState.value
        assertEquals(false, state.isSuccess)
        assertFalse(state.isApplying)
        assertTrue(state.message!!.contains("boom"))
    }

    @Test
    fun `manual apply completed event surfaces success state`() = runTest {
        val pipeline = mockk<PmpPipeline>()
        every { pipeline.execute(any()) } returns flowOf(PmpEvent.Completed(PmpResult.NoChange))
        val vm = viewModel(pipeline)

        vm.applyProfile(manualProfile())

        val state = vm.applyState.value
        assertEquals(true, state.isSuccess)
        assertFalse(state.isApplying)
    }

    @Test
    fun `manual apply failed event surfaces failure state`() = runTest {
        val pipeline = mockk<PmpPipeline>()
        every { pipeline.execute(any()) } returns flowOf(PmpEvent.Failed("disk full"))
        val vm = viewModel(pipeline)

        vm.applyProfile(manualProfile())

        val state = vm.applyState.value
        assertEquals(false, state.isSuccess)
        assertFalse(state.isApplying)
        assertEquals("disk full", state.message)
    }
}
