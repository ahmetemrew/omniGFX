package com.basitce.gfx.core.core_engine.verification

import com.basitce.gfx.core.core_engine.FileMetadata
import com.basitce.gfx.core.core_engine.PmpEngine
import com.basitce.gfx.core.core_engine.shizuku.RemoteShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class VerifyOptions(
    val requireHash: Boolean = true,
    val requireSize: Boolean = true,
    val requireMetadata: Boolean = false,
    val requireSelinux: Boolean = false
)

data class VerifyResult(
    val success: Boolean,
    val message: String,
    val hashMatch: Boolean?,
    val sizeMatch: Boolean?,
    val metadataMatch: Boolean?,
    val warnings: List<String>
)

/**
 * Push sonrası hedef dosyayı doğrular.
 *
 * Kontroller:
 * - SHA-256 hash karşılaştırması
 * - Dosya boyutu karşılaştırması
 * - Opsiyonel metadata karşılaştırması
 * - Opsiyonel SELinux context karşılaştırması
 */
@Singleton
class PmpVerifier @Inject constructor(
    private val remoteShell: RemoteShell,
    private val pmpEngine: PmpEngine
) {

    suspend fun verifyPush(
        localFile: File,
        remotePath: String,
        expectedMetadata: FileMetadata? = null,
        options: VerifyOptions = VerifyOptions()
    ): VerifyResult {
        if (!localFile.exists()) {
            return VerifyResult(
                success = false,
                message = "Local doğrulama dosyası bulunamadı.",
                hashMatch = null,
                sizeMatch = null,
                metadataMatch = null,
                warnings = emptyList()
            )
        }

        val localHash = withContext(Dispatchers.IO) {
            runCatching {
                sha256Local(localFile)
            }.getOrNull()
        }

        return verify(
            localHash = localHash,
            localSize = localFile.length(),
            remotePath = remotePath,
            expectedMetadata = expectedMetadata,
            options = options
        )
    }

    suspend fun verifyContent(
        expectedContent: String,
        remotePath: String,
        expectedMetadata: FileMetadata? = null,
        options: VerifyOptions = VerifyOptions(requireSize = false)
    ): VerifyResult {
        val bytes = expectedContent.toByteArray(Charsets.UTF_8)

        val localHash = runCatching {
            sha256Bytes(bytes)
        }.getOrNull()

        return verify(
            localHash = localHash,
            localSize = bytes.size.toLong(),
            remotePath = remotePath,
            expectedMetadata = expectedMetadata,
            options = options
        )
    }

    private suspend fun verify(
        localHash: String?,
        localSize: Long,
        remotePath: String,
        expectedMetadata: FileMetadata?,
        options: VerifyOptions
    ): VerifyResult = withContext(Dispatchers.IO) {
        val warnings = mutableListOf<String>()

        if (remotePath.isBlank()) {
            return@withContext VerifyResult(
                success = false,
                message = "Remote path boş.",
                hashMatch = null,
                sizeMatch = null,
                metadataMatch = null,
                warnings = warnings
            )
        }

        if (!remotePath.startsWith("/")) {
            warnings.add("Remote path absolute değil: $remotePath")
        }

        // Hash
        val remoteHash = remoteHash(remotePath)

        if (remoteHash == null) {
            warnings.add("Remote hash alınamadı.")
        }

        val hashMatch = if (localHash != null && remoteHash != null) {
            localHash.equals(remoteHash, ignoreCase = true)
        } else {
            null
        }

        // Size
        val remoteSize = remoteSize(remotePath)

        if (remoteSize == null) {
            warnings.add("Remote dosya boyutu alınamadı.")
        }

        val sizeMatch = if (remoteSize != null) {
            localSize == remoteSize
        } else {
            null
        }

        // Metadata
        var metadataMatch: Boolean? = null

        if (options.requireMetadata) {
            if (expectedMetadata == null) {
                metadataMatch = false
                warnings.add("Metadata doğrulaması istendi ama expectedMetadata sağlanmadı.")
            } else {
                val actualMetadata = try {
                    pmpEngine.stat(remotePath)
                } catch (e: Exception) {
                    warnings.add("Metadata stat alınamadı: ${e.message}")
                    null
                }

                metadataMatch = compareMetadata(
                    expected = expectedMetadata,
                    actual = actualMetadata,
                    requireSelinux = options.requireSelinux
                )

                if (metadataMatch != true) {
                    warnings.add("Metadata doğrulanamadı.")
                }
            }
        }

        val hashOk = !options.requireHash || hashMatch == true
        val sizeOk = !options.requireSize || sizeMatch == true
        val metadataOk = !options.requireMetadata || metadataMatch == true

        val success = hashOk && sizeOk && metadataOk

        val message = if (success) {
            "Doğrulama başarılı."
        } else {
            "Doğrulama başarısız."
        }

        VerifyResult(
            success = success,
            message = message,
            hashMatch = hashMatch,
            sizeMatch = sizeMatch,
            metadataMatch = metadataMatch,
            warnings = warnings
        )
    }

    private suspend fun remoteHash(remotePath: String): String? {
        val quotedPath = shellQuote(remotePath)

        val commands = listOf(
            "sha256sum $quotedPath",
            "toybox sha256sum $quotedPath",
            "busybox sha256sum $quotedPath"
        )

        for (command in commands) {
            val result = remoteShell.execute(command)

            if (!result.isSuccess) continue

            val token = result.stdout
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()

            if (token != null && token.length == 64) {
                return token.lowercase()
            }
        }

        return null
    }

    private suspend fun remoteSize(remotePath: String): Long? {
        val quotedPath = shellQuote(remotePath)

        val result = remoteShell.execute("stat -c '%s' $quotedPath")

        if (!result.isSuccess) return null

        return result.stdout.trim().toLongOrNull()
    }

    private fun compareMetadata(
        expected: FileMetadata,
        actual: FileMetadata?,
        requireSelinux: Boolean
    ): Boolean {
        if (actual == null) return false

        var matches = true

        if (expected.uid != null && actual.uid != expected.uid) {
            matches = false
        }

        if (expected.gid != null && actual.gid != expected.gid) {
            matches = false
        }

        if (expected.mode != null && !modeEquals(expected.mode, actual.mode)) {
            matches = false
        }

        if (requireSelinux &&
            expected.seContext != null &&
            actual.seContext != expected.seContext
        ) {
            matches = false
        }

        return matches
    }

    private fun modeEquals(left: String?, right: String?): Boolean {
        if (left == null || right == null) return false

        val leftOctal = left.trim().toLongOrNull(8)
        val rightOctal = right.trim().toLongOrNull(8)

        return leftOctal != null && leftOctal == rightOctal
    }

    private fun sha256Local(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")

        file.inputStream().use { inputStream ->
            val buffer = ByteArray(8192)

            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().toHexString()
    }

    private fun sha256Bytes(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { byte ->
            (byte.toInt() and 0xFF)
                .toString(16)
                .padStart(2, '0')
        }
    }

    private fun shellQuote(value: String): String {
        val escaped = value.replace("'", "'\\''")
        return "'$escaped'"
    }
}
