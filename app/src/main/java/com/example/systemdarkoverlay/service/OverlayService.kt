package com.example.systemdarkoverlay.service

import android.app.Notification
import android.app.Service
import com.example.systemdarkoverlay.OverlayPrefs
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.example.systemdarkoverlay.R

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlayView()
    }

    private fun setupOverlayView() {
        overlayView = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = currentOpacity
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        intent?.let {
            if (it.hasExtra(EXTRA_OPACITY)) {
                currentOpacity = it.getFloatExtra(EXTRA_OPACITY, 0.5f)
                overlayView.alpha = currentOpacity
            }
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setContentTitle("Dark Screen Active")
            .setContentText("System-wide dark screen is running")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        OverlayPrefs.setRunning(this, false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_OPACITY = "EXTRA_OPACITY"
        var isRunning = false
        var currentOpacity: Float = 0.5f // Global state for widgets and UI
    }
}
