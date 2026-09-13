package com.fapiko.jetbrains.plugins.better_direnv.settings

data class DirenvSettings(
    @JvmField val direnvEnabled: Boolean,
    @JvmField val direnvTrusted: Boolean
) {
    fun isDirenvEnabled(): Boolean = direnvEnabled
    fun isDirenvTrusted(): Boolean = direnvTrusted
}