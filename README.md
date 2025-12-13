# ProjectGeminiApps

A production-ready Kotlin Multiplatform Mobile (KMM) white-label WebView app framework with product flavors for rapid deployment of multiple branded applications. Built with Clean Architecture, modular design, and complete Play Store release infrastructure.

## Overview

This project provides a complete infrastructure for building and deploying multiple white-label Android apps from a single codebase. Each app can have its own branding, configuration, and backend URL while sharing the core functionality.

**Key Features:**
- Kotlin Multiplatform Mobile (KMM) with Compose Multiplatform
- WebView-based apps with native Android shell (iOS ready)
- Product flavors for easy white-labeling and multi-app support
- Complete production release infrastructure
- Automated build scripts and Play Store deployment guides
- Modular Clean Architecture

## Current Apps

### DuoPlay - Gaming Platform
- **Package ID**: `com.duoplay.app`
- **Backend**: https://duoplay-589720827506.us-west1.run.app
- **Status**: Production-ready with signed releases
- **Version**: 1.0.0

## Features

### Core Capabilities
- **Kotlin Multiplatform Mobile (KMM)** with Compose Multiplatform
- **Clean Architecture** with modularized layers (Core, Feature, Shared)
- **WebView Integration** with native Android WebView and iOS WKWebView
- **JavaScript Bridge** for bidirectional web-native communication
- **Deep Linking** support for both platforms
- **Push Notifications** infrastructure (Firebase/APNs)
- **Network Monitoring** with offline detection
- **Analytics** integration ready
- **Security**: SSL enforcement, safe browsing, domain whitelisting

### Production Infrastructure
- **Automated Release Scripts** for AAB and APK generation
- **Keystore Management** with secure signing configuration
- **Complete Documentation** for Play Store submission
- **Release Checklists** for deployment process
- **Graphics Asset Templates** and requirements
- **ProGuard/R8** optimization for release builds

## Project Structure

```
ProjectGeminiApps/
├── androidApp/                    # Android application module
│   ├── src/
│   │   ├── main/                  # Default source set
│   │   └── duoplay/               # DuoPlay flavor-specific resources
│   │       └── res/
│   │           ├── mipmap-*/      # App icons
│   │           └── values/        # Strings, colors, themes
│   └── build.gradle.kts           # App build configuration with flavors
│
├── shared/                        # Shared KMM code
│   └── src/
│       ├── commonMain/            # Platform-agnostic code
│       ├── androidMain/           # Android-specific code
│       └── iosMain/               # iOS-specific code (ready)
│
├── core/                          # Core modules
│   ├── common/                    # Common utilities, Result, Logger
│   ├── network/                   # Network monitoring
│   └── analytics/                 # Analytics abstraction
│
├── feature/                       # Feature modules
│   └── webview/                   # WebView functionality & JS bridge
│
├── AndroidRelease/                # Production release management
│   ├── README.md                  # Release infrastructure overview
│   ├── RELEASE_CHECKLIST.md      # Step-by-step release guide
│   ├── keystore/                  # App signing (gitignored)
│   │   ├── duoplay-release.keystore (not in git)
│   │   ├── CREDENTIALS.md        (not in git)
│   │   └── keystore.properties.template
│   ├── scripts/                   # Build automation
│   │   ├── quick-release.sh       # One-command AAB build
│   │   ├── build-release-aab.sh   # Interactive AAB builder
│   │   ├── build-release-apk.sh   # Interactive APK builder
│   │   └── generate-keystore.sh   # Keystore generator
│   ├── documentation/             # Guides and specifications
│   │   ├── PLAY_STORE_LISTING.md
│   │   ├── PLAY_STORE_SUBMISSION_GUIDE.md
│   │   └── GRAPHICS_ASSETS_REQUIREMENTS.md
│   └── builds/                    # Output directory (gitignored)
│
└── gradle/
    └── libs.versions.toml         # Version catalog
```

## Technologies

