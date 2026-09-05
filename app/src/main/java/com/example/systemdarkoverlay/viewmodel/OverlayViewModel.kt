package com.example.systemdarkoverlay.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.systemdarkoverlay.OverlayPrefs
import com.example.systemdarkoverlay.service.DarkScreenAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverlayViewModel : ViewModel() {

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _opacity = MutableStateFlow(0.5f)
    val opacity: StateFlow<Float> = _opacity.asStateFlow()

    fun checkOverlayPermission(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == context.packageName) {
                return true
            }
        }
        
        // Fallback to checking our local instance just in case
        return DarkScreenAccessibilityService.isServiceEnabled()
    }

    fun requestAccessibilityPermission(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun toggleOverlay(context: Context) {
        val isActive = !_isOverlayActive.value
        _isOverlayActive.value = isActive
        OverlayPrefs.setRunning(context, isActive)

        DarkScreenAccessibilityService.updateOverlayState()
    }

    fun updateOpacity(context: Context, newOpacity: Float) {
        _opacity.value = newOpacity
        OverlayPrefs.setOpacity(context, newOpacity)
        
        DarkScreenAccessibilityService.updateOverlayState()
    }
    
    fun syncStateWithService(context: Context) {
        // If accessibility service is disabled from settings by user, we should stop running state
        if (!checkOverlayPermission(context)) {
            OverlayPrefs.setRunning(context, false)
        }
        _isOverlayActive.value = OverlayPrefs.isRunning(context)
        _opacity.value = OverlayPrefs.getOpacity(context)
    }
}
