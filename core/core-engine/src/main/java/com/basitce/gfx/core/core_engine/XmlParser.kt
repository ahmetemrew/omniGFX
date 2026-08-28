package com.basitce.gfx.core.core_engine

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.inject.Inject
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * XML ve INI tabanlı oyun config dosyaları için parser.
 *
 * XML path örnekleri:
 * CVars/r.PUBG.Quality
 * /Config/Graphics/@Quality
 * xpath://Config/Graphics/Quality
 *
 * INI path örnekleri:
 * CVars/r.PUBG.Quality
 * [/Script/Engine.Engine]/r.PUBG.Quality
 */
class XmlParser @Inject constructor() : ConfigParser {

    enum class FormatHint {
        AUTO,
        XML,
        INI
    }

    /**
     * ConfigEngine tarafından dosya uzantısına göre ayarlanabilir.
     * Özellikle boş dosyalarda yanlış format seçilmesini engeller.
     */
    var formatHint: FormatHint = FormatHint.AUTO

    private enum class Mode {
        UNKNOWN,
        EMPTY,
        XML,
        INI
    }

    private var mode: Mode = Mode.UNKNOWN
    private var originalContent: String = ""
    private var document: Document? = null
    private var hadXmlDeclaration: Boolean = false

    private val iniDocument = IniDocument()

    override fun parse(content: String) {
        originalContent = content
        document = null
        hadXmlDeclaration = false
        iniDocument.clear()

        mode = when {
            content.isBlank() -> Mode.EMPTY

            looksLikeXml(content) -> {
                if (tryParseXml(content)) {
                    Mode.XML
                } else if (tryParseIni(content)) {
                    Mode.INI
                } else {
                    throw ConfigParserException(
                        "İçerik XML olarak parse edilemedi ve INI olarak da değerlendirilemedi."
                    )
                }
            }

            looksLikeIni(content) -> {
                if (tryParseIni(content)) {
                    Mode.INI
                } else if (tryParseXml(content)) {
                    Mode.XML
                } else {
                    throw ConfigParserException(
                        "İçerik INI olarak parse edilemedi ve XML olarak da değerlendirilemedi."
                    )
                }
            }

            else -> {
                if (tryParseXml(content)) {
                    Mode.XML
                } else if (tryParseIni(content)) {
                    Mode.INI
                } else {
                    throw ConfigParserException(
                        "İçerik ne XML ne de INI formatında parse edilebildi."
                    )
                }
            }
        }
    }

    override fun updateValue(path: String, value: Any) {
        if (path.isBlank()) {
            throw ConfigParserException("Path boş olamaz.")
        }

        when (mode) {
            Mode.UNKNOWN -> {
                throw ConfigParserException("XmlParser kullanılmadan önce parse() çağrılmalıdır.")
            }

            Mode.EMPTY -> {
                mode = when (formatHint) {
                    FormatHint.XML -> Mode.XML
                    FormatHint.INI -> Mode.INI
                    FormatHint.AUTO -> {
                        if (looksLikeXmlPath(path)) Mode.XML else Mode.INI
                    }
                }

                // Recursive olarak yeni modda güncelle.
                updateValue(path, value)
            }

            Mode.XML -> updateXml(path, value)

            Mode.INI -> updateIni(path, value)
        }
    }

    override fun serialize(): String {
        return when (mode) {
            Mode.UNKNOWN -> {
                throw ConfigParserException("XmlParser kullanılmadan önce parse() çağrılmalıdır.")
            }

            Mode.EMPTY -> originalContent

            Mode.XML -> serializeXml()

            Mode.INI -> iniDocument.serialize()
        }
    }

    // region XML

