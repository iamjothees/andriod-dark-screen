package com.example.systemdarkoverlay

import android.content.Context
import android.content.SharedPreferences

object OverlayPrefs {
    private const val PREFS_NAME = "overlay_prefs"
    private const val KEY_IS_RUNNING = "is_running"
    private const val KEY_OPACITY = "opacity"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isRunning(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_RUNNING, false)
    }

    fun setRunning(context: Context, isRunning: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_RUNNING, isRunning).commit()
    }

    fun getOpacity(context: Context): Float {
        return getPrefs(context).getFloat(KEY_OPACITY, 0.5f)
    }

    fun setOpacity(context: Context, opacity: Float) {
        getPrefs(context).edit().putFloat(KEY_OPACITY, opacity).apply()
    }
}
