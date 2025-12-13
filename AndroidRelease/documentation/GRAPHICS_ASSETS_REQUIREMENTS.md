# 🎨 Play Store Graphics Assets Requirements

Complete specifications for all required and optional graphics assets.

## Required Assets

### 1. App Icon ✅
**Status**: Already created in project

**Location**: `androidApp/src/duoplay/res/mipmap-*/`
- `ic_launcher.png` - Standard icon
- `ic_launcher_round.png` - Round icon variant

**Specifications**:
- **Format**: PNG (32-bit)
- **Densities** (automatically generated):
  - mdpi: 48×48 px
  - hdpi: 72×72 px
  - xhdpi: 96×96 px
  - xxhdpi: 144×144 px
  - xxxhdpi: 192×192 px
- **Design**: White play symbol, dual dots, circle on black background
- **Notes**:
  - No transparency in outer square
  - Adaptive icon with foreground and background layers
  - System handles rounding and masking

### 2. Feature Graphic ⚠️ REQUIRED
**Status**: NEEDS TO BE CREATED

**Specifications**:
- **Size**: 1024 × 500 pixels
- **Format**: PNG or JPG
- **File size**: Maximum 1 MB
- **Requirements**:
  - No transparency (use solid background)
  - High resolution
  - Shows app branding
  - Can include app name and tagline

**Design Recommendations**:
```
┌────────────────────────────────────────┐
│                                        │
│     [App Icon]   DUO PLAY             │
│                                        │
│              Your Gaming Platform      │
│                                        │
└────────────────────────────────────────┘
        1024 × 500 px - Black background
```

**Creating the Feature Graphic**:
1. Use design tool (Figma, Canva, Photoshop)
2. Canvas: 1024 × 500 px
3. Background: Black (#000000)
4. Include app icon (scaled appropriately)
5. App name: "DUO PLAY" in white
6. Tagline: Optional subtitle
7. Export as PNG or JPG

### 3. Screenshots ⚠️ REQUIRED (2-8 images)
**Status**: NEED TO BE CREATED

**Phone Screenshots**:
- **Minimum requirement**: 2 screenshots
- **Maximum allowed**: 8 screenshots
- **Dimensions**:
  - Recommended: 1080 × 2340 px (19.5:9)
  - Or: 1080 × 1920 px (16:9)
  - Or: 1080 × 2400 px (20:9)
- **Format**: PNG or JPG (24-bit RGB, no alpha)
- **File size**: Maximum 8 MB each

**What to Capture**:
1. **Home/Main Screen**: App opened showing main interface
2. **Key Feature 1**: Gaming interface in action
3. **Key Feature 2**: (optional) Another important feature
4. **Visual Appeal**: Clean, well-designed screens

**Screenshot Guidelines**:
- Use actual app UI (no mockups)
- High quality, clear text
- No device frames needed (Play Store adds them)
- Consistent status bar appearance
- No personal information visible
- Portrait orientation preferred

**Capturing Screenshots**:
```bash
# Using Android Studio or adb
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Or use Android device:
Power + Volume Down
```

**Editing Screenshots**:
- Crop to remove unnecessary parts
- Ensure exact dimensions (1080 × 2340 recommended)
- Remove sensitive information if any
- Verify readability

### 4. Tablet Screenshots (Optional but Recommended)
**Specifications**:
- **Dimensions**: 1920 × 1200 px or 2560 × 1600 px
- **Aspect ratio**: 16:10 or 16:9
- **Format**: PNG or JPG
- **Quantity**: 2-8 images

## Optional Assets

### 5. Promo Video (Optional)
**Specifications**:
- **Platform**: YouTube only
- **Length**: 30 seconds to 2 minutes recommended
- **Format**: Any format accepted by YouTube
- **Content**: App demonstration, features showcase

**Video Ideas**:
- Quick tour of main features
- Gameplay demonstration
- Value proposition explanation
- How to get started

### 6. TV Banner (Optional - if supporting Android TV)
**Specifications**:
- **Size**: 1280 × 720 pixels
- **Format**: PNG or JPG
- **Notes**: Only needed for Android TV apps

### 7. Wear OS Screenshots (Optional - if supporting Wear OS)
**Specifications**:
- **Size**: 384 × 384 px (square) or 450 × 450 px
- **Format**: PNG or JPG
- **Quantity**: 3-8 images

## Asset Checklist

### Must Have (Required)
- [x] App Icon (already in project)
- [ ] Feature Graphic (1024 × 500)
- [ ] Phone Screenshots (minimum 2)

### Should Have (Recommended)
- [ ] Phone Screenshots (4-8 total)
- [ ] Tablet Screenshots
- [ ] Promo Video

### Nice to Have (Optional)
- [ ] TV Banner (if TV support)
- [ ] Wear screenshots (if Wear support)

## Quick Start: Creating Assets

### For Feature Graphic:
1. Open Figma/Canva/Photoshop
2. Create 1024 × 500 canvas
3. Black background
4. Add app icon (exported from project)
5. Add "DUO PLAY" text in white
6. Export as PNG

### For Screenshots:
1. Run app on device or emulator
2. Navigate to key screens
3. Take screenshots:
   - Main screen
   - Key features (2-6 more screens)
4. Transfer to computer
5. Verify dimensions and quality
6. Upload to Play Console

## Tools & Resources

### Design Tools (Free)
- **Canva**: https://canva.com (easy, templates)
- **Figma**: https://figma.com (professional)
- **GIMP**: https://gimp.org (open source)

### Design Tools (Paid)
- **Adobe Photoshop**: Industry standard
- **Sketch**: Mac only
- **Affinity Designer**: One-time purchase

### Screenshot Tools
- **Android Studio**: Built-in screenshot tool
- **adb**: Command line tool
- **Vysor**: Screen mirroring + screenshots

### Templates
- Search "Play Store Feature Graphic Template"
- Look for 1024×500 templates
- Many free options on Figma Community

## Asset Storage

Store all assets in:
```
AndroidRelease/
├── graphics/
│   ├── feature-graphic.png (1024×500)
│   ├── screenshots/
│   │   ├── phone/
│   │   │   ├── 01-main-screen.png
│   │   │   ├── 02-gameplay.png
│   │   │   └── ...
│   │   └── tablet/
│   │       ├── 01-main-tablet.png
│   │       └── ...
│   └── promo/
│       └── video-link.txt
```

## Best Practices

1. **Consistency**: Use consistent styling across all assets
2. **Quality**: High resolution, crisp graphics
3. **Branding**: Maintain brand colors and style
4. **Clarity**: Clear, easy to understand visuals
5. **Testing**: Preview on different screen sizes
6. **Localization**: Create localized versions for key markets

## Common Mistakes to Avoid

❌ Using low-resolution images
❌ Including personal/test data in screenshots
❌ Blurry or pixelated graphics
❌ Wrong dimensions
❌ Transparency in feature graphic
❌ Text too small to read
❌ Device frames on screenshots (not needed)
❌ Inconsistent branding

## Verification

Before upload, verify:
- [ ] All images are correct dimensions
- [ ] File sizes under limits
- [ ] No personal information visible
- [ ] High quality, not blurry
- [ ] Proper format (PNG or JPG)
- [ ] Branding is consistent