    private fun tryParseXml(content: String): Boolean {
        return try {
            parseXmlInternal(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseXmlInternal(content: String) {
        hadXmlDeclaration = content.trimStart().startsWith("<?xml", ignoreCase = true)

        val factory = secureDocumentBuilderFactory()
        val builder = factory.newDocumentBuilder()

        val parsedDocument = builder.parse(InputSource(StringReader(content)))
        parsedDocument.documentElement.normalize()

        document = parsedDocument
    }

    private fun secureDocumentBuilderFactory(): DocumentBuilderFactory {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isValidating = false
            setXIncludeAware(false)

            // XML external entity saldırılarına karşı sertleştirme.
            runCatching {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            runCatching {
                setFeature("http://xml.org/sax/features/external-general-entities", false)
            }
            runCatching {
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }
            runCatching {
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            }
        }
    }

    private fun newDocument(): Document {
        return secureDocumentBuilderFactory().newDocumentBuilder().newDocument()
    }

    private fun updateXml(path: String, value: Any) {
        val trimmedPath = path.trim()

        if (trimmedPath.startsWith("xpath:")) {
            val doc = ensureDocument(trimmedPath)
            updateByXPath(doc, trimmedPath.removePrefix("xpath:").trim(), value)
            return
        }

        val doc = ensureDocument(trimmedPath)
        val (nodePath, attributeName) = splitAttribute(trimmedPath)

        val element = resolveOrCreateElement(doc, nodePath)

        if (attributeName != null) {
            element.setAttribute(attributeName, value.toString())
        } else {
            element.textContent = value.toString()
        }
    }

    private fun ensureDocument(path: String): Document {
        document?.let { return it }

        val rootName = extractFirstSegment(path) ?: "Config"

        val doc = try {
            newDocument()
        } catch (e: Exception) {
            throw ConfigParserException("Yeni XML document oluşturulamadı.", e)
        }

        val rootElement = doc.createElement(sanitizeXmlName(rootName))
        doc.appendChild(rootElement)

        document = doc
        return doc
    }

    private fun extractFirstSegment(path: String): String? {
        val cleaned = path
            .trim()
            .removePrefix("xpath:")
            .trim()
            .substringBefore('@')
            .trim()

        if (cleaned.isEmpty()) return null

        val withoutLeadingSlash = cleaned.trimStart('/')
        val first = withoutLeadingSlash.substringBefore('/').trim()

        return first.ifBlank { null }
    }

    private fun splitAttribute(path: String): Pair<String, String?> {
        val trimmed = path.trim()

        val atIndex = trimmed.lastIndexOf('@')
        if (atIndex == -1) return trimmed to null

        val nodePart = trimmed.substring(0, atIndex).trimEnd('/')
        val attributePart = trimmed.substring(atIndex + 1).trim()

        if (attributePart.isEmpty()) {
            throw ConfigParserException("Attribute adı boş olamaz. Path: $path")
        }

        return nodePart to attributePart
    }

    private fun resolveOrCreateElement(doc: Document, path: String): Element {
        val root = doc.documentElement
            ?: throw ConfigParserException("XML document root bulunamadı.")

        if (path.isBlank()) return root

        val cleanedPath = path.trim().trimStart('/')
        val segments = cleanedPath
            .split('/')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (segments.isEmpty()) return root

        var current: Element = root
        var startIndex = 0

        val firstSegment = segments.first()
        val sanitizedFirstSegment = sanitizeXmlName(firstSegment)

        if (firstSegment == root.nodeName || sanitizedFirstSegment == root.nodeName) {
            startIndex = 1
        }

        for (i in startIndex until segments.size) {
            val segment = segments[i]

            val existingChild = findChildElement(current, segment)

            current = existingChild ?: run {
                val safeName = sanitizeXmlName(segment)
                current.appendChild(doc.createElement(safeName)) as Element
            }
        }

        return current
    }

    private fun findChildElement(parent: Element, name: String): Element? {
        val safeName = sanitizeXmlName(name)
        val children = parent.childNodes

        for (i in 0 until children.length) {
            val node = children.item(i)

            if (node.nodeType != Node.ELEMENT_NODE) continue

            if (node.nodeName == name || node.nodeName == safeName) {
                return node as Element
            }
        }

        return null
    }

    private fun updateByXPath(doc: Document, xpath: String, value: Any) {
        if (xpath.isBlank()) {
            throw ConfigParserException("XPath ifadesi boş olamaz.")
        }

        val (nodeXpath, attributeName) = splitAttribute(xpath)

        val xPathEvaluator = XPathFactory.newInstance().newXPath()

        val node = try {
            xPathEvaluator.evaluate(nodeXpath, doc, XPathConstants.NODE) as? Node
        } catch (e: Exception) {
            throw ConfigParserException("XPath evaluate edilemedi. XPath: $xpath", e)
        }

        if (node == null) {
            throw ConfigPathNotFoundException("XPath ile node bulunamadı. XPath: $xpath")
        }

        if (attributeName != null) {
            if (node is Element) {
                node.setAttribute(attributeName, value.toString())
            } else {
                throw ConfigParserException(
                    "Attribute sadece Element node üzerine yazılabilir. XPath: $xpath"
                )
            }
        } else {
            node.textContent = value.toString()
        }
    }

    private fun serializeXml(): String {
        val doc = document ?: return originalContent

        return try {
            val transformerFactory = TransformerFactory.newInstance()

            runCatching {
                transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
            }
            runCatching {
                transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "")
            }

            val transformer = transformerFactory.newTransformer()

            transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                if (hadXmlDeclaration) "no" else "yes"
            )
            transformer.setOutputProperty(OutputKeys.INDENT, "no")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")

            val writer = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(writer))

            writer.toString()
        } catch (e: Exception) {
            throw ConfigParserException("XML serialize edilemedi.", e)
        }
    }

