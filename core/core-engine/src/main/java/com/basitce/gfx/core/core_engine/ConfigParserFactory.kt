package com.basitce.gfx.core.core_engine

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Parser seçimini dosya adı / içerik analizine göre yapar.
 */
@Singleton
class ConfigParserFactory @Inject constructor(
    private val jsonParserProvider: Provider<JsonPathParser>,
    private val xmlParserProvider: Provider<XmlParser>
) {

    private val iniExtensions = setOf(
        "ini",
        "cfg",
        "conf",
        "properties"
    )

    fun create(fileName: String, content: String): ConfigParser {
        val extension = fileName
            .substringAfterLast('.', "")
            .lowercase()

        return when {
            extension == "json" || (extension.isEmpty() && looksLikeJson(content)) -> {
                jsonParserProvider.get()
            }

            extension == "xml" -> {
                xmlParserProvider.get().apply {
                    formatHint = XmlParser.FormatHint.XML
                }
            }

            extension in iniExtensions -> {
                xmlParserProvider.get().apply {
                    formatHint = XmlParser.FormatHint.INI
                }
            }

            looksLikeJson(content) -> {
                jsonParserProvider.get()
            }

            looksLikeXml(content) -> {
                xmlParserProvider.get().apply {
                    formatHint = XmlParser.FormatHint.XML
                }
            }

            looksLikeIni(content) -> {
                xmlParserProvider.get().apply {
                    formatHint = XmlParser.FormatHint.INI
                }
            }

            else -> {
                xmlParserProvider.get().apply {
                    formatHint = XmlParser.FormatHint.AUTO
                }
            }
        }
    }

    private fun looksLikeJson(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("{") || trimmed.startsWith("[")
    }

    private fun looksLikeXml(content: String): Boolean {
        val trimmed = content.trim()

        return trimmed.startsWith("<?xml", ignoreCase = true) ||
            (trimmed.startsWith("<") && trimmed.contains(">"))
    }

    private fun looksLikeIni(content: String): Boolean {
        if (content.isBlank()) return false

        return content.lineSequence().any { line ->
            val trimmed = line.trim()

            (trimmed.startsWith("[") && trimmed.contains("]")) ||
                trimmed.contains("=")
        }
    }
}
