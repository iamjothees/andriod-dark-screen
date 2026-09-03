package com.example.systemdarkoverlay.viewmodel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.example.systemdarkoverlay.OverlayPrefs
import com.example.systemdarkoverlay.service.OverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OverlayViewModel : ViewModel() {

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _opacity = MutableStateFlow(0.5f)
    val opacity: StateFlow<Float> = _opacity.asStateFlow()

    fun checkOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun toggleOverlay(context: Context) {
        val isActive = !_isOverlayActive.value
        _isOverlayActive.value = isActive
        OverlayPrefs.setRunning(context, isActive)

        val intent = Intent(context, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_OPACITY, _opacity.value)
        }

        if (isActive) {
            context.startForegroundService(intent)
        } else {
            context.stopService(intent)
        }
    }

    fun updateOpacity(context: Context, newOpacity: Float) {
        _opacity.value = newOpacity
        OverlayPrefs.setOpacity(context, newOpacity)
        if (_isOverlayActive.value) {
            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_OPACITY, newOpacity)
            }
            context.startForegroundService(intent)
        }
    }
    
    fun syncStateWithService(context: Context) {
        _isOverlayActive.value = OverlayPrefs.isRunning(context)
        _opacity.value = OverlayPrefs.getOpacity(context)
    }
}
