package com.example.systemdarkoverlay

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
                    viewModel.syncStateWithService(this@MainActivity)
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
                    onRequestPermission()
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
