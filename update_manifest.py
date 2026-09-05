import re

path = "app/src/main/AndroidManifest.xml"
with open(path, "r") as f:
    content = f.read()

# Remove DarkToggleWidgetReceiver
content = re.sub(r'<receiver\s+android:name="\.widget\.DarkToggleWidgetReceiver"[\s\S]*?</receiver>', '', content)
# Remove DarkToggleBroadcastReceiver
content = re.sub(r'<receiver\s+android:name="\.widget\.DarkToggleBroadcastReceiver"[\s\S]*?</receiver>', '', content)

with open(path, "w") as f:
    f.write(content)