- **Language**: Kotlin
- **Architecture**: Kotlin Multiplatform Mobile (KMM)
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Koin
- **Build System**: Gradle with Kotlin DSL
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Java Version**: 17

## Getting Started

### Prerequisites

- **JDK 17** or higher
- **Android Studio Hedgehog (2023.1.1)** or newer
- **Android SDK** with API 35
- **Xcode 15+** (for iOS development)
- **CocoaPods** (for iOS dependencies)
- Git

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/srj555/ProjectGeminiApps.git
   cd ProjectGeminiApps
   ```

2. **Open in Android Studio:**
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. **Build the project:**
   ```bash
   ./gradlew build
   ```

4. **Run Android App (DuoPlay flavor):**
   ```bash
   ./gradlew :androidApp:installDuoplayDebug
   ```
   Or use Android Studio:
   - Select build variant: `duoplayDebug`
   - Click Run ▶️

5. **Run iOS App:**
   ```bash
   cd iosApp
   pod install
   open iosApp.xcworkspace
   ```

## Creating a New App Flavor

To create a new white-label app (e.g., "MyApp"):

### 1. Add Product Flavor

Edit `androidApp/build.gradle.kts`:

```kotlin
productFlavors {
    create("myapp") {
        dimension = "app"
        applicationId = "com.myapp.app"
        versionNameSuffix = "-myapp"

        buildConfigField("String", "APP_NAME", "\"MyApp\"")
        buildConfigField("String", "BASE_URL", "\"https://myapp.example.com\"")
        buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        buildConfigField("boolean", "ENABLE_PUSH_NOTIFICATIONS", "true")

        resValue("string", "app_name", "MyApp")
        resValue("string", "base_url", "https://myapp.example.com")
    }
}
```

### 2. Create Flavor Resources

Create directory: `androidApp/src/myapp/res/`

**Add app icon:**
- Create: `mipmap-mdpi/`, `mipmap-hdpi/`, `mipmap-xhdpi/`, `mipmap-xxhdpi/`, `mipmap-xxxhdpi/`
- Add: `ic_launcher.png` and `ic_launcher_round.png` in each

**Add colors/themes:**
Create `values/colors.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#FF6200EE</color>
    <color name="primary_dark">#FF3700B3</color>
    <color name="accent">#FF03DAC5</color>
</resources>
```

### 3. Update Deep Links (Optional)

In `androidApp/src/main/AndroidManifest.xml`, add flavor-specific deep link:
```xml
<data
    android:scheme="https"
    android:host="myapp.example.com" />
