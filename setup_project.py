import os
import urllib.request
import stat

base_dir = "/Users/jotheeswaran/workspace/android-projects/SystemDarkOverlay"

files = {
    "settings.gradle.kts": """
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SystemDarkOverlay"
include(":app")
""",
    "build.gradle.kts": """
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    }
}
plugins {
    id("com.android.application") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
""",
    "gradle.properties": """
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
""",
    "app/build.gradle.kts": """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.systemdarkoverlay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.systemdarkoverlay"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // Glance for Widgets
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")
}
""",
    "app/src/main/AndroidManifest.xml": """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

    <application
        android:name=".OverlayApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Dark Overlay"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SystemDarkOverlay">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SystemDarkOverlay">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.OverlayService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse" />

        <receiver
            android:name=".widget.OverlayWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/overlay_widget_info" />
        </receiver>
    </application>

</manifest>
""",
    "app/src/main/res/values/strings.xml": """<resources>
    <string name="app_name">System Dark Overlay</string>
    <string name="notification_channel_id">overlay_channel</string>
    <string name="notification_channel_name">Overlay Service</string>
</resources>
""",
    "app/src/main/res/values/themes.xml": """<resources>
    <style name="Theme.SystemDarkOverlay" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
""",
    "app/src/main/res/xml/overlay_widget_info.xml": """<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp"
    android:minHeight="40dp"
    android:updatePeriodMillis="86400000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen" />
""",
    "app/src/main/java/com/example/systemdarkoverlay/OverlayApplication.kt": """package com.example.systemdarkoverlay

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class OverlayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = "Channel for Dark Overlay foreground service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(getString(R.string.notification_channel_id), name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
""",
    "app/src/main/java/com/example/systemdarkoverlay/service/OverlayService.kt": """package com.example.systemdarkoverlay.service

import android.app.Notification
import android.app.Service
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
    private var currentOpacity: Float = 0.5f

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
            .setContentTitle("Dark Overlay Active")
            .setContentText("System-wide dark overlay is running")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_OPACITY = "EXTRA_OPACITY"
        var isRunning = false // Simple global state for the widget to toggle
    }
}
""",
    "app/src/main/java/com/example/systemdarkoverlay/viewmodel/OverlayViewModel.kt": """package com.example.systemdarkoverlay.viewmodel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
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
        OverlayService.isRunning = isActive

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
        if (_isOverlayActive.value) {
            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_OPACITY, newOpacity)
            }
            context.startForegroundService(intent)
        }
    }
    
    fun syncStateWithService() {
        _isOverlayActive.value = OverlayService.isRunning
    }
}
""",
    "app/src/main/java/com/example/systemdarkoverlay/MainActivity.kt": """package com.example.systemdarkoverlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.systemdarkoverlay.viewmodel.OverlayViewModel
import com.example.systemdarkoverlay.ui.theme.SystemDarkOverlayTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: OverlayViewModel

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check permission on return
        if (viewModel.checkOverlayPermission(this)) {
            viewModel.toggleOverlay(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SystemDarkOverlayTheme {
                viewModel = viewModel()
                
                LaunchedEffect(Unit) {
                    viewModel.syncStateWithService()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OverlayScreen(viewModel = viewModel, onRequestPermission = {
                        requestOverlayPermission()
                    })
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${packageName}")
        )
        overlayPermissionLauncher.launch(intent)
    }
}

@Composable
fun OverlayScreen(viewModel: OverlayViewModel, onRequestPermission: () -> Unit) {
    val isActive by viewModel.isOverlayActive.collectAsState()
    val opacity by viewModel.opacity.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "System Dark Overlay",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            if (viewModel.checkOverlayPermission(context)) {
                viewModel.toggleOverlay(context)
            } else {
                onRequestPermission()
            }
        }) {
            Text(if (isActive) "Stop Overlay" else "Start Overlay")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Opacity: ${(opacity * 100).toInt()}%")
        Slider(
            value = opacity,
            onValueChange = { viewModel.updateOpacity(context, it) },
            valueRange = 0.1f..0.9f,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}
""",
    "app/src/main/java/com/example/systemdarkoverlay/ui/theme/Color.kt": """package com.example.systemdarkoverlay.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
""",
    "app/src/main/java/com/example/systemdarkoverlay/ui/theme/Theme.kt": """package com.example.systemdarkoverlay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun SystemDarkOverlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
""",
    "app/src/main/java/com/example/systemdarkoverlay/ui/theme/Type.kt": """package com.example.systemdarkoverlay.ui.theme

import androidx.compose.material3.Typography

val Typography = Typography()
""",
    "app/src/main/java/com/example/systemdarkoverlay/widget/OverlayWidget.kt": """package com.example.systemdarkoverlay.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.example.systemdarkoverlay.service.OverlayService

class OverlayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceContent(context)
        }
    }

    @Composable
    private fun GlanceContent(context: Context) {
        val isRunning = OverlayService.isRunning
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
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
            // Can't request permission from widget directly, normally would prompt a notification or open app.
            return
        }

        val intent = Intent(context, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_OPACITY, 0.5f) // Default or saved opacity
        }

        if (OverlayService.isRunning) {
            context.stopService(intent)
            OverlayService.isRunning = false
        } else {
            context.startForegroundService(intent)
            OverlayService.isRunning = true
        }

        // Update the widget UI
        OverlayWidget().updateAll(context)
    }
}
""",
    "app/src/main/java/com/example/systemdarkoverlay/widget/OverlayWidgetReceiver.kt": """package com.example.systemdarkoverlay.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class OverlayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OverlayWidget()
}
""",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml": """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@android:color/black" />
    <foreground android:drawable="@android:drawable/ic_menu_view" />
</adaptive-icon>
""",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml": """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@android:color/black" />
    <foreground android:drawable="@android:drawable/ic_menu_view" />
</adaptive-icon>
"""
}

def create_files():
    for rel_path, content in files.items():
        full_path = os.path.join(base_dir, rel_path)
        os.makedirs(os.path.dirname(full_path), exist_ok=True)
        with open(full_path, "w") as f:
            f.write(content)
            
    print("Files created.")

create_files()

# Download gradle wrapper if needed
os.chdir(base_dir)
os.system("gradle wrapper --gradle-version 8.5 || echo 'Gradle not found, skipping wrapper'")
