package com.example.systemdarkoverlay.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.example.systemdarkoverlay.OverlayPrefs
import com.example.systemdarkoverlay.service.OverlayService

class OverlayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceContent(context)
        }
    }

    @Composable
    private fun GlanceContent(context: Context) {
        val isRunning = OverlayPrefs.isRunning(context)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color.DarkGray)
                .clickable(actionRunCallback<ToggleOverlayAction>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = if (isRunning) "Stop Overlay" else "Start Overlay",
                onClick = actionRunCallback<ToggleOverlayAction>()
            )
        }
    }
}

class ToggleOverlayAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        if (!Settings.canDrawOverlays(context)) {
            return
        }

        val isCurrentlyRunning = OverlayPrefs.isRunning(context)
        val intent = Intent(context, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_OPACITY, OverlayPrefs.getOpacity(context))
        }

        if (isCurrentlyRunning) {
            context.stopService(intent)
            OverlayPrefs.setRunning(context, false)
        } else {
            context.startForegroundService(intent)
            OverlayPrefs.setRunning(context, true)
        }

        OverlayWidget().updateAll(context)
        OpacityWidget().updateAll(context)
    }
}
