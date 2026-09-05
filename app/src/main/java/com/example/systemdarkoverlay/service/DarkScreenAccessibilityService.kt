package com.example.systemdarkoverlay.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import com.example.systemdarkoverlay.OverlayPrefs

class DarkScreenAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isOverlayAdded = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        instance = this
        updateOverlay()
    }

    fun updateOverlay() {
        val isRunning = OverlayPrefs.isRunning(this)
        val opacity = OverlayPrefs.getOpacity(this)

        if (isRunning) {
            if (!isOverlayAdded) {
                showOverlay(opacity)
            } else {
                overlayView?.alpha = opacity
            }
        } else {
            if (isOverlayAdded) {
                hideOverlay()
            }
        }
    }

    private fun showOverlay(opacity: Float) {
        if (overlayView == null) {
            overlayView = FrameLayout(this).apply {
                setBackgroundColor(Color.BLACK)
            }
        }
        overlayView?.alpha = opacity

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        try {
            windowManager?.addView(overlayView, params)
            isOverlayAdded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        if (isOverlayAdded) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isOverlayAdded = false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
    
    override fun onAccessibilityButtonClicked() {
        super.onAccessibilityButtonClicked()
        val isCurrentlyRunning = OverlayPrefs.isRunning(this)
        OverlayPrefs.setRunning(this, !isCurrentlyRunning)
        updateOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        instance = null
    }

    companion object {
        var instance: DarkScreenAccessibilityService? = null
            private set

        fun isServiceEnabled(): Boolean = instance != null

        fun updateOverlayState() {
            instance?.updateOverlay()
        }
    }
}
