package com.basitce.gfx.feature.feature_wizard.viewmodel

import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupWizardViewModelTest {

    private fun viewModel(): SetupWizardViewModel = SetupWizardViewModel(
        context = mockk(relaxed = true),
        pmpEngine = mockk(relaxed = true),
        pmpPipeline = mockk(relaxed = true),
        parserFactory = mockk(relaxed = true),
        remoteFileBrowser = mockk(relaxed = true),
        gameProcessManager = mockk(relaxed = true),
        shizukuStateManager = mockk(relaxed = true),
        pathProbe = mockk(relaxed = true),
        workflowCache = mockk(relaxed = true),
        pmpVerifier = mockk(relaxed = true),
        rollbackManager = mockk(relaxed = true),
        gameDao = mockk(relaxed = true),
        schemaDao = mockk(relaxed = true),
        profileDao = mockk(relaxed = true)
    )

    @Test
    fun `undo redo flags are exposed in observable state and update on edit undo redo`() {
        val vm = viewModel()

        assertFalse(vm.state.value.canUndo)
        assertFalse(vm.state.value.canRedo)

        vm.updateNodeValue("node", "60")
        assertTrue(vm.state.value.canUndo)
        assertFalse(vm.state.value.canRedo)

        vm.undo()
        assertFalse(vm.state.value.canUndo)
        assertTrue(vm.state.value.canRedo)

        vm.redo()
        assertTrue(vm.state.value.canUndo)
        assertFalse(vm.state.value.canRedo)
    }

    @Test
    fun `undo flag reflects remaining stack depth`() {
        val vm = viewModel()

        vm.updateNodeValue("a", "1")
        vm.updateNodeValue("b", "2")
        assertTrue(vm.state.value.canUndo)
        assertFalse(vm.state.value.canRedo)

        vm.undo()
        assertTrue(vm.state.value.canUndo)
        assertTrue(vm.state.value.canRedo)

        vm.undo()
        assertFalse(vm.state.value.canUndo)
        assertTrue(vm.state.value.canRedo)
    }
}
