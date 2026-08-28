package com.basitce.gfx.core.core_engine.profile

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ProfilePathResolverTest {

    private val resolver = ProfilePathResolver()

    @Test
    fun `absolute template resolves directly`() {
        val profile = UserConfigProfile(
            name = "Test",
            packageName = "com.test",
            targetPathTemplate = "/data/data/com.test/files/config.ini",
            patches = listOf(ProfilePatch("Key", "Value"))
        )
        val candidates = resolver.resolveCandidates(profile)
        assertTrue(candidates.contains("/data/data/com.test/files/config.ini"))
    }

    @Test
    fun `relative template resolves to multiple candidates`() {
        val profile = UserConfigProfile(
            name = "Test",
            packageName = "com.test",
            targetPathTemplate = "files/config.ini",
            patches = listOf(ProfilePatch("Key", "Value"))
        )
        val candidates = resolver.resolveCandidates(profile)
        assertTrue(candidates.any { it.startsWith("/data/data/com.test/") })
        assertTrue(candidates.any { it.startsWith("/sdcard/Android/data/com.test/") })
    }

    @Test
    fun `packageName placeholder substituted`() {
        val profile = UserConfigProfile(
            name = "Test",
            packageName = "com.example.game",
            targetPathTemplate = "/data/data/{{packageName}}/files/config.ini",
            patches = listOf(ProfilePatch("Key", "Value"))
        )
        val candidates = resolver.resolveCandidates(profile)
        assertTrue(candidates.contains("/data/data/com.example.game/files/config.ini"))
    }

    @Test
    fun `empty template returns empty list`() {
        val profile = UserConfigProfile(
            name = "Test",
            packageName = "com.test",
            targetPathTemplate = "",
            patches = listOf(ProfilePatch("Key", "Value"))
        )
        val candidates = resolver.resolveCandidates(profile)
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `double slashes normalized`() {
        val profile = UserConfigProfile(
            name = "Test",
            packageName = "com.test",
            targetPathTemplate = "/data/data/com.test//files//config.ini",
            patches = listOf(ProfilePatch("Key", "Value"))
        )
        val candidates = resolver.resolveCandidates(profile)
        assertTrue(candidates.all { !it.contains("//") })
    }

    @Test
    fun `trailing slash removed`() {
        val profile = UserConfigProfile(
            name = "Test",
            packageName = "com.test",
            targetPathTemplate = "/data/data/com.test/files/",
            patches = listOf(ProfilePatch("Key", "Value"))
        )
        val candidates = resolver.resolveCandidates(profile)
        assertTrue(candidates.all { !it.endsWith("/") || it == "/" })
    }
}
