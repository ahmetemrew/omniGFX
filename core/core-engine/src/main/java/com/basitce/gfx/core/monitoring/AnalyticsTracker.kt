package com.basitce.gfx.core.monitoring

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap()
)

interface AnalyticsTracker {

    fun track(event: AnalyticsEvent)

    fun setUserId(userId: String?)

    fun setUserProperty(key: String, value: String?)
}
