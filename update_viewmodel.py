import re

path = "app/src/main/java/com/example/systemdarkoverlay/viewmodel/OverlayViewModel.kt"
with open(path, "r") as f:
    content = f.read()

# Remove imports
content = re.sub(r'import com\.example\.systemdarkoverlay\.widget\.DarkToggleWidget\n', '', content)
content = re.sub(r'import androidx\.glance\.appwidget\.updateAll\n', '', content)

# Remove widget update block
content = re.sub(r'\s*viewModelScope\.launch \{\s*DarkToggleWidget\(\)\.updateAll\(context\)\s*\}', '', content)

with open(path, "w") as f:
    f.write(content)
