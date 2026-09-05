import re

manifest_path = "app/src/main/AndroidManifest.xml"
with open(manifest_path, "r") as f:
    content = f.read()

# Remove old service
content = re.sub(r'<service\s+android:name="\.service\.OverlayService"[\s\S]*?</service>', '', content)

new_service = """        <service
            android:name=".service.DarkScreenAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>"""

content = content.replace("</activity>", "</activity>\n\n" + new_service)

with open(manifest_path, "w") as f:
    f.write(content)
