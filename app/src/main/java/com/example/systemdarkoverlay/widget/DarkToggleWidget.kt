package com.example.systemdarkoverlay.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.example.systemdarkoverlay.R
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast

class DarkToggleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionSendBroadcast<DarkToggleBroadcastReceiver>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_round),
                    contentDescription = "App Logo Toggle",
                    contentScale = ContentScale.Fit,
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
    }
}
