# ProjectDuoPlay - KMM WebView Framework

A production-ready Kotlin Multiplatform Mobile (KMM) framework for building WebView-based applications with support for Android and iOS. Built with Clean Architecture, modular design, and product flavor support for easy white-labeling.

## Features

- **Kotlin Multiplatform Mobile (KMM)** with Compose Multiplatform
- **Clean Architecture** with modularized layers (Core, Feature, Shared)
- **Product Flavors** for easy white-labeling and multi-app support
- **WebView Integration** with native Android WebView and iOS WKWebView
- **JavaScript Bridge** for bidirectional web-native communication
- **Deep Linking** support for both platforms
- **Push Notifications** infrastructure (Firebase/APNs)
- **Network Monitoring** with offline detection
- **Analytics** integration ready
- **Production Features**: error handling, loading states, security (SSL, safe browsing)

## Project Structure

```
ProjectDuoPlay/
├── androidApp/                    # Android application
│   ├── src/
│   │   ├── main/                  # Main source set
│   │   └── duoplay/               # DuoPlay flavor resources
│   └── build.gradle.kts
├── iosApp/                        # iOS application
│   └── iosApp/
│       ├── iOSApp.swift
│       └── Info.plist
├── shared/                        # Shared KMM module
│   ├── src/
│   │   ├── commonMain/            # Common Kotlin code
│   │   ├── androidMain/           # Android-specific code
│   │   └── iosMain/               # iOS-specific code
│   └── build.gradle.kts
├── core/                          # Core reusable modules
│   ├── common/                    # Common utilities
│   ├── network/                   # Network utilities
│   └── analytics/                 # Analytics abstraction
├── feature/                       # Feature modules
│   └── webview/                   # WebView feature
└── gradle/
    └── libs.versions.toml         # Version catalog
```

## Getting Started

### Prerequisites

- **JDK 17** or higher
- **Android Studio Koala** or newer
- **Xcode 15** or higher (for iOS development)
- **CocoaPods** (for iOS dependencies)

### Setup

1. **Clone the repository**
   ```bash
   cd ProjectDuoPlay
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run Android App (DuoPlay flavor)**
   ```bash
   ./gradlew :androidApp:installDuoplayDebug
   ```

4. **Run iOS App**
   ```bash
   cd iosApp
   pod install
   open iosApp.xcworkspace
   ```

## Creating a New Flavor/App

To create a new white-labeled app (e.g., "MyApp"), follow these steps:

### Android

1. **Edit `androidApp/build.gradle.kts`**

   Add a new product flavor:
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

2. **Create flavor-specific resources**

   Create directory: `androidApp/src/myapp/res/values/strings.xml`
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="app_name">MyApp</string>
   </resources>
   ```

3. **Update AndroidManifest.xml deep links**

   In `androidApp/src/main/AndroidManifest.xml`, add flavor-specific deep link filters:
   ```xml
   <data
       android:scheme="https"
       android:host="myapp.example.com" />
   ```

4. **Build the flavor**
   ```bash
   ./gradlew :androidApp:assembleMyappRelease
   ```

### iOS

For iOS, the configuration is centralized in the shared module:

1. **Update `shared/src/iosMain/kotlin/com/projectduoplay/shared/KoinIOS.kt`**

   Modify the `iosAppModule()` function to support multiple configurations:
   ```kotlin
   private fun iosAppModule() = module {
       single<AppConfigProvider> {
           object : AppConfigProvider {
               override fun getConfig(): AppConfig {
                   // Read from configuration or Info.plist
                   return AppConfig(
                       appName = "MyApp",
                       appId = "com.myapp.app",
                       baseUrl = "https://myapp.example.com",
                       // ... other config
                   )
               }
           }
       }
   }
   ```

2. **Update `iosApp/iosApp/Info.plist`**

   Update bundle identifier, app name, and deep link configuration:
   ```xml
   <key>CFBundleDisplayName</key>
   <string>MyApp</string>
   <key>CFBundleURLSchemes</key>
   <array>
       <string>myapp</string>
   </array>
   ```

## Architecture

### Core Modules

#### core:common
- `Result.kt` - Result wrapper for operations
- `Logger.kt` - Platform-agnostic logging
- `AppConfig.kt` - Application configuration

#### core:network
- `NetworkMonitor.kt` - Network connectivity monitoring
- Platform-specific implementations for Android and iOS

#### core:analytics
- `AnalyticsTracker.kt` - Analytics abstraction
- Platform-specific implementations

### Feature Modules

#### feature:webview
- `WebViewController.kt` - WebView abstraction
- `JavaScriptBridge.kt` - JS-Native bridge
- `WebViewViewModel.kt` - WebView state management
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

The JavaScript bridge allows bidirectional communication:

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

## Security Features

- **SSL/TLS** enforced (no cleartext traffic)
- **Safe Browsing** enabled
- **Domain whitelisting** for WebView navigation
- **ProGuard** rules for Android release builds
- **JavaScript interface** security

## Building for Production

### Android

```bash
# Build release APK for specific flavor
./gradlew :androidApp:assembleDuoplayRelease

# Build release AAB for Google Play
./gradlew :androidApp:bundleDuoplayRelease
```

### iOS

1. Open `iosApp.xcworkspace` in Xcode
2. Select Product > Archive
3. Follow App Store submission process

## Testing

```bash
# Run Android tests
./gradlew :shared:testDebugUnitTest

# Run iOS tests
cd iosApp
xcodebuild test -workspace iosApp.xcworkspace -scheme iosApp
```

## Troubleshooting

### Common Issues

1. **CocoaPods issues**
   ```bash
   cd iosApp
   pod deintegrate
   pod install
   ```

2. **Gradle sync issues**
   ```bash
   ./gradlew clean
   ./gradlew --stop
   ```

3. **WebView not loading**
   - Check network permissions
   - Verify URL is accessible
   - Check domain whitelist configuration

## License

Copyright © 2024. All rights reserved.

## Contributing

This is a proprietary project template. Customize as needed for your applications.

## Support

For issues and questions, please contact the development team.
