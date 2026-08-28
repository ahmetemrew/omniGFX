package com.basitce.gfx.core.core_engine.profile

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Profil path template'inden aday hedef path'ler üretir.
 *
 * Desteklenen placeholder'lar:
 * {{packageName}}
 * {{userId}}
 * {{dataDir}}
 * {{externalDataDir}}
 * {{externalFilesDir}}
 */
@Singleton
class ProfilePathResolver @Inject constructor() {

    fun resolveCandidates(
        profile: UserConfigProfile,
        userId: Int = 0
    ): List<String> {
        val packageName = profile.packageName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val template = profile.targetPathTemplate.trim()

        if (template.isBlank()) return emptyList()

        val candidates = LinkedHashSet<String>()

        val directResolved = normalize(
            substitute(
                template = template,
                packageName = packageName,
                userId = userId
            )
        )

        // Template absolute ise doğrudan aday olarak ekle.
        if (directResolved.startsWith("/")) {
            candidates.add(directResolved)
        }

        // Template relative ise yaygın Android data dizinlerini dene.
        if (packageName != null && !template.startsWith("/")) {
            val relativeResolved = normalize(
                substitute(
                    template = template,
                    packageName = packageName,
                    userId = userId
                )
            ).removePrefix("/")

            if (relativeResolved.isNotBlank()) {
                candidates.add("/data/data/$packageName/$relativeResolved")
                candidates.add("/data/user/$userId/$packageName/$relativeResolved")
                candidates.add("/sdcard/Android/data/$packageName/$relativeResolved")
                candidates.add("/storage/emulated/0/Android/data/$packageName/$relativeResolved")
            }
        }

        return candidates
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun substitute(
        template: String,
        packageName: String?,
        userId: Int
    ): String {
        val safePackageName = packageName.orEmpty()

        return template
            .replace("{{packageName}}", safePackageName)
            .replace("{{userId}}", userId.toString())
            .replace("{{dataDir}}", "/data/data/$safePackageName")
            .replace("{{externalDataDir}}", "/sdcard/Android/data/$safePackageName")
            .replace("{{externalFilesDir}}", "/sdcard/Android/data/$safePackageName/files")
    }

    private fun normalize(path: String): String {
        if (path.isBlank()) return path

        val collapsed = path.replace(Regex("/{2,}"), "/")

        return if (collapsed.length > 1 && collapsed.endsWith("/")) {
            collapsed.dropLast(1)
        } else {
            collapsed
        }
    }
}
