package com.basitce.gfx

import android.app.Application
import android.util.Log
import com.basitce.gfx.core.monitoring.OmniGfxCrashHandler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class OmniApplication : Application() {

    @Inject
    lateinit var crashHandler: OmniGfxCrashHandler

    override fun onCreate() {
        super.onCreate()
        crashHandler.install()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= Log.ERROR) {
                if (t != null) {
                    Log.e(tag ?: "OmniGFX", message, t)
                } else {
                    Log.e(tag ?: "OmniGFX", message)
                }
            }
        }
    }
}
