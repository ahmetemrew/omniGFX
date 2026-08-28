package com.basitce.gfx.core.core_engine.profile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSecurityScannerTest {

    private val scanner = ProfileSecurityScanner()

    private fun createProfile(
        targetPath: String = "/data/data/com.test/files/config.ini",
        packageName: String? = "com.test",
        patches: List<ProfilePatch> = listOf(ProfilePatch("Section/Key", "value"))
    ) = UserConfigProfile(
        name = "Test Profile",
        packageName = packageName,
        targetPathTemplate = targetPath,
        patches = patches
    )

    @Test
    fun `valid profile passes security scan`() {
        val profile = createProfile()
        val candidates = listOf("/data/data/com.test/files/config.ini")
        val report = scanner.scan(profile, candidates)
        assertTrue(report.allowed)
        assertTrue(report.errors.isEmpty())
    }

    @Test
    fun `path traversal blocked`() {
        val profile = createProfile(targetPath = "/data/data/com.test/../../../etc/passwd")
        val candidates = listOf("/etc/passwd")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
        assertTrue(report.errors.any { it.contains("..") })
    }

    @Test
    fun `proc path blocked`() {
        val profile = createProfile(targetPath = "/proc/self/mem")
        val candidates = listOf("/proc/self/mem")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `sys path blocked`() {
        val profile = createProfile(targetPath = "/sys/class/thermal")
        val candidates = listOf("/sys/class/thermal")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `dev path blocked`() {
        val profile = createProfile(targetPath = "/dev/block/boot")
        val candidates = listOf("/dev/block/boot")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `system path blocked without expert mode`() {
        val profile = createProfile(targetPath = "/system/build.prop")
        val candidates = listOf("/system/build.prop")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `data system path blocked`() {
        val profile = createProfile(targetPath = "/data/system/packages.xml")
        val candidates = listOf("/data/system/packages.xml")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `newline in path blocked`() {
        val profile = createProfile(targetPath = "/data/data/com.test/files/config.ini\nrm -rf /")
        val candidates = listOf("/data/data/com.test/files/config.ini\nrm -rf /")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `empty target path blocked`() {
        val profile = createProfile(targetPath = "")
        val candidates = emptyList<String>()
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `empty patches blocked`() {
        val profile = createProfile(patches = emptyList())
        val candidates = listOf("/data/data/com.test/files/config.ini")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `invalid package name blocked`() {
        val profile = createProfile(packageName = "com.test; rm -rf /")
        val candidates = listOf("/data/data/com.test/files/config.ini")
        val report = scanner.scan(profile, candidates)
        assertFalse(report.allowed)
    }

    @Test
    fun `allowed app data path passes`() {
        val profile = createProfile(
            targetPath = "/data/data/com.example.game/files/UE4Game/config.ini"
        )
        val candidates = listOf("/data/data/com.example.game/files/UE4Game/config.ini")
        val report = scanner.scan(profile, candidates)
        assertTrue(report.allowed)
    }

    @Test
    fun `allowed sdcard path passes`() {
        val profile = createProfile(
            targetPath = "/sdcard/Android/data/com.example.game/files/config.ini"
        )
        val candidates = listOf("/sdcard/Android/data/com.example.game/files/config.ini")
        val report = scanner.scan(profile, candidates)
        assertTrue(report.allowed)
    }
}
