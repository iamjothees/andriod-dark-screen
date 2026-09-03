import re

manifest_path = "app/src/main/AndroidManifest.xml"
with open(manifest_path, "r") as f:
    content = f.read()

new_receiver = """        <receiver
            android:name=".widget.DarkToggleBroadcastReceiver"
            android:exported="false">
        </receiver>
"""
if "DarkToggleBroadcastReceiver" not in content:
    content = content.replace("</application>", new_receiver + "    </application>")
    with open(manifest_path, "w") as f:
        f.write(content)
