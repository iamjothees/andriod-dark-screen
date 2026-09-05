import re

path = "app/src/main/java/com/example/systemdarkoverlay/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

dialog_code = """
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
"""

# Insert dialog code before OverlayScreen
content = content.replace("@Composable\nfun OverlayScreen", dialog_code + "\n@Composable\nfun OverlayScreen")

# Update OverlayScreen to show dialog
overlay_screen_replacement = """
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
"""
content = re.sub(r'@Composable\nfun OverlayScreen.*?Column\(', overlay_screen_replacement, content, flags=re.DOTALL)

# Let's write the modified content back
with open(path, "w") as f:
    f.write(content)
