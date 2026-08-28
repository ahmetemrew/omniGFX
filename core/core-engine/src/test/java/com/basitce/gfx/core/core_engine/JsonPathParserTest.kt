package com.basitce.gfx.core.core_engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonPathParserTest {

    private fun createParser(): JsonPathParser = JsonPathParser()

    @Test
    fun `parse simple json object`() {
        val parser = createParser()
        parser.parse("""{"root":{"engine":{"fps":30}}}""")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("fps"))
    }

    @Test
    fun `updateValue modifies nested path`() {
        val parser = createParser()
        parser.parse("""{"root":{"engine":{"fps":30}}}""")
        parser.updateValue("$.root.engine.fps", 60)
        val serialized = parser.serialize()
        assertTrue(serialized.contains("60"))
    }

    @Test
    fun `updateValue creates missing path`() {
        val parser = createParser()
        parser.parse("""{"root":{}}""")
        parser.updateValue("$.root.engine.fps", 60)
        val serialized = parser.serialize()
        assertTrue(serialized.contains("60"))
    }

    @Test
    fun `updateValue coerces string to number`() {
        val parser = createParser()
        parser.parse("""{"root":{"engine":{"fps":30}}}""")
        parser.updateValue("$.root.engine.fps", "60")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("60"))
    }

    @Test
    fun `updateValue handles boolean`() {
        val parser = createParser()
        parser.parse("""{"settings":{"enable":false}}""")
        parser.updateValue("$.settings.enable", true)
        val serialized = parser.serialize()
        assertTrue(serialized.contains("true"))
    }

    @Test
    fun `serialize preserves original for empty content`() {
        val parser = createParser()
        parser.parse("")
        assertEquals("", parser.serialize())
    }

    @Test(expected = ConfigParserException::class)
    fun `parse throws on invalid json`() {
        val parser = createParser()
        parser.parse("not valid json {{{")
    }

    @Test
    fun `updateValue with array index`() {
        val parser = createParser()
        parser.parse("""{"items":[1,2,3]}""")
        parser.updateValue("$.items[1]", 99)
        val serialized = parser.serialize()
        assertTrue(serialized.contains("99"))
    }
}
