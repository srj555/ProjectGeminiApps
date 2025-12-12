# Quick Start Guide

Get up and running with ProjectDuoPlay in 5 minutes!

## Prerequisites

- JDK 17+
- Android Studio Koala or newer
- Xcode 15+ (for iOS)
- CocoaPods (for iOS)

## Quick Setup

### 1. Build the Project

```bash
# Navigate to project directory
cd ProjectDuoPlay

# Build all modules
./gradlew build
```

### 2. Run on Android

#### Option A: Android Studio
1. Open project in Android Studio
2. Wait for Gradle sync to complete
3. Select `androidApp` configuration
4. Choose `duoplay` build variant (Build Variants panel)
5. Click Run ▶️

#### Option B: Command Line
```bash
# Build and install DuoPlay flavor
./gradlew :androidApp:installDuoplayDebug

# Or use adb directly
./gradlew :androidApp:assembleDuoplayDebug
adb install -r androidApp/build/outputs/apk/duoplay/debug/*.apk
```

### 3. Run on iOS

```bash
# Install CocoaPods dependencies
cd iosApp
pod install

# Open workspace
open iosApp.xcworkspace
```

Then in Xcode:
1. Select a simulator or device
2. Click Run ▶️

## What You Get

### DuoPlay App
- Loads: `https://duoplay-589720827506.us-west1.run.app`
- Features enabled:
  - WebView with full web support
  - JavaScript bridge for native communication
  - Deep linking support
  - Push notifications ready
  - Network monitoring
  - Loading states and error handling

### App Structure
```
ProjectDuoPlay/
├── androidApp/       # Android app (DuoPlay flavor)
├── iosApp/          # iOS app
├── shared/          # Shared KMM code
├── core/            # Reusable core modules
└── feature/         # Feature modules (WebView)
```

## Testing the App

### 1. Test WebView Loading
The app should load the DuoPlay gaming website automatically.

### 2. Test Navigation
- Pull down to refresh
- Navigate within the website
- Use device back button (Android) - should go back in web history

### 3. Test Deep Links

**Android:**
```bash
adb shell am start -a android.intent.action.VIEW \
  -d "https://duoplay-589720827506.us-west1.run.app"
```

**iOS:**
Use Safari and navigate to the deep link URL.

### 4. Test JavaScript Bridge

Open browser console in the WebView and try:
```javascript
window.NativeBridge.postMessage('test', JSON.stringify({
    action: 'getDeviceInfo',
    data: {}
}));
```

### 5. Test Offline Behavior
- Enable airplane mode
- App should show offline error
- Disable airplane mode - app should recover

## Customizing for Your App

See the comprehensive guides:
- [README.md](README.md) - Full documentation
- [FLAVOR_GUIDE.md](FLAVOR_GUIDE.md) - Create new flavors
- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture details

### Quick Flavor Creation

#### Android
Edit `androidApp/build.gradle.kts`:
```kotlin
create("myapp") {
    dimension = "app"
    applicationId = "com.myapp.app"
    buildConfigField("String", "APP_NAME", "\"MyApp\"")
    buildConfigField("String", "BASE_URL", "\"https://myapp.com\"")
    resValue("string", "app_name", "MyApp")
}
```

#### iOS
Edit `shared/src/iosMain/kotlin/.../KoinIOS.kt`:
```kotlin
AppConfig(
    appName = "MyApp",
    baseUrl = "https://myapp.com",
    // ...
)
```

Build your flavor:
```bash
./gradlew :androidApp:assembleMyappDebug
```

## Common Commands

### Android
```bash
# List build variants
./gradlew :androidApp:tasks

# Clean build
./gradlew clean

# Build release
./gradlew :androidApp:bundleDuoplayRelease

# Run tests
./gradlew :shared:testDebugUnitTest
```

### iOS
```bash
# Update pods
cd iosApp && pod install

# Clean build
rm -rf ~/Library/Developer/Xcode/DerivedData

# Build from command line
xcodebuild -workspace iosApp.xcworkspace \
  -scheme iosApp \
  -configuration Debug
```

## Troubleshooting

### Build Failures

**Gradle sync failed:**
```bash
./gradlew clean
./gradlew --stop
rm -rf .gradle
./gradlew build
```

**CocoaPods issues:**
```bash
cd iosApp
pod deintegrate
pod install
```

### Runtime Issues

**WebView not loading:**
1. Check internet connection
2. Verify URL is accessible in browser
3. Check LogCat/Console for errors

**App crashes on startup:**
1. Check Koin initialization
2. Verify all dependencies are included
3. Check for ProGuard issues (release builds)

## Next Steps

1. **Customize the WebView URL** - Edit flavor configuration
2. **Add App Icons** - Replace default launcher icons
3. **Configure Analytics** - Set up Firebase
4. **Set up Push Notifications** - Add google-services.json
5. **Create Your Flavor** - Follow FLAVOR_GUIDE.md
6. **Deploy to Stores** - Build release versions

## Development Workflow

### 1. Make Changes
Edit files in `shared/`, `core/`, or `feature/` modules

### 2. Hot Reload (Compose)
Changes to Compose UI auto-reload in many cases

### 3. Rebuild
```bash
./gradlew :shared:build
```

### 4. Test on Both Platforms
Always test changes on both Android and iOS

### 5. Commit
```bash
git add .
git commit -m "Your changes"
```

## Resources

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Koin Documentation](https://insert-koin.io/)
- [WebView Best Practices](https://developer.android.com/develop/ui/views/layout/webapps/best-practices)

## Support

For issues:
1. Check existing documentation
2. Review error logs
3. Check dependencies are up to date
4. Contact development team

## License

Copyright © 2024. All rights reserved.

---

**Happy Coding!** 🚀
