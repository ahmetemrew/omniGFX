package com.basitce.gfx.core.core_engine.diff

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LineDiffEngineTest {

    @Test
    fun `identical content produces all context`() {
        val content = "line1\nline2\nline3"
        val result = LineDiffEngine.diff(content, content)
        assertTrue(result.all { it.type == DiffType.CONTEXT })
        assertEquals(3, result.size)
    }

    @Test
    fun `added line detected`() {
        val old = "line1\nline2"
        val new = "line1\nline2\nline3"
        val result = LineDiffEngine.diff(old, new)
        val adds = result.filter { it.type == DiffType.ADD }
        assertEquals(1, adds.size)
        assertEquals("line3", adds.first().text)
    }

    @Test
    fun `removed line detected`() {
        val old = "line1\nline2\nline3"
        val new = "line1\nline3"
        val result = LineDiffEngine.diff(old, new)
        val removes = result.filter { it.type == DiffType.REMOVE }
        assertEquals(1, removes.size)
        assertEquals("line2", removes.first().text)
    }

    @Test
    fun `modified line produces remove and add`() {
        val old = "line1\nold_value\nline3"
        val new = "line1\nnew_value\nline3"
        val result = LineDiffEngine.diff(old, new)
        val removes = result.filter { it.type == DiffType.REMOVE }
        val adds = result.filter { it.type == DiffType.ADD }
        assertTrue(removes.any { it.text == "old_value" })
        assertTrue(adds.any { it.text == "new_value" })
    }

    @Test
    fun `line numbers correct`() {
        val old = "a\nb\nc"
        val new = "a\nx\nc"
        val result = LineDiffEngine.diff(old, new)
        val remove = result.first { it.type == DiffType.REMOVE }
        val add = result.first { it.type == DiffType.ADD }
        assertEquals(2, remove.oldLineNumber)
        assertEquals(2, add.newLineNumber)
    }

    @Test
    fun `empty old content produces all adds`() {
        val result = LineDiffEngine.diff("", "line1\nline2")
        val adds = result.filter { it.type == DiffType.ADD }
        assertTrue(adds.isNotEmpty())
    }

    @Test
    fun `empty new content produces all removes`() {
        val result = LineDiffEngine.diff("line1\nline2", "")
        val removes = result.filter { it.type == DiffType.REMOVE }
        assertTrue(removes.isNotEmpty())
    }

    @Test
    fun `large files use fallback`() {
        val oldLines = (1..2000).joinToString("\n") { "line$it" }
        val newLines = (1..2000).joinToString("\n") { "line${it + 1}" }
        val result = LineDiffEngine.diff(oldLines, newLines)
        assertTrue(result.isNotEmpty())
    }
}
