from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = Path('/sdcard/Download/17278-Photoroom.png')
RES = ROOT / 'app' / 'src' / 'main' / 'res'

LEGACY_SIZES = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

img = Image.open(SRC).convert('RGBA')

# Use the provided artwork directly for legacy icons.
for folder, size in LEGACY_SIZES.items():
    out_dir = RES / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    legacy = img.resize((size, size), Image.LANCZOS)
    legacy.save(out_dir / 'ic_launcher.png')
    legacy.save(out_dir / 'ic_launcher_round.png')

# Create an adaptive-foreground image with padding to avoid mask clipping.
canvas = Image.new('RGBA', (432, 432), (0, 0, 0, 0))
foreground = img.resize((348, 348), Image.LANCZOS)
offset = ((432 - 348) // 2, (432 - 348) // 2)
canvas.alpha_composite(foreground, dest=offset)
canvas.save(RES / 'drawable' / 'ic_launcher_foreground_image.png')

# Reuse the original art as an optional full-bleed fallback in mipmap-anydpi previews.
print('Generated launcher icons from', SRC)
