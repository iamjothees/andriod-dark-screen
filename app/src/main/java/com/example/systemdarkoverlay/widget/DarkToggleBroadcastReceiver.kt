package com.example.systemdarkoverlay.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.systemdarkoverlay.OverlayPrefs
import com.example.systemdarkoverlay.service.OverlayService

class DarkToggleBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Settings.canDrawOverlays(context)) {
            return
        }

        val isCurrentlyRunning = OverlayPrefs.isRunning(context)
        val serviceIntent = Intent(context, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_OPACITY, OverlayPrefs.getOpacity(context))
        }

        try {
            if (isCurrentlyRunning) {
                context.stopService(serviceIntent)
                OverlayPrefs.setRunning(context, false)
            } else {
                context.startForegroundService(serviceIntent)
                OverlayPrefs.setRunning(context, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            OverlayPrefs.setRunning(context, !isCurrentlyRunning)
        }
    }
}
