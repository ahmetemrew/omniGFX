package com.basitce.gfx.core.core_ui.theme

import android.database.ContentObserver
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Hareket tokenları. Sistem animasyon ölçeği 0 ise tüm süreler 0'a düşer → anında geçiş
 * (azaltılmış hareket). Sürekli dekoratif animasyon yoktur; yalnızca kullanıcı eylemine
 * cevap veya durum açıklaması.
 */
@Immutable
data class OmniMotion(
    val scale: Float = 1f,
    val short: Int = 120,
    val medium: Int = 220,
    val long: Int = 320
) {
    val reduced: Boolean get() = scale == 0f

    fun shortMs(): Int = (short * scale).toInt()
    fun mediumMs(): Int = (medium * scale).toInt()
    fun longMs(): Int = (long * scale).toInt()

    fun <T> shortTween() = tween<T>(durationMillis = shortMs(), easing = Standard)
    fun <T> mediumTween() = tween<T>(durationMillis = mediumMs(), easing = Emphasized)
    fun <T> longTween() = tween<T>(durationMillis = longMs(), easing = EmphasizedDecelerate)

    /** Basılı hal / şekil morphu için: azaltılmış harekette anında. */
    fun <T> snappySpring() = spring<T>(
        dampingRatio = if (reduced) Spring.DampingRatioNoBouncy else 0.72f,
        stiffness = if (reduced) Spring.StiffnessHigh else 420f
    )

    companion object {
        val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    }
}

val LocalOmniMotion = staticCompositionLocalOf { OmniMotion() }

/** Sistem animasyon ölçeğini okur ve değişikliklerini canlı izler. */
@Composable
fun rememberOmniMotion(): OmniMotion {
    val resolver = LocalContext.current.contentResolver
    var scale by remember {
        mutableFloatStateOf(
            runCatching {
                Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
            }.getOrDefault(1f)
        )
    }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                scale = runCatching {
                    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
                }.getOrDefault(1f)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return remember(scale) { OmniMotion(scale = if (scale == 0f) 0f else 1f) }
}
