package com.basitce.gfx.core.core_engine.shizuku

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote dosya sistemi girdisi.
 *
 * ls -la çıktısından parse edilen zengin bilgi içerir.
 */
data class RemoteFileSystemEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    /** Dosya boyutu (byte). Dizinler için null. */
    val sizeBytes: Long? = null,
    /** Unix izin string'i. Örn: rw-r--r-- */
    val permissions: String? = null,
    /** Sahip. Örn: u0_a123 veya root */
    val owner: String? = null,
    /** Son değişiklik tarihi. Örn: 2024-01-15 10:30 */
    val modifiedDate: String? = null
) {
    /** Dosya uzantısı (küçük harf). Örn: "ini", "json" */
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()

    /** Bilinen config dosyası mı? */
    val isKnownConfigFile: Boolean
        get() = extension in listOf(
            "ini", "cfg", "conf", "properties",
            "json", "xml", "yaml", "yml",
            "sav", "txt", "dat"
        )

    /** Boyutu okunabilir formata çevirir. */
    val readableSize: String
        get() {
            val bytes = sizeBytes ?: return ""
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
        }
}

data class RemoteListResult(
    val path: String,
    val entries: List<RemoteFileSystemEntry>,
    val error: String? = null
)

/**
 * Shizuku shell üzerinden remote dizin listeler.
 *
 * ls -la çıktısını parse eder:
 * drwxr-xr-x 2 root root 4096 2024-01-15 10:30 dirname
 * -rw-r--r-- 1 u0_a123 u0_a123 1234 2024-01-15 10:30 file.ini
 */
@Singleton
class RemoteFileBrowser @Inject constructor(
    private val remoteShell: RemoteShell
) {

    companion object {
        private val LS_LA_REGEX = Regex(
            "^([dlsb-][rwxsStT-]{9})\\s+" +
            "\\d+\\s+" +
            "(\\S+)\\s+" +
            "(\\S+)\\s+" +
            "(\\d+)\\s+" +
            "(\\S+\\s+\\S+\\s+\\S+)\\s+" +
            "(.+)$"
        )
    }

    suspend fun list(path: String): RemoteListResult {
        val targetPath = path.ifBlank { "/" }
        val quotedPath = shellQuote(targetPath)

        val result = remoteShell.execute("ls -la $quotedPath")

        if (!result.isSuccess) {
            return listSimple(targetPath, quotedPath)
        }

        val entries = mutableListOf<RemoteFileSystemEntry>()

        result.stdout
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { !it.startsWith("total") }
            .forEach { line ->
                val match = LS_LA_REGEX.find(line)
                if (match != null) {
                    val permissions = match.groupValues[1]
                    val owner = match.groupValues[2]
                    val size = match.groupValues[4].toLongOrNull()
                    val dateStr = match.groupValues[5]
                    val name = match.groupValues[6].trim()

                    if (name == "." || name == "..") return@forEach

                    val isDirectory = permissions.startsWith("d")
                    val isSymlink = permissions.startsWith("l")

                    val actualName = if (isSymlink && name.contains(" -> ")) {
                        name.substringBefore(" -> ")
                    } else {
                        name
                    }

                    entries.add(
                        RemoteFileSystemEntry(
                            name = actualName,
                            path = joinPath(targetPath, actualName),
                            isDirectory = isDirectory,
                            sizeBytes = if (isDirectory) null else size,
                            permissions = permissions,
                            owner = owner,
                            modifiedDate = dateStr
                        )
                    )
                }
            }

        return RemoteListResult(
            path = targetPath,
            entries = sortEntries(entries),
            error = null
        )
    }

    private suspend fun listSimple(
        targetPath: String,
        quotedPath: String
    ): RemoteListResult {
        val result = remoteShell.execute("ls -1 -a -p $quotedPath")
        if (!result.isSuccess) {
            return RemoteListResult(
                path = targetPath,
                entries = emptyList(),
                error = result.stderr.ifBlank { "Dizin listelenemedi." }
            )
        }

        val entries = mutableListOf<RemoteFileSystemEntry>()
        result.stdout
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { it != "./" && it != "../" }
            .forEach { line ->
                val isDirectory = line.endsWith("/")
                val name = line.removeSuffix("/").trim()
                if (name.isBlank()) return@forEach

                entries.add(
                    RemoteFileSystemEntry(
                        name = name,
                        path = joinPath(targetPath, name),
                        isDirectory = isDirectory
                    )
                )
            }

        return RemoteListResult(
            path = targetPath,
            entries = sortEntries(entries),
            error = null
        )
    }

    fun parentPath(path: String): String? {
        if (path == "/") return null
        val trimmed = path.trimEnd('/')
        val parent = trimmed.substringBeforeLast('/')
        return if (parent.isBlank()) "/" else parent
    }

    fun breadcrumbParts(path: String): List<Pair<String, String>> {
        if (path.isBlank() || path == "/") return listOf("Root" to "/")

        val parts = mutableListOf("Root" to "/")
        val segments = path.trimStart('/').split('/')
        var currentPath = ""
        segments.forEach { segment ->
            currentPath += "/$segment"
            parts.add(segment to currentPath)
        }
        return parts
    }

    private fun sortEntries(entries: List<RemoteFileSystemEntry>): List<RemoteFileSystemEntry> {
        return entries.sortedWith(
            compareByDescending<RemoteFileSystemEntry> { it.isDirectory }
                .thenByDescending { it.isKnownConfigFile }
                .thenBy { it.name.lowercase() }
        )
    }

    private fun joinPath(base: String, child: String): String {
        if (base == "/") return "/$child"
        return if (base.endsWith("/")) {
            "$base$child"
        } else {
            "$base/$child"
        }
    }

    private fun shellQuote(value: String): String {
        val escaped = value.replace("'", "'\\''")
        return "'$escaped'"
    }
}
