package com.basitce.gfx.feature.feature_wizard.viewmodel

import com.basitce.gfx.core.core_engine.workflow.DetectedFormat
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

object ConfigTreeBuilder {

    fun build(content: String, format: DetectedFormat): List<ConfigNode> {
        if (content.isBlank()) return emptyList()

        return try {
            when (format) {
                DetectedFormat.JSON -> buildJsonTree(content)
                DetectedFormat.XML -> buildXmlTree(content)
                DetectedFormat.INI -> buildIniTree(content)
                DetectedFormat.PLAIN_TEXT, DetectedFormat.UNKNOWN -> buildPlainTextTree(content)
            }
        } catch (e: Exception) {
            buildPlainTextTree(content)
        }
    }

    private fun buildIniTree(content: String): List<ConfigNode> {
        val nodes = mutableListOf<ConfigNode>()
        var currentSection = ""

        content.lines().forEachIndexed { index, rawLine ->
            val lineNum = index + 1
            val trimmed = rawLine.trim()

            when {
                trimmed.isBlank() -> {
                    nodes.add(
                        ConfigNode(
                            lineNumber = lineNum,
                            type = NodeType.BLANK,
                            rawLine = rawLine,
                            section = currentSection
                        )
                    )
                }
                trimmed.startsWith(";") || trimmed.startsWith("#") ||
                    trimmed.startsWith("//") -> {
                    nodes.add(
                        ConfigNode(
                            lineNumber = lineNum,
                            type = NodeType.COMMENT,
                            rawLine = rawLine,
                            section = currentSection
                        )
                    )
                }
                trimmed.startsWith("[") && trimmed.contains("]") -> {
                    currentSection = trimmed
                        .substringAfter("[")
                        .substringBefore("]")
                        .trim()
                    nodes.add(
                        ConfigNode(
                            lineNumber = lineNum,
                            type = NodeType.SECTION_HEADER,
                            key = currentSection,
                            rawLine = rawLine,
                            section = currentSection
                        )
                    )
                }
                trimmed.contains("=") -> {
                    val key = trimmed.substringBefore("=").trim()
                    val value = trimmed.substringAfter("=").trim()
                    val path = if (currentSection.isBlank()) key
                        else "$currentSection/$key"
                    nodes.add(
                        ConfigNode(
                            lineNumber = lineNum,
                            type = NodeType.KEY_VALUE,
                            path = path,
                            key = key,
                            value = value,
                            rawLine = rawLine,
                            section = currentSection
                        )
                    )
                }
                else -> {
                    nodes.add(
                        ConfigNode(
                            lineNumber = lineNum,
                            type = NodeType.UNKNOWN,
                            rawLine = rawLine,
                            section = currentSection
                        )
                    )
                }
            }
        }
        return nodes
    }

    private fun buildJsonTree(content: String): List<ConfigNode> {
        val nodes = mutableListOf<ConfigNode>()
        var lineCount = 0

        try {
            val json = JSONObject(content)
            traverseJson("$", json, nodes) { lineCount++ }
        } catch (e: Exception) {
            return buildPlainTextTree(content)
        }

        return nodes
    }

    private fun traverseJson(
        currentPath: String,
        value: Any,
        nodes: MutableList<ConfigNode>,
        nextLine: () -> Int
    ) {
        when (value) {
            is JSONObject -> {
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val childPath = "$currentPath.$key"
                    traverseJson(childPath, value.get(key), nodes, nextLine)
                }
            }
            is JSONArray -> {
                for (i in 0 until value.length()) {
                    val childPath = "$currentPath[$i]"
                    traverseJson(childPath, value.get(i), nodes, nextLine)
                }
            }
            else -> {
                val line = nextLine()
                val key = currentPath
                    .substringAfterLast('.')
                    .substringAfterLast('[')
                    .removeSuffix("]")
                nodes.add(
                    ConfigNode(
                        lineNumber = line,
                        type = NodeType.KEY_VALUE,
                        path = currentPath,
                        key = key,
                        value = value.toString(),
                        rawLine = "$key: $value",
                        section = currentPath.substringBefore('.', "")
                    )
                )
            }
        }
    }

    private fun buildXmlTree(content: String): List<ConfigNode> {
        val nodes = mutableListOf<ConfigNode>()
        var lineCount = 0

        try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isValidating = false
            }
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(content)))
            doc.documentElement.normalize()

            traverseXml(doc.documentElement, "", nodes) { lineCount++ }
        } catch (e: Exception) {
            return buildPlainTextTree(content)
        }

        return nodes
    }

    private fun traverseXml(
        element: Element,
        parentPath: String,
        nodes: MutableList<ConfigNode>,
        nextLine: () -> Int
    ) {
        val currentPath = if (parentPath.isBlank()) element.tagName
            else "$parentPath/${element.tagName}"

        val attrs = element.attributes
        if (attrs != null) {
            for (i in 0 until attrs.length) {
                val attr = attrs.item(i)
                val line = nextLine()
                nodes.add(
                    ConfigNode(
                        lineNumber = line,
                        type = NodeType.KEY_VALUE,
                        path = "$currentPath/@${attr.nodeName}",
                        key = "@${attr.nodeName}",
                        value = attr.nodeValue,
                        rawLine = "@${attr.nodeName}=\"${attr.nodeValue}\"",
                        section = currentPath
                    )
                )
            }
        }

        val children = element.childNodes
        var hasElementChildren = false

        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                hasElementChildren = true
                traverseXml(child as Element, currentPath, nodes, nextLine)
            }
        }

        if (!hasElementChildren) {
            val textContent = element.textContent.trim()
            if (textContent.isNotEmpty()) {
                val line = nextLine()
                nodes.add(
                    ConfigNode(
                        lineNumber = line,
                        type = NodeType.KEY_VALUE,
                        path = currentPath,
                        key = element.tagName,
                        value = textContent,
                        rawLine = "<${element.tagName}>$textContent</${element.tagName}>",
                        section = currentPath
                    )
                )
            }
        }
    }

    private fun buildPlainTextTree(content: String): List<ConfigNode> {
        return content.lines().mapIndexed { index, line ->
            val trimmed = line.trim()
            val type = when {
                trimmed.isBlank() -> NodeType.BLANK
                trimmed.startsWith("#") || trimmed.startsWith("//") ||
                    trimmed.startsWith(";") -> NodeType.COMMENT
                trimmed.contains("=") -> NodeType.KEY_VALUE
                else -> NodeType.UNKNOWN
            }
            ConfigNode(
                lineNumber = index + 1,
                type = type,
                path = if (type == NodeType.KEY_VALUE) trimmed.substringBefore("=").trim()
                    else "line_${index + 1}",
                key = if (type == NodeType.KEY_VALUE) trimmed.substringBefore("=").trim()
                    else "Satır ${index + 1}",
                value = if (type == NodeType.KEY_VALUE) trimmed.substringAfter("=").trim()
                    else line,
                rawLine = line
            )
        }
    }
}
