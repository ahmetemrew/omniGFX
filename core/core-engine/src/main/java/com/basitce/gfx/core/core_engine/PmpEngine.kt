package com.basitce.gfx.core.core_engine

import java.io.File

/**
 * Shizuku / PMP dosya meta bilgisi.
 *
 * stat çıktısından parse edilir:
 * uid, gid, mode ve mümkünse SELinux context.
 */
data class FileMetadata(
    val uid: Int?,
    val gid: Int?,
    val mode: String?,
    val seContext: String? = null
)

/**
 * PMP = Pull-Modify-Push
 *
 * Bu interface ShizukuRemoteProcess veya başka bir privileged transport ile implement edilir.
 * ConfigEngine, Shizuku'nun kendisine doğrudan bağımlı değildir.
 *
 * Yeni eklenen gelişmiş özellikler default olarak pasiftir.
 * Böylece eski basit PmpEngine implementasyonları bozulmaz.
 */
interface PmpEngine {

    /**
     * Atomic push destekleniyor mu?
     * Genellikle temp dosya + mv/rename mantığı.
     */
    val supportsAtomicPush: Boolean
        get() = false

    /**
     * Remote copy / remote backup destekleniyor mu?
     */
    val supportsRemoteCopy: Boolean
        get() = false

    /**
     * SELinux context restore destekleniyor mu?
     */
    val supportsSelinux: Boolean
        get() = false

    suspend fun stat(remotePath: String): FileMetadata?

    suspend fun pull(
        remotePath: String,
        destination: File
    ): Boolean

    /**
     * Basit push.
     *
     * Atomic replace zorunlu değildir.
     * Metadata geri yüklemesi ConfigEngine tarafından ayrıca yapılabilir.
     */
    suspend fun push(
        source: File,
        remotePath: String
    ): Boolean

    suspend fun chown(
        remotePath: String,
        uid: Int,
        gid: Int
    ): Boolean

    suspend fun chmod(
        remotePath: String,
        mode: String
    ): Boolean

    /**
     * SELinux context geri yüklemesi.
     *
     * Örnek:
     * chcon u:object_r:app_data_file:s0 /data/data/com.example/files/config.ini
     */
    suspend fun chcon(
        remotePath: String,
        context: String
    ): Boolean = false

    /**
     * Remote -> remote copy.
     *
     * Remote backup için kullanılır.
     */
    suspend fun copyRemote(
        sourceRemotePath: String,
        destinationRemotePath: String
    ): Boolean = false

    /**
     * Metadata uygulayarak atomic push.
     *
     * Implementasyon beklentisi:
     * 1. Aynı dizinde temp dosya oluştur.
     * 2. source içeriğini temp dosyaya yaz.
     * 3. metadata'yı temp dosyaya uygula.
     * 4. mv/rename ile hedefi değiştir.
     * 5. mv başarısız olursa güvenli fallback yap.
     */
    suspend fun pushAtomic(
        source: File,
        remotePath: String,
        metadata: FileMetadata?,
        restoreSelinux: Boolean
    ): Boolean = push(source, remotePath)
}
