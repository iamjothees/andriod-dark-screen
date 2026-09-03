import re

manifest_path = "app/src/main/AndroidManifest.xml"
with open(manifest_path, "r") as f:
    content = f.read()

# Remove the two receivers
content = re.sub(r'<receiver\s+android:name="\.widget\.OverlayWidgetReceiver"[\s\S]*?</receiver>', '', content)
content = re.sub(r'<receiver\s+android:name="\.widget\.OpacityWidgetReceiver"[\s\S]*?</receiver>', '', content)

# Insert the new receiver before </application>
new_receiver = """        <receiver
            android:name=".widget.DarkToggleWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/dark_toggle_widget_info" />
        </receiver>
"""
content = content.replace("</application>", new_receiver + "    </application>")

with open(manifest_path, "w") as f:
    f.write(content)
