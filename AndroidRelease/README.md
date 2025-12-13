# 📱 Android Release Package

Complete release management package for DuoPlay Android app.

## 📁 Folder Structure

```
AndroidRelease/
├── README.md                          ← You are here
├── RELEASE_CHECKLIST.md              ← Step-by-step release guide
│
├── keystore/                         ← App signing
│   ├── duoplay-release.keystore      ← Keystore file (⚠️ Keep secure!)
│   ├── CREDENTIALS.md                ← Passwords and details
│   └── keystore.properties.template  ← Template for new keystores
│
├── builds/                           ← Output directory
│   └── (AAB and APK files saved here)
│
├── scripts/                          ← Build automation
│   ├── quick-release.sh              ← One-command release build
│   ├── build-release-aab.sh          ← Build AAB for Play Store
│   ├── build-release-apk.sh          ← Build APK for testing
│   └── generate-keystore.sh          ← Generate new keystore
│
├── graphics/                         ← Store assets
│   ├── feature-graphic.png           ← 1024×500 (create this)
│   └── screenshots/
│       ├── phone/                    ← Phone screenshots (create these)
│       └── tablet/                   ← Tablet screenshots (optional)
│
└── documentation/                    ← Complete guides
    ├── PLAY_STORE_LISTING.md         ← Store text and metadata
    ├── PLAY_STORE_SUBMISSION_GUIDE.md ← Detailed submission guide
    └── GRAPHICS_ASSETS_REQUIREMENTS.md ← Asset specifications
```

## 🚀 Quick Start

### First Time Setup

1. **Verify Keystore** (already created):
   ```bash
   ls -la keystore/duoplay-release.keystore
   cat keystore/CREDENTIALS.md
   ```

2. **Create Graphics Assets**:
   - Feature graphic: 1024 × 500 px
   - Screenshots: Minimum 2 phone screenshots
   - See `documentation/GRAPHICS_ASSETS_REQUIREMENTS.md`

3. **Build Release**:
   ```bash
   ./scripts/quick-release.sh
   ```

4. **Follow Checklist**:
   - Open `RELEASE_CHECKLIST.md`
   - Check off each step
   - Submit to Play Store

## 🔨 Building Releases

### Quick Release (Recommended)
```bash
cd AndroidRelease
./scripts/quick-release.sh
```
Output: `builds/duoplay-v1.0.0-[timestamp].aab`

### Build AAB (Play Store)
```bash
./scripts/build-release-aab.sh
```

### Build APK (Testing)
```bash
./scripts/build-release-apk.sh
```

### Manual Build
```bash
# From project root
./gradlew clean
./gradlew :androidApp:bundleDuoplayRelease

# Output:
# androidApp/build/outputs/bundle/duoplayRelease/
```

## 📋 Essential Files

### 1. Keystore (CRITICAL)
- **File**: `keystore/duoplay-release.keystore`
- **Purpose**: Signs your app
- **⚠️ NEVER LOSE THIS**: Cannot update app without it
- **Backup**: Store securely (cloud, password manager)
- **Credentials**: See `keystore/CREDENTIALS.md`

### 2. Release Checklist
- **File**: `RELEASE_CHECKLIST.md`
- **Use**: Step-by-step release process
- **Start here**: First time releasing

### 3. Store Listing
- **File**: `documentation/PLAY_STORE_LISTING.md`
- **Use**: Copy-paste content for Play Console
- **Contains**: Descriptions, release notes, metadata

## 🎨 Graphics Assets

### Required (Must Create)
1. **Feature Graphic**: 1024 × 500 px
   - Location: `graphics/feature-graphic.png`
   - Tools: Canva, Figma, Photoshop
   - Template: Black background, white text

2. **Screenshots**: Minimum 2
   - Location: `graphics/screenshots/phone/`
   - Size: 1080 × 2340 px (recommended)
   - Capture from real device or emulator

### Already Have
- ✅ App icon (in project: `androidApp/src/duoplay/res/mipmap-*/`)

See `documentation/GRAPHICS_ASSETS_REQUIREMENTS.md` for complete specs.

## 📖 Documentation

| File | Purpose |
|------|---------|
| `RELEASE_CHECKLIST.md` | Complete step-by-step release guide |
| `documentation/PLAY_STORE_LISTING.md` | Store text, descriptions, metadata |
| `documentation/PLAY_STORE_SUBMISSION_GUIDE.md` | Detailed Play Console guide |
| `documentation/GRAPHICS_ASSETS_REQUIREMENTS.md` | Asset specifications |

