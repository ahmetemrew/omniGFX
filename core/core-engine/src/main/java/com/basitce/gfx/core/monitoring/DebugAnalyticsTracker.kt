package com.basitce.gfx.core.monitoring

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugAnalyticsTracker @Inject constructor() : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        Log.d("Analytics", "Event: ${event.name} params=${event.params}")
    }

    override fun setUserId(userId: String?) {
        Log.d("Analytics", "setUserId: $userId")
    }

    override fun setUserProperty(key: String, value: String?) {
        Log.d("Analytics", "setUserProperty: $key=$value")
    }
}
