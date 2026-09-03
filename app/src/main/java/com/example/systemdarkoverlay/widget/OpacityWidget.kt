package com.example.systemdarkoverlay.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.systemdarkoverlay.OverlayPrefs
import com.example.systemdarkoverlay.service.OverlayService
import kotlin.math.roundToInt

class OpacityWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            OpacityWidgetContent(context)
        }
    }

    @Composable
    private fun OpacityWidgetContent(context: Context) {
        val opacity = OverlayPrefs.getOpacity(context)
        val opacityPercent = (opacity * 100).roundToInt()
        val levels = listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f)
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color.DarkGray),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opacity: $opacityPercent%", 
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // "Slider" bar
            Row(
                modifier = GlanceModifier.fillMaxWidth().height(24.dp).background(Color.LightGray),
                verticalAlignment = Alignment.CenterVertically
            ) {
                levels.forEach { level ->
                    val isSelected = opacity >= level - 0.05f
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(if (isSelected) Color(0xFF6200EE) else Color.Transparent)
                            .clickable(actionRunCallback<SetOpacityAction>(
                                actionParametersOf(SetOpacityAction.levelKey to level)
                            )),
                        contentAlignment = Alignment.Center
                    ) {
                         // empty box representing a slider segment
                    }
                }
            }
        }
    }
}

class SetOpacityAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        if (!Settings.canDrawOverlays(context)) return

        val newOpacity = parameters[levelKey] ?: 0.5f
        OverlayPrefs.setOpacity(context, newOpacity)

        if (OverlayPrefs.isRunning(context)) {
            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_OPACITY, newOpacity)
            }
            context.startForegroundService(intent)
        }

        OpacityWidget().updateAll(context)
    }

    companion object {
        val levelKey = ActionParameters.Key<Float>("opacity_level")
    }
}