## 🔐 Security

### Keystore Security
- ⚠️ **NEVER commit keystore to git** (.gitignore configured)
- ⚠️ **NEVER commit keystore.properties** (.gitignore configured)
- ✅ **DO backup keystore** to secure location
- ✅ **DO use password manager** for credentials

### What's Safe to Commit
- ✅ Scripts in `scripts/`
- ✅ Documentation in `documentation/`
- ✅ This README
- ✅ Template files
- ❌ Keystore files
- ❌ keystore.properties
- ❌ Built APK/AAB files (too large)

## 📊 Version Management

Update version before each release in `androidApp/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1      // Increment each release
    versionName = "1.0.0" // Semantic versioning
}
```

## 🧪 Testing Release Build

### Install AAB (requires bundletool)
```bash
# Download bundletool from:
# https://github.com/google/bundletool/releases

# Generate APKs from AAB
bundletool build-apks \
  --bundle=builds/duoplay-v1.0.0-*.aab \
  --output=test.apks \
  --mode=universal

# Install
bundletool install-apks --apks=test.apks
```

### Install APK (simpler)
```bash
# Build APK
./scripts/build-release-apk.sh

# Install
adb install builds/duoplay-v1.0.0-*.apk
```

### Verify Signing
```bash
jarsigner -verify -verbose -certs builds/duoplay-v1.0.0-*.aab
```

## 📤 Upload to Play Store

1. **Build AAB**: `./scripts/quick-release.sh`
2. **Go to Play Console**: https://play.google.com/console
3. **Navigate**: Production → Create new release
4. **Upload**: File from `builds/` folder
5. **Fill**: Release notes, review, rollout
6. **Submit**: Wait 1-3 days for review

See `RELEASE_CHECKLIST.md` for complete process.

## 🛠 Troubleshooting

### "Keystore not found"
```bash
# Check keystore exists
ls -la keystore/duoplay-release.keystore

# Check keystore.properties
cat ../keystore.properties
```

### "Build fails with signing error"
1. Verify `keystore.properties` exists in project root
2. Check passwords are correct
3. Verify keystore file path

### "AAB/APK not generated"
1. Check build output for errors
2. Run `./gradlew clean` first
3. Check disk space
4. Verify Android SDK is properly configured

### "Screenshots wrong size"
Use image editor to resize:
- Recommended: 1080 × 2340 px
- Format: PNG or JPG
- No transparency

## 🔄 Release Process Summary

```
1. ✅ Setup Complete (keystore, scripts, docs)
   ↓
2. Create Graphics (feature graphic, screenshots)
   ↓
3. Build Release (./scripts/quick-release.sh)
   ↓
4. Test Build (install and test on device)
   ↓
5. Play Console Setup (create app, configure)
   ↓
6. Upload AAB (from builds/ folder)
   ↓
7. Submit for Review (1-3 days)
   ↓
8. ✅ Live on Play Store!
```

## 📞 Support & Resources

- **Play Console**: https://play.google.com/console
- **Developer Support**: https://support.google.com/googleplay/android-developer
- **Policy Center**: https://play.google.com/about/developer-content-policy/
- **Keystore Docs**: https://developer.android.com/studio/publish/app-signing

## ✅ Pre-Release Checklist (Quick)

Before submitting, ensure you have:

- [ ] Keystore backed up securely
- [ ] Feature graphic created (1024×500)
- [ ] Screenshots captured (minimum 2)
- [ ] AAB built and tested
- [ ] Privacy policy URL ready
- [ ] Play Console account created
- [ ] Store listing text prepared
- [ ] `RELEASE_CHECKLIST.md` reviewed

## 🎯 Next Steps

1. **If first time releasing**:
   - Read `RELEASE_CHECKLIST.md` completely
   - Create graphics assets
   - Set up Play Console account

2. **If ready to build**:
   ```bash
   ./scripts/quick-release.sh
   ```

3. **If ready to submit**:
   - Follow `RELEASE_CHECKLIST.md`
   - Upload to Play Console

---

**Need help?** Review the documentation files in `documentation/`

**Ready to release?** Start with `RELEASE_CHECKLIST.md`

**Quick build?** Run `./scripts/quick-release.sh`

🚀 **Good luck with your release!**
