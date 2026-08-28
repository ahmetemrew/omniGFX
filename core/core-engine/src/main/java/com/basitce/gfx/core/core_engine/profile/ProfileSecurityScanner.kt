package com.basitce.gfx.core.core_engine.profile

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kullanıcı tanımlı profilleri güvenlik açısından tarar.
 *
 * Özellikle path traversal ve kritik sistem dizinlerine erişimi engeller.
 */
@Singleton
class ProfileSecurityScanner @Inject constructor() {

    data class SecurityReport(
        val allowed: Boolean,
        val errors: List<String>,
        val warnings: List<String>
    )

    fun scan(
        profile: UserConfigProfile,
        candidatePaths: List<String>
    ): SecurityReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (profile.targetPathTemplate.isBlank()) {
            errors.add("Target path template boş olamaz.")
        }

        if (profile.targetPathTemplate.contains("..")) {
            errors.add("Target path template içinde '..' kullanılamaz.")
        }

        if (profile.targetPathTemplate.any { it == '\n' || it == '\r' }) {
            errors.add("Target path template yeni satır karakteri içeremez.")
        }

        profile.packageName?.let { packageName ->
            if (!isValidPackageName(packageName)) {
                errors.add("Geçersiz package name: $packageName")
            }
        }

        if (profile.packageName.isNullOrBlank() &&
            profile.targetPathTemplate.contains("{{packageName}}")
        ) {
            warnings.add(
                "Template {{packageName}} kullanıyor ama profile packageName tanımlanmamış."
            )
        }

        if (profile.patches.isEmpty()) {
            errors.add("Profilde en az bir patch olmalıdır.")
        }

        profile.patches.forEachIndexed { index, patch ->
            if (patch.path.isBlank()) {
                errors.add("Patch[$index] path boş olamaz.")
            }

            if (patch.path.any { it == '\n' || it == '\r' }) {
                errors.add("Patch[$index] path yeni satır karakteri içeremez.")
            }
        }

        if (candidatePaths.isEmpty()) {
            errors.add("Aday path üretilemedi.")
        }

        candidatePaths.forEach { path ->
            scanCandidatePath(
                path = path,
                profile = profile,
                errors = errors,
                warnings = warnings
            )
        }

        return SecurityReport(
            allowed = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    private fun scanCandidatePath(
        path: String,
        profile: UserConfigProfile,
        errors: MutableList<String>,
        warnings: MutableList<String>
    ) {
        if (path.isBlank()) {
            errors.add("Aday path boş.")
            return
        }

        if (!path.startsWith("/")) {
            errors.add("Aday path absolute olmalıdır: $path")
        }

        if (path.contains("..")) {
            errors.add("Aday path '..' içeremez: $path")
        }

        if (path.any { it == '\n' || it == '\r' }) {
            errors.add("Aday path yeni satır karakteri içeremez: $path")
        }

        val alwaysBlockedPrefixes = listOf(
            "/proc",
            "/sys",
            "/dev"
        )

        if (alwaysBlockedPrefixes.any { path.startsWith(it) }) {
            errors.add("Bu path her zaman engellidir: $path")
            return
        }

        val blockedPrefixes = listOf(
            "/system",
            "/vendor",
            "/boot",
            "/root",
            "/sbin",
            "/data/system",
            "/data/misc",
            "/data/adb",
            "/data/property"
        )

        if (blockedPrefixes.any { path.startsWith(it) }) {
            if (profile.options.allowUnsafeSystemPaths) {
                warnings.add("Güvensiz sistem path'i uzman modda kullanılıyor: $path")
            } else {
                errors.add("Sistem path'i engellendi: $path")
                return
            }
        }

        val allowedRoots = listOf(
            "/data/data/",
            "/data/user/",
            "/sdcard/Android/data/",
            "/storage/emulated/",
            "/data/local/tmp/"
        )

        val isInAllowedRoots = allowedRoots.any { path.startsWith(it) }

        if (!isInAllowedRoots) {
            if (!profile.options.expertMode) {
                errors.add("Expert mode kapalıyken sadece izinli app/storage path'leri kullanılabilir: $path")
            } else {
                warnings.add("Path standart allowlist dışında: $path")
            }
        }
    }

    private fun isValidPackageName(packageName: String): Boolean {
        return packageName.matches(PACKAGE_NAME_REGEX)
    }

    companion object {
        private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*$")
    }
}
