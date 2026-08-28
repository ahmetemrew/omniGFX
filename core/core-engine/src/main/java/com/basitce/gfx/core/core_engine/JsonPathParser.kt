package com.basitce.gfx.core.core_engine

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.PathNotFoundException
import net.minidev.json.JSONValue
import javax.inject.Inject

/**
 * JSON tabanlı config dosyaları için JSONPath parser.
 *
 * Örnek:
 * $.root.engine.fps
 * $.graphics.maxFps
 * $['root']['engine']['fps']
 */
class JsonPathParser @Inject constructor() : ConfigParser {

    private val configuration: Configuration = Configuration.builder()
        .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
        .build()

    private var originalContent: String = ""
    private var root: Any? = null
    private var rootSet: Boolean = false
    private var parsed: Boolean = false

    override fun parse(content: String) {
        originalContent = content
        root = null
        rootSet = false
        parsed = false

        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            parsed = true
            return
        }

        try {
            val parsedJson = JsonPath.using(configuration).parse(content).json<Any?>()
            if (parsedJson !is Map<*, *> && parsedJson !is List<*>) {
                throw ConfigParserException("JSON içeriği geçerli bir Object veya Array olmalıdır.")
            }
            root = parsedJson
            rootSet = true
            parsed = true
        } catch (e: ConfigParserException) {
            throw e
        } catch (e: Throwable) {
            throw ConfigParserException("JSON içeriği parse edilemedi.", e)
        }
    }

    override fun updateValue(path: String, value: Any) {
        ensureParsed()

        val normalizedPath = normalizeJsonPath(path)
        val newValue = coerce(value)

        // Kök node'u değiştirmek
        if (normalizedPath == "$") {
            root = newValue
            rootSet = true
            return
        }

        // Önce mevcut path'i JsonPath üzerinden set etmeyi dene.
        try {
            if (rootSet && root != null) {
                val context = JsonPath.using(configuration).parse(root)
                context.set(normalizedPath, newValue)
                root = context.json<Any?>()
                rootSet = true
                return
            }
        } catch (e: PathNotFoundException) {
            // Path bulunamadı. Basit path ise güvenli şekilde oluşturmaya çalış.
        } catch (e: Exception) {
            // Eğer path kompleks / filter'lı bir JsonPath ise ve fallback desteklenmiyorsa hata ver.
            if (tokenize(normalizedPath) == null) {
                throw ConfigParserException(
                    "JSONPath güncellemesi başarısız oldu. Path: $path",
                    e
                )
            }
        }

        // Basit path'lerde eksik node'ları güvenli şekilde oluştur.
        if (!createAndSet(normalizedPath, newValue)) {
            throw ConfigPathNotFoundException(
                "JSONPath bulunamadı ve güvenli şekilde oluşturulamadı. Path: $path"
            )
        }
    }

    override fun serialize(): String {
        ensureParsed()

        if (!rootSet) {
            return if (originalContent.isBlank()) "" else originalContent
        }

        return if (root == null) {
            "null"
        } else {
            try {
                JSONValue.toJSONString(root)
            } catch (e: Exception) {
                throw ConfigParserException("JSON serialize edilemedi.", e)
            }
        }
    }

    private fun ensureParsed() {
        if (!parsed) {
            throw ConfigParserException("JsonPathParser kullanılmadan önce parse() çağrılmalıdır.")
        }
    }

    private fun normalizeJsonPath(path: String): String {
        val trimmed = path.trim()

        if (trimmed.isEmpty()) return "$"
        if (trimmed == "$") return "$"

        return if (trimmed.startsWith("$")) {
            trimmed
        } else {
            "$.$trimmed"
        }
    }

    /**
     * UI veya preset katmanından gelen String değerleri mümkün olduğunca
     * JSON primitive tiplerine çevirir.
     *
     * "60" -> 60
     * "true" -> true
     * "1.5" -> 1.5
     */
    private fun coerce(value: Any?): Any? {
        if (value !is String) return value

        val trimmed = value.trim()
        if (trimmed.isEmpty()) return value

        val lower = trimmed.lowercase()

        if (lower == "true") return true
        if (lower == "false") return false
        if (lower == "null") return null

        trimmed.toIntOrNull()?.let { return it }
        trimmed.toLongOrNull()?.let { return it }
        trimmed.toDoubleOrNull()?.let { return it }

        return value
    }

    @Suppress("UNCHECKED_CAST")
    private fun createAndSet(path: String, value: Any?): Boolean {
        val tokens = tokenize(path) ?: return false

        if (tokens.isEmpty()) {
            root = value
            rootSet = true
            return true
        }

        if (root == null) {
            root = createContainer(tokens.first())
            rootSet = true
        }

        var current: Any? = root

        for ((index, token) in tokens.withIndex()) {
            val isLast = index == tokens.lastIndex
            val nextToken = tokens.getOrNull(index + 1)

            when (token) {
                is Token.Property -> {
                    val map = current as? MutableMap<Any?, Any?> ?: return false

                    if (isLast) {
                        map[token.name] = value
                    } else {
                        val existing = map[token.name]

                        val child = if (existing == null) {
                            createContainer(nextToken).also { created ->
                                map[token.name] = created
                            }
                        } else {
                            existing
                        }

                        if (!isContainer(child)) return false
                        current = child
                    }
                }

                is Token.Index -> {
                    val list = current as? MutableList<Any?> ?: return false

                    ensureListSize(list, token.index + 1)

                    if (isLast) {
                        list[token.index] = value
                    } else {
                        val existing = list[token.index]

                        val child = if (existing == null) {
                            createContainer(nextToken).also { created ->
                                list[token.index] = created
                            }
                        } else {
                            existing
                        }

                        if (!isContainer(child)) return false
                        current = child
                    }
                }
            }
        }

        return true
    }

    private fun tokenize(path: String): List<Token>? {
        if (path == "$") return emptyList()
        if (!path.startsWith("$")) return null

        val raw = path.substring(1)
        if (raw.isEmpty()) return emptyList()

        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < raw.length) {
            when (raw[i]) {
                '.' -> {
                    i++

                    if (i >= raw.length) return null

                    val start = i
                    while (i < raw.length && raw[i] != '.' && raw[i] != '[') {
                        i++
                    }

                    val name = raw.substring(start, i)
                    if (name.isEmpty()) return null

                    tokens.add(Token.Property(name))
                }

                '[' -> {
                    val end = raw.indexOf(']', i)
                    if (end == -1) return null

                    val inside = raw.substring(i + 1, end).trim()

                    when {
                        inside.toIntOrNull() != null -> {
                            tokens.add(Token.Index(inside.toInt()))
                        }

                        inside.length >= 2 &&
                            inside.startsWith("'") &&
                            inside.endsWith("'") -> {
                            tokens.add(Token.Property(inside.substring(1, inside.length - 1)))
                        }

                        inside.length >= 2 &&
                            inside.startsWith("\"") &&
                            inside.endsWith("\"") -> {
                            tokens.add(Token.Property(inside.substring(1, inside.length - 1)))
                        }

                        else -> return null
                    }

                    i = end + 1
                }

                else -> return null
            }
        }

        return tokens
    }

    private fun createContainer(nextToken: Token?): Any {
        return if (nextToken is Token.Index) {
            ArrayList<Any?>()
        } else {
            LinkedHashMap<Any?, Any?>()
        }
    }

    private fun ensureListSize(list: MutableList<Any?>, size: Int) {
        while (list.size < size) {
            list.add(null)
        }
    }

    private fun isContainer(value: Any?): Boolean {
        return value is MutableMap<*, *> || value is MutableList<*>
    }

    private sealed class Token {
        data class Property(val name: String) : Token()
        data class Index(val index: Int) : Token()
    }
}
