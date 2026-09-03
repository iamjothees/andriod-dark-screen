import re

manifest_path = "/Users/jotheeswaran/workspace/android-projects/SystemDarkOverlay/app/src/main/java/com/example/systemdarkoverlay/service/OverlayService.kt"
with open(manifest_path, "r") as f:
    content = f.read()

# Add import for OverlayPrefs if missing
if "import com.example.systemdarkoverlay.OverlayPrefs" not in content:
    content = content.replace("import android.app.Service", "import android.app.Service\nimport com.example.systemdarkoverlay.OverlayPrefs")

# Modify onDestroy
replacement = """    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        OverlayPrefs.setRunning(this, false)
    }"""

content = re.sub(r'    override fun onDestroy\(\) \{[\s\S]*?\}', replacement, content)

with open(manifest_path, "w") as f:
    f.write(content)
