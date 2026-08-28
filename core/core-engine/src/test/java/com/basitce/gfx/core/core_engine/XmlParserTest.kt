package com.basitce.gfx.core.core_engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XmlParserTest {

    private fun createParser(): XmlParser = XmlParser()

    // ─── INI Mode Tests ──────────────────────────────

    @Test
    fun `parse ini content detects ini mode`() {
        val parser = createParser()
        parser.parse("[Section]\nKey=Value\n")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("[Section]"))
        assertTrue(serialized.contains("Key=Value"))
    }

    @Test
    fun `updateValue modifies ini key`() {
        val parser = createParser()
        parser.parse("[Section]\nKey=Value\n")
        parser.updateValue("Section/Key", "NewValue")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("Key=NewValue"))
    }

    @Test
    fun `updateValue preserves ini comments`() {
        val parser = createParser()
        parser.parse("; comment\n[Section]\nKey=Value\n")
        parser.updateValue("Section/Key", "NewValue")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("; comment"))
    }

    @Test
    fun `updateValue preserves blank lines`() {
        val parser = createParser()
        parser.parse("[Section]\n\nKey=Value\n")
        parser.updateValue("Section/Key", "NewValue")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("\n\n"))
    }

    @Test
    fun `updateValue adds key to existing section`() {
        val parser = createParser()
        parser.parse("[Section]\nKey1=Value1\n")
        parser.updateValue("Section/Key2", "Value2")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("Key2=Value2"))
    }

    @Test
    fun `updateValue creates section if missing`() {
        val parser = createParser()
        parser.parse("[Existing]\nKey=Value\n")
        parser.updateValue("NewSection/NewKey", "NewValue")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("[NewSection]"))
        assertTrue(serialized.contains("NewKey=NewValue"))
    }

    @Test
    fun `ini roundtrip preserves structure`() {
        val original = "[UserCustom DeviceProfile]\n+CVars=abc123\n+CVars=def456\n"
        val parser = createParser()
        parser.parse(original)
        val serialized = parser.serialize()
        assertEquals(original, serialized)
    }

    @Test
    fun `ini with brackets in section name`() {
        val parser = createParser()
        parser.parse("[/Script/Engine.Engine]\nr.PUBG.Quality=3\n")
        parser.updateValue("/Script/Engine.Engine/r.PUBG.Quality", "5")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("r.PUBG.Quality=5"))
    }

    // ─── XML Mode Tests ──────────────────────────────

    @Test
    fun `parse xml content detects xml mode`() {
        val parser = createParser()
        parser.parse("<?xml version=\"1.0\"?><Config><Graphics Quality=\"3\"/></Config>")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("Config"))
    }

    @Test
    fun `updateValue modifies xml element text`() {
        val parser = createParser()
        parser.parse("<Config><FPS>30</FPS></Config>")
        parser.updateValue("Config/FPS", "60")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("60"))
    }

    @Test
    fun `updateValue modifies xml attribute`() {
        val parser = createParser()
        parser.parse("<Config><Graphics Quality=\"3\"/></Config>")
        parser.updateValue("Config/Graphics/@Quality", "5")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("Quality=\"5\""))
    }

    @Test
    fun `updateValue creates missing xml elements`() {
        val parser = createParser()
        parser.parse("<Config/>")
        parser.updateValue("Config/Engine/FPS", "60")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("Engine"))
        assertTrue(serialized.contains("FPS"))
        assertTrue(serialized.contains("60"))
    }

    @Test
    fun `serialize xml preserves declaration`() {
        val parser = createParser()
        parser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Config/>")
        parser.updateValue("Config/Key", "Value")
        val serialized = parser.serialize()
        assertTrue(serialized.startsWith("<?xml"))
    }

    @Test
    fun `serialize xml omits declaration when absent`() {
        val parser = createParser()
        parser.parse("<Config/>")
        parser.updateValue("Config/Key", "Value")
        val serialized = parser.serialize()
        assertTrue(!serialized.startsWith("<?xml"))
    }

    // ─── Edge Cases ──────────────────────────────────

    @Test
    fun `parse empty content`() {
        val parser = createParser()
        parser.parse("")
        val serialized = parser.serialize()
        assertEquals("", serialized)
    }

    @Test(expected = ConfigParserException::class)
    fun `updateValue without parse throws`() {
        val parser = createParser()
        parser.updateValue("Section/Key", "Value")
    }

    @Test(expected = ConfigParserException::class)
    fun `serialize without parse throws`() {
        val parser = createParser()
        parser.serialize()
    }

    @Test
    fun `formatHint forces ini mode on empty`() {
        val parser = createParser()
        parser.formatHint = XmlParser.FormatHint.INI
        parser.parse("")
        parser.updateValue("Section/Key", "Value")
        val serialized = parser.serialize()
        assertTrue(serialized.contains("[Section]"))
        assertTrue(serialized.contains("Key=Value"))
    }
}
