package com.basitce.gfx

data class AppInfo(
    val name: String,
    val packageName: String,
    val isGame: Boolean,
    val hasLauncherEntry: Boolean = false,
    val isUserFacing: Boolean = true,
    var isWhitelisted: Boolean = false,
    var isFocusGame: Boolean = false
)