    private fun sanitizeXmlName(name: String): String {
        if (name.isBlank()) return "node"

        val builder = StringBuilder()

        name.forEachIndexed { index, ch ->
            val valid = if (index == 0) {
                ch.isLetterOrDigit() || ch == '_'
            } else {
                ch.isLetterOrDigit() || ch == '_' || ch == '-' || ch == '.'
            }

            if (valid) {
                builder.append(ch)
            } else {
                builder.append('_')
            }
        }

        if (builder.isEmpty()) return "node"

        if (!builder[0].isLetter() && builder[0] != '_') {
            builder.insert(0, '_')
        }

        return builder.toString()
    }

    private fun looksLikeXmlPath(path: String): Boolean {
        val trimmed = path.trim()

        return trimmed.startsWith("/") ||
            trimmed.startsWith("xpath:") ||
            trimmed.contains("@") ||
            trimmed.count { it == '/' } > 1
    }

    private fun looksLikeXml(content: String): Boolean {
        val trimmed = content.trim()

        return trimmed.startsWith("<?xml", ignoreCase = true) ||
            (trimmed.startsWith("<") && trimmed.contains(">"))
    }

    // endregion

    // region INI

    private fun tryParseIni(content: String): Boolean {
        return try {
            iniDocument.parse(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun updateIni(path: String, value: Any) {
        val trimmed = path.trim()

        val section: String
        val key: String

        if (trimmed.contains('/')) {
            val rawSection = trimmed
                .substringBeforeLast('/')
                .trim()
                .trimStart('/')

            section = rawSection
                .removeSurrounding("[", "]")
                .trim()

            key = trimmed
                .substringAfterLast('/')
                .trim()
        } else {
            section = ""
            key = trimmed.removeSurrounding("[", "]").trim()
        }

        if (key.isEmpty()) {
            throw ConfigParserException("INI key değeri boş olamaz. Path: $path")
        }

        iniDocument.update(section, key, value.toString())
    }

    private fun looksLikeIni(content: String): Boolean {
        if (content.isBlank()) return false

        return content.lineSequence().any { line ->
            val trimmed = line.trim()

            (trimmed.startsWith("[") && trimmed.contains("]")) ||
                trimmed.contains("=")
        }
    }

    // endregion

    // region INI Document

    /**
     * Basit ama yorum satırlarını ve boş satırları korumaya çalışan INI modeli.
     */
    private class IniDocument {

        private sealed class IniItem {
            data class Comment(val raw: String) : IniItem()
            object Blank : IniItem()
            data class SectionHeader(val name: String) : IniItem()
            data class Entry(
                val section: String,
                val key: String,
                val value: String
            ) : IniItem()
        }

        private val items = mutableListOf<IniItem>()

        fun clear() {
            items.clear()
        }

        fun parse(content: String) {
            items.clear()

            var currentSection = ""

            content.lines().forEach { rawLine ->
                val line = rawLine.trim()

                when {
                    rawLine.isBlank() -> {
                        items.add(IniItem.Blank)
                    }

                    line.startsWith(";") || line.startsWith("#") -> {
                        items.add(IniItem.Comment(rawLine))
                    }

                    line.startsWith("[") && line.contains("]") -> {
                        currentSection = line
                            .substringAfter("[")
                            .substringBefore("]")
                            .trim()

                        items.add(IniItem.SectionHeader(currentSection))
                    }

                    line.contains("=") -> {
                        val key = line.substringBefore('=').trim()
                        val value = line.substringAfter('=').trim()

                        if (key.isEmpty()) {
                            items.add(IniItem.Comment(rawLine))
                        } else {
                            items.add(
                                IniItem.Entry(
                                    section = currentSection,
                                    key = key,
                                    value = value
                                )
                            )
                        }
                    }

                    else -> {
                        items.add(IniItem.Comment(rawLine))
                    }
                }
            }
        }

        fun update(section: String, key: String, value: String) {
            val normalizedSection = section
                .trim()
                .removeSurrounding("[", "]")
                .trim()

            val newEntry = IniItem.Entry(
                section = normalizedSection,
                key = key.trim(),
                value = value.trim()
            )

            // 1) Mevcut key'i bul ve güncelle.
            val existingIndex = items.indexOfFirst { item ->
                (item as? IniItem.Entry)?.let { entry ->
                    entry.section.equals(normalizedSection, ignoreCase = true) &&
                        entry.key.equals(key, ignoreCase = true)
                } == true
            }

            if (existingIndex != -1) {
                items[existingIndex] = newEntry
                return
            }

            // 2) Section var mı?
            val sectionHeaderIndex = items.indexOfLast { item ->
                (item as? IniItem.SectionHeader)?.name.equals(normalizedSection, ignoreCase = true)
            }

            if (sectionHeaderIndex != -1) {
                var insertIndex = sectionHeaderIndex + 1

                for (i in sectionHeaderIndex + 1 until items.size) {
                    val item = items[i]

                    if (item is IniItem.SectionHeader) {
                        break
                    }

                    insertIndex = i + 1
                }

                items.add(insertIndex, newEntry)
                return
            }

            // 3) Global section'a ekle.
            if (normalizedSection.isEmpty()) {
                val firstHeaderIndex = items.indexOfFirst { it is IniItem.SectionHeader }

                if (firstHeaderIndex == -1) {
                    items.add(0, newEntry)
                } else {
                    items.add(firstHeaderIndex, newEntry)
                }

                return
            }

            // 4) Section yoksa sonuna ekle.
            if (items.isNotEmpty() && items.last() !is IniItem.Blank) {
                items.add(IniItem.Blank)
            }

            items.add(IniItem.SectionHeader(normalizedSection))
            items.add(newEntry)
        }

        fun serialize(): String {
            return items.joinToString(separator = "\n") { item ->
                when (item) {
                    is IniItem.Comment -> item.raw
                    is IniItem.Blank -> ""
                    is IniItem.SectionHeader -> "[${item.name}]"
                    is IniItem.Entry -> "${item.key}=${item.value}"
                }
            }
        }
    }

    // endregion
}
