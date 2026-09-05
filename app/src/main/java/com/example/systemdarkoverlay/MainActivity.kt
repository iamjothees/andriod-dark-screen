package com.example.systemdarkoverlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SystemDarkOverlayTheme {
                viewModel = viewModel()
                
                LaunchedEffect(Unit) {
                    viewModel.syncStateWithService(this@MainActivity)
                }

                var showOnboarding by remember { mutableStateOf(!com.example.systemdarkoverlay.OverlayPrefs.isSetupComplete(this@MainActivity)) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showOnboarding) {
                        com.example.systemdarkoverlay.ui.components.OnboardingScreen(
                            onFinish = {
                                com.example.systemdarkoverlay.OverlayPrefs.setSetupComplete(this@MainActivity)
                                showOnboarding = false
                            },
                            modifier = Modifier.systemBarsPadding()
                        )
                    } else {
                        OverlayScreen(viewModel = viewModel, onRequestPermission = {
                            requestOverlayPermission()
                        }, modifier = Modifier.systemBarsPadding())
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.syncStateWithService(this)
        }
    }

    private fun requestOverlayPermission() {
        viewModel.requestAccessibilityPermission(this)
        // Note: Accessibility settings don't return a strict result code like OVERLAY_PERMISSION,
        // so the user will return to the app and the LaunchedEffect or onResume will pick it up.
    }
}


@Composable
fun AccessibilityGuideDialog(onDismiss: () -> Unit, onProceed: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Enable Accessibility", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        },
        text = {
            Column {
                Text("To draw the dark mask over your lock screen, you need to enable the Accessibility Service.")
                Spacer(modifier = Modifier.height(16.dp))
                Text("1. Tap 'Proceed to Settings'.")
                Text("2. Scroll to 'Downloaded Apps' (or 'Installed Apps').")
                Text("3. Select 'Dark Screen'.")
                Text("4. Turn ON 'Use Dark Screen'.")
                Text("5. (Recommended) Turn ON the 'Dark Screen shortcut' for a floating button.")
            }
        },
        confirmButton = {
            TextButton(onClick = onProceed) {
                Text("Proceed to Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun OverlayScreen(viewModel: OverlayViewModel, onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    val isActive by viewModel.isOverlayActive.collectAsState()
    val opacity by viewModel.opacity.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AccessibilityGuideDialog(
            onDismiss = { showDialog = false },
            onProceed = {
                showDialog = false
                onRequestPermission()
            }
        )
    }

    Column(

        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dark Screen",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        com.example.systemdarkoverlay.ui.components.CustomMoonSwitch(
            checked = isActive,
            onCheckedChange = {
                if (viewModel.checkOverlayPermission(context)) {
                    viewModel.toggleOverlay(context)
                } else {
                    showDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        com.example.systemdarkoverlay.ui.components.CustomMoonSlider(
            value = opacity,
            onValueChange = { viewModel.updateOpacity(context, it) },
            valueRange = 0.1f..0.9f,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}
