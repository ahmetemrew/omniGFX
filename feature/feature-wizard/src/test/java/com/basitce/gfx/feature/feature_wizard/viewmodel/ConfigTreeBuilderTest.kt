package com.basitce.gfx.feature.feature_wizard.viewmodel

import com.basitce.gfx.core.core_engine.JsonPathParser
import com.basitce.gfx.core.core_engine.XmlParser
import com.basitce.gfx.core.core_engine.workflow.DetectedFormat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ConfigTreeBuilderTest {
    @Test fun `root JSON array is editable`() {
        val content = """[{"fps":30},{"fps":60}]"""
        val nodes = ConfigTreeBuilder.build(content, DetectedFormat.JSON)
        assertEquals(2, nodes.size)
        val parser = JsonPathParser().apply { parse(content); updateValue(nodes[1].path, "90") }
        assertEquals(90, JSONArray(parser.serialize()).getJSONObject(1).getInt("fps"))
    }

    @Test fun `JSON property with dots is not interpreted as nested path`() {
        val content = """{"r.Quality":"01","nested":{"r.Quality":2}}"""
        val nodes = ConfigTreeBuilder.build(content, DetectedFormat.JSON)
        val target = nodes.first { it.value == "01" }
        val parser = JsonPathParser().apply { parse(content); updateValue(target.path, "02") }
        val result = JSONObject(parser.serialize())
        assertEquals("02", result.getString("r.Quality"))
        assertEquals(2, result.getJSONObject("nested").getInt("r.Quality"))
    }

    @Test fun `XML repeated siblings have distinct paths`() {
        val content = """<config><quality value="low"/><quality value="high"/></config>"""
        val nodes = ConfigTreeBuilder.build(content, DetectedFormat.XML)
        assertEquals(2, nodes.map { it.path }.distinct().size)
        val parser = XmlParser().apply { parse(content); updateValue(nodes[1].path, "ultra") }
        val result = ConfigTreeBuilder.build(parser.serialize(), DetectedFormat.XML)
        assertEquals(listOf("low", "ultra"), result.map { it.value })
    }

    @Test fun `INI comment and section paths are retained`() {
        val nodes = ConfigTreeBuilder.build("; comment\r\n[Graphics]\r\nfps=60\r\n", DetectedFormat.INI)
        assertEquals(NodeType.COMMENT, nodes[0].type)
        assertEquals("Graphics/fps", nodes[2].path)
        assertEquals(NodeType.BLANK, nodes.last().type)
    }
}
