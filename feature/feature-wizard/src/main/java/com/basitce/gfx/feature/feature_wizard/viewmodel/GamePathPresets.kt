package com.basitce.gfx.feature.feature_wizard.viewmodel

import com.basitce.gfx.core.core_engine.workflow.DetectedFormat

data class GamePathPreset(
    val label: String,
    val description: String,
    val pathTemplate: String,
    val expectedFormat: DetectedFormat,
    val targetPackages: List<String> = emptyList()
)

object GamePathPresets {

    private val presets = listOf(
        GamePathPreset(
            label = "Grafik & FPS Ayarları",
            description = "FPS sınırı, çözünürlük, gölge kalitesi, anti-aliasing",
            pathTemplate = "/sdcard/Android/data/{{packageName}}/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini",
            expectedFormat = DetectedFormat.INI,
            targetPackages = listOf(
                "com.tencent.ig",
                "com.pubg.krmobile",
                "com.rekoo.pubgm",
                "com.vng.pubg",
                "com.tencent.tmgp.pubgmhd"
            )
        ),
        GamePathPreset(
            label = "Grafik Ayarları",
            description = "FPS, çözünürlük, gölge ve efekt ayarları",
            pathTemplate = "/sdcard/Android/data/{{packageName}}/files/UE4Game/CallOfDutyMobile/CallOfDutyMobile/Saved/Config/Android/UserCustom.ini",
            expectedFormat = DetectedFormat.INI,
            targetPackages = listOf(
                "com.activision.callofduty.shooter",
                "com.garena.game.codm",
                "com.tencent.tmgp.kr.codm"
            )
        ),
        GamePathPreset(
            label = "Grafik Config",
            description = "Render mesafesi, FPS sınırı, gölge kalitesi",
            pathTemplate = "/sdcard/Android/data/{{packageName}}/files/UnityCache/Shared/config.json",
            expectedFormat = DetectedFormat.JSON,
            targetPackages = listOf(
                "com.miHoYo.GenshinImpact",
                "com.miHoYo.Yuanshen"
            )
        ),
        GamePathPreset(
            label = "Grafik Config",
            description = "FPS, çözünürlük, render ayarları",
            pathTemplate = "/sdcard/Android/data/{{packageName}}/files/UnityCache/Shared/config.json",
            expectedFormat = DetectedFormat.JSON,
            targetPackages = listOf(
                "com.HoYoverse.StarRail",
                "com.HoYoverse.hkrpg"
            )
        ),
        GamePathPreset(
            label = "Grafik Ayarları",
            description = "FPS, çözünürlük, gölge kalitesi",
            pathTemplate = "/sdcard/Android/data/{{packageName}}/files/UE4Game/FortniteGame/FortniteGame/Saved/Config/Android/UserCustom.ini",
            expectedFormat = DetectedFormat.INI,
            targetPackages = listOf("com.epicgames.fortnite")
        ),
        GamePathPreset(
            label = "Oyun Data Dizini",
            description = "Oyunun ana data klasörünü aç",
            pathTemplate = "/data/data/{{packageName}}",
            expectedFormat = DetectedFormat.UNKNOWN
        ),
        GamePathPreset(
            label = "Harici Data Dizini",
            description = "sdcard üzerindeki oyun data klasörünü aç",
            pathTemplate = "/sdcard/Android/data/{{packageName}}",
            expectedFormat = DetectedFormat.UNKNOWN
        )
    )

    fun getPresetsFor(packageName: String): List<GamePathPreset> {
        val specific = presets.filter { preset ->
            preset.targetPackages.any {
                it.equals(packageName, ignoreCase = true)
            }
        }
        val general = presets.filter {
            it.targetPackages.isEmpty()
        }
        return (specific + general).map { preset ->
            preset.copy(
                pathTemplate = preset.pathTemplate.replace(
                    "{{packageName}}", packageName
                )
            )
        }
    }

    fun getFilePresetsFor(packageName: String): List<GamePathPreset> {
        return getPresetsFor(packageName).filter {
            it.expectedFormat != DetectedFormat.UNKNOWN
        }
    }
}
