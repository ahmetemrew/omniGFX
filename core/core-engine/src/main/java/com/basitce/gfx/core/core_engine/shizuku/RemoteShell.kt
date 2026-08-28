package com.basitce.gfx.core.core_engine.shizuku

/**
 * Shizuku üzerinden shell komutu çalıştırmak için soyutlama.
 *
 * Capability checker, path probe ve process manager gibi katmanlar
 * doğrudan Shizuku API'sine değil bu arayüze bağımlı olur.
 */
interface RemoteShell {

    suspend fun execute(command: String): ShellCommandResult
}

data class ShellCommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean
        get() = exitCode == 0
}
