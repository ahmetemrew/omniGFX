package com.basitce.gfx.core.core_engine.shizuku

import javax.inject.Inject
import javax.inject.Singleton

data class PathProbeResult(
    val path: String,
    val exists: Boolean,
    val readable: Boolean,
    val writable: Boolean,
    val parentExists: Boolean,
    val parentWritable: Boolean,
    val canStat: Boolean,
    val messages: List<String>
)

/**
 * Hedef path'in Shizuku tarafından erişilebilirliğini test eder.
 *
 * Root olmadan /data/data erişimi cihazdan cihaza değişebilir.
 * Bu yüzden apply öncesi probe zorunludur.
 */
@Singleton
class ShizukuPathProbe @Inject constructor(
    private val remoteShell: RemoteShell
) {

    suspend fun probe(path: String): PathProbeResult {
        if (path.isBlank()) {
            return PathProbeResult(
                path = path,
                exists = false,
                readable = false,
                writable = false,
                parentExists = false,
                parentWritable = false,
                canStat = false,
                messages = listOf("Path boş.")
            )
        }

        val quotedPath = shellQuote(path)
        val parent = parentPath(path)
        val quotedParent = shellQuote(parent)

        val exists = remoteShell.execute("test -e $quotedPath").isSuccess

        val readable = exists &&
            remoteShell.execute("test -r $quotedPath").isSuccess

        val writable = exists &&
            remoteShell.execute("test -w $quotedPath").isSuccess

        val parentExists = remoteShell.execute("test -d $quotedParent").isSuccess

        val parentWritable = parentExists &&
            remoteShell.execute("test -w $quotedParent").isSuccess

        val canStat = exists &&
            remoteShell.execute("stat -c '%u %g %a' $quotedPath").isSuccess

        val messages = mutableListOf<String>()

        messages.add(
            "$path -> exists=$exists readable=$readable writable=$writable " +
                "parentExists=$parentExists parentWritable=$parentWritable canStat=$canStat"
        )

        if (!exists) {
            messages.add("Path bulunamadı.")
        }

        if (exists && !readable) {
            messages.add("Path okunamıyor.")
        }

        if (exists && readable && !writable) {
            messages.add("Path okunabiliyor ama yazılamıyor.")
        }

        if (!parentExists) {
            messages.add("Parent dizin bulunamadı.")
        }

        if (parentExists && !parentWritable) {
            messages.add("Parent dizin yazılabilir değil.")
        }

        return PathProbeResult(
            path = path,
            exists = exists,
            readable = readable,
            writable = writable,
            parentExists = parentExists,
            parentWritable = parentWritable,
            canStat = canStat,
            messages = messages
        )
    }

    private fun parentPath(path: String): String {
        val parent = path.substringBeforeLast('/')
        return if (parent.isBlank()) "/" else parent
    }

    private fun shellQuote(value: String): String {
        val escaped = value.replace("'", "'\\''")
        return "'$escaped'"
    }
}