```

### 4. Build and Test

```bash
./gradlew :androidApp:assembleMyappDebug
./gradlew :androidApp:installMyappDebug
```

## Building for Production

### Quick Release Build (Recommended)

```bash
cd AndroidRelease
./scripts/quick-release.sh
```

This will:
1. Clean the project
2. Build signed AAB (Android App Bundle)
3. Copy to `AndroidRelease/builds/` with timestamp
4. Display verification command

**Output:** `AndroidRelease/builds/duoplay-v1.0.0-[timestamp].aab`

### Manual Build Commands

**Build AAB (for Play Store):**
```bash
./gradlew clean
./gradlew :androidApp:bundleDuoplayRelease
```

**Build APK (for testing):**
```bash
./gradlew :androidApp:assembleDuoplayRelease
```

**Or use the scripts:**
```bash
./AndroidRelease/scripts/build-release-aab.sh
./AndroidRelease/scripts/build-release-apk.sh
```

**Verify Signing:**
```bash
jarsigner -verify -verbose -certs AndroidRelease/builds/[file].aab
```

### iOS Release

1. Open `iosApp.xcworkspace` in Xcode
2. Select Product → Archive
3. Follow App Store submission process

## Release Management

Complete release infrastructure in `AndroidRelease/` directory.

### Documentation

- **README.md**: Overview and quick start
- **RELEASE_CHECKLIST.md**: Complete step-by-step release process
- **documentation/PLAY_STORE_LISTING.md**: Store content and metadata
- **documentation/PLAY_STORE_SUBMISSION_GUIDE.md**: Detailed submission guide
- **documentation/GRAPHICS_ASSETS_REQUIREMENTS.md**: Asset specifications

### Automated Scripts

All scripts in `AndroidRelease/scripts/`:

- **quick-release.sh**: One-command AAB build ⚡
- **build-release-aab.sh**: Interactive AAB builder
- **build-release-apk.sh**: Interactive APK builder
- **generate-keystore.sh**: Create new keystore for app signing

### Keystore Management

**Generate keystore for new app:**
```bash
cd AndroidRelease
./scripts/generate-keystore.sh
```

**Security Best Practices:**
- Keystore files are gitignored for security
- NEVER commit keystores or credentials to version control
- Backup keystore securely (you cannot update app without it)
- Store passwords in password manager
- Credentials documented in `AndroidRelease/keystore/CREDENTIALS.md` (not in git)

## Play Store Submission

Follow the complete checklist at `AndroidRelease/RELEASE_CHECKLIST.md`

### Quick Summary

1. ✅ Complete development and testing
2. 📦 Build signed AAB
3. 🎨 Create graphics (feature graphic, screenshots)
4. 🏪 Set up Google Play Console account ($25 one-time)
5. 📝 Configure store listing
6. ⭐ Complete content rating
7. 📤 Upload AAB and submit for review (1-3 days)
8. 🚀 Go live!

### Required Assets

- **Feature graphic**: 1024 × 500 px PNG/JPG
- **Screenshots**: Minimum 2 (1080 × 2340 px recommended)
- **Privacy policy**: Public URL required
- **App icon**: Automatically included from AAB

See `AndroidRelease/documentation/GRAPHICS_ASSETS_REQUIREMENTS.md` for complete specifications.

## Architecture

### Core Modules

**core:common**
- `Result.kt` - Result wrapper for operations
- `Logger.kt` - Platform-agnostic logging
- `AppConfig.kt` - Application configuration

**core:network**
- `NetworkMonitor.kt` - Network connectivity monitoring
- Platform-specific implementations

**core:analytics**
- `AnalyticsTracker.kt` - Analytics abstraction
- Platform-specific implementations

### Feature Modules

**feature:webview**
- `WebViewController.kt` - WebView abstraction
- `JavaScriptBridge.kt` - JS-Native bridge
- `WebViewViewModel.kt` - State management
- Platform-specific WebView implementations

## Configuration

### App Configuration

Each flavor can have its own configuration via `AppConfig`:

```kotlin
data class AppConfig(
    val appName: String,
    val appId: String,
    val baseUrl: String,
    val enableAnalytics: Boolean = true,
    val enablePushNotifications: Boolean = true,
    val enableDeepLinking: Boolean = true,
    val enableJavaScriptBridge: Boolean = true,
    val allowedDomains: List<String> = emptyList(),
    val customUserAgent: String? = null,
    val theme: AppTheme = AppTheme()
)
```

### JavaScript Bridge

Bidirectional communication between web and native:

**From Web to Native:**
```javascript
window.NativeBridge.postMessage('commandName', JSON.stringify({
    action: 'navigateTo',
    data: { url: 'https://example.com' }
}));
```

**From Native to Web:**
```kotlin
javaScriptBridge.callJavaScriptFunction("onNativeEvent", "data")
```

### Deep Linking

**Android:**
- Configure in `AndroidManifest.xml`
- Handled in `MainActivity.handleDeepLink()`

**iOS:**
- Configure in `Info.plist`
- URL schemes and Universal Links supported

## Security Features

- **SSL/TLS** enforced (no cleartext traffic)
- **Safe Browsing** enabled
- **Domain whitelisting** for WebView navigation
- **ProGuard/R8** optimization for release builds
- **JavaScript interface** security
- **Code minification** and resource shrinking

## Version Management

Update in `androidApp/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1      // Increment for each release
    versionName = "1.0.0" // Semantic versioning (MAJOR.MINOR.PATCH)
}
```

**Version Rules:**
- `versionCode`: Integer, must increment with each Play Store release
- `versionName`: Display version following semantic versioning

## Build Variants

### Debug vs Release

**Debug Build:**
- No code minification
- Debuggable
- Application ID suffix: `.debug`
- Fast build times
- Development environment

**Release Build:**
- R8 code minification enabled
- Resource shrinking enabled
- Signed with release keystore
- ProGuard rules applied
- Optimized for distribution
- Production environment

### Available Variants

- `duoplayDebug`
- `duoplayRelease`

Future flavors add more variants automatically.

## Push Notifications

### Android (Firebase Cloud Messaging)

1. Add `google-services.json` to `androidApp/`
2. Uncomment Firebase dependencies in `build.gradle.kts`
3. Apply google-services plugin
4. Implement in `PushNotificationService.kt`

### iOS (APNs)

1. Enable Push Notifications capability in Xcode
2. Configure in `Info.plist`
3. Implement in `PushNotificationManager.swift`

## Testing

```bash
# Run Android tests
./gradlew :shared:testDebugUnitTest

