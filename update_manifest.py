import re

manifest_path = "/Users/jotheeswaran/workspace/android-projects/SystemDarkOverlay/app/src/main/AndroidManifest.xml"
with open(manifest_path, "r") as f:
    content = f.read()

replacement = """        <service
            android:name=".service.OverlayService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="Screen darkening overlay" />
        </service>"""

content = re.sub(r'<service[\s\S]*?/>', replacement, content)

with open(manifest_path, "w") as f:
    f.write(content)
