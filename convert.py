import json
from PIL import Image, ImageDraw, ImageColor

# Load from file
with open('output.json') as f:
    pixels = json.load(f)

# Find bounds to normalize negative coordinates
min_x = min(p['x'] for p in pixels)
min_y = min(p['y'] for p in pixels)
max_x = max(p['x'] for p in pixels)
max_y = max(p['y'] for p in pixels)

width = max_x - min_x + 1
height = max_y - min_y + 1

# Create an image with transparent background (RGBA)
img = Image.new("RGBA", (width, height), color=(0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Draw pixels with alpha = 255 (fully opaque)
for p in pixels:
    x = p['x'] - min_x
    y = max_y - p['y']  # Flip y-axis
    color = ImageColor.getrgb(p['shade']) + (255,)  # Convert #hex -> (r, g, b, 255)
    draw.point((x, y), fill=color)

# Save with transparency
img.save("output.png", format="PNG")
img.show()