# Run iOS tests
cd iosApp
xcodebuild test -workspace iosApp.xcworkspace -scheme iosApp

# Run specific flavor tests
./gradlew :androidApp:testDuoplayDebugUnitTest
```

## Troubleshooting

### Build Issues

**"Keystore not found"**
- Ensure `keystore.properties` exists in project root
- Verify paths in `keystore.properties` are correct
- Run `AndroidRelease/scripts/generate-keystore.sh` if needed

**Gradle sync fails**
- Update Android Studio to latest version
- Invalidate caches: File → Invalidate Caches → Invalidate and Restart
- Check JDK version is 17+
- Run `./gradlew clean`

**App crashes on startup**
- Check WebView URL is accessible
- Verify internet permissions in AndroidManifest.xml
- Check logcat for specific errors
- Verify BASE_URL configuration

### iOS Issues

**CocoaPods issues**
```bash
cd iosApp
pod deintegrate
pod install
```

**WebView not loading**
- Check network permissions
- Verify URL is accessible
- Check domain whitelist configuration
- Review App Transport Security settings

## Dependencies

Key libraries:
- **Jetpack Compose**: Modern UI toolkit
- **Koin**: Dependency injection
- **Kotlin Coroutines**: Asynchronous programming
- **AndroidX Libraries**: Core Android components

See `gradle/libs.versions.toml` for complete dependency list.

## Security

### Protected (gitignored)

- `*.keystore`, `*.jks` - Signing keystores
- `keystore.properties` - Keystore credentials
- `AndroidRelease/keystore/CREDENTIALS.md` - Passwords
- `AndroidRelease/builds/` - Built APK/AAB files
- `.claude/` - Local development settings
- `Release/` - Old release folders

### Never Commit

- API keys or secrets
- Keystore files or passwords
- Personal data or credentials
- Large binary files
- `google-services.json` (Firebase config)

## Contributing

This is a private project framework. For team members:

1. Create a feature branch
2. Make your changes
3. Test thoroughly (debug and release builds)
4. Create pull request
5. Code review required before merge

## Support

- **Documentation**: `AndroidRelease/documentation/`
- **Issues**: https://github.com/srj555/ProjectGeminiApps/issues
- **Play Console**: https://play.google.com/console
- **Developer Support**: https://support.google.com/googleplay/android-developer

## License

Proprietary - All rights reserved

## Changelog

### Version 1.0.0 (December 2024)
- ✅ KMM WebView framework with Clean Architecture
- ✅ DuoPlay app implementation
- ✅ Product flavor architecture for white-labeling
- ✅ Complete release infrastructure and automation
- ✅ Play Store deployment documentation and guides
- ✅ Automated build scripts (AAB/APK)
- ✅ Keystore management and signing configuration
- ✅ Security best practices and ProGuard rules

---

**Built with ❤️ using Kotlin Multiplatform Mobile**

🚀 Ready to deploy multiple apps from a single codebase!
