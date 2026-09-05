import re

path = "app/src/main/java/com/example/systemdarkoverlay/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

# Add import
import_str = "import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen\n"
if "import androidx.core.splashscreen" not in content:
    content = content.replace("import android.os.Bundle\n", "import android.os.Bundle\n" + import_str)

# Replace the explicit call
content = content.replace("androidx.core.splashscreen.SplashScreen.installSplashScreen()", "installSplashScreen()")

with open(path, "w") as f:
    f.write(content)
