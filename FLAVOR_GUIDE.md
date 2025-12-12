# Product Flavor Creation Guide

This guide walks you through creating a new white-labeled app from the ProjectDuoPlay framework.

## Overview

The framework is designed to support multiple white-labeled apps (flavors) from a single codebase. Each flavor can have:
- Unique app name and branding
- Different WebView URL
- Custom configuration
- Separate app IDs for app stores

## Step-by-Step Guide

### 1. Android Flavor Setup

#### 1.1 Add Product Flavor

Edit `androidApp/build.gradle.kts` and add your flavor in the `productFlavors` block:

```kotlin
create("yourapp") {
    dimension = "app"
    applicationId = "com.yourcompany.yourapp"
    versionNameSuffix = "-yourapp"

    // Build config fields
    buildConfigField("String", "APP_NAME", "\"YourApp\"")
    buildConfigField("String", "BASE_URL", "\"https://yourapp.example.com\"")
    buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
    buildConfigField("boolean", "ENABLE_PUSH_NOTIFICATIONS", "true")

    // Resource values
    resValue("string", "app_name", "YourApp")
    resValue("string", "base_url", "https://yourapp.example.com")
}
```

#### 1.2 Create Flavor Resources

Create the following directory structure:
```
androidApp/src/yourapp/res/
└── values/
    └── strings.xml
```

`androidApp/src/yourapp/res/values/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">YourApp</string>
</resources>
```

#### 1.3 Add App Icons (Optional)

Place app icons in flavor-specific directories:
```
androidApp/src/yourapp/res/
├── mipmap-hdpi/
├── mipmap-mdpi/
├── mipmap-xhdpi/
├── mipmap-xxhdpi/
└── mipmap-xxxhdpi/
```

#### 1.4 Configure Deep Links

Update `androidApp/src/main/AndroidManifest.xml` to add flavor-specific deep links:

```xml
<!-- In the MainActivity intent-filter -->
<data
    android:scheme="https"
    android:host="yourapp.example.com" />
```

Or create a flavor-specific manifest:
`androidApp/src/yourapp/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <activity android:name=".MainActivity">
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:scheme="https"
                    android:host="yourapp.example.com" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 2. iOS Configuration

#### 2.1 Update Shared Module

Edit `shared/src/iosMain/kotlin/com/projectduoplay/shared/KoinIOS.kt`:

```kotlin
private fun iosAppModule() = module {
    single<AppConfigProvider> {
        object : AppConfigProvider {
            override fun getConfig(): AppConfig {
                return AppConfig(
                    appName = "YourApp",
                    appId = "com.yourcompany.yourapp",
                    baseUrl = "https://yourapp.example.com",
                    enableAnalytics = true,
                    enablePushNotifications = true,
                    enableDeepLinking = true,
                    enableJavaScriptBridge = true,
                    allowedDomains = listOf("yourapp.example.com"),
                    customUserAgent = "YourApp/1.0.0",
                    theme = AppTheme(
                        primaryColor = "#YOUR_COLOR",
                        splashScreenBackground = "#FFFFFF",
                        statusBarColor = "#YOUR_COLOR"
                    )
                )
            }
        }
    }
}
```

#### 2.2 Update Info.plist

Edit `iosApp/iosApp/Info.plist`:

```xml
<key>CFBundleDisplayName</key>
<string>YourApp</string>

<key>CFBundleIdentifier</key>
<string>com.yourcompany.yourapp</string>

<!-- Deep Link Support -->
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLName</key>
        <string>com.yourcompany.yourapp</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>yourapp</string>
        </array>
    </dict>
</array>

<!-- Universal Links -->
<key>com.apple.developer.associated-domains</key>
<array>
    <string>applinks:yourapp.example.com</string>
</array>
```

#### 2.3 Update Xcode Project

1. Open `iosApp.xcworkspace` in Xcode
2. Select the project in Project Navigator
3. Update:
   - **Bundle Identifier**: `com.yourcompany.yourapp`
   - **Display Name**: `YourApp`
   - **Version**: `1.0`
   - **Build**: `1`

### 3. Build and Test

#### Android

```bash
# Debug build
./gradlew :androidApp:assembleYourappDebug

# Release build
./gradlew :androidApp:assembleYourappRelease

# Install on device
./gradlew :androidApp:installYourappDebug
```

#### iOS

1. Open `iosApp.xcworkspace`
2. Select your scheme
3. Build and run (Cmd + R)

### 4. Customization Options

#### Theme Customization

Update `AppTheme` in your config:

```kotlin
AppTheme(
    primaryColor = "#6200EE",          // Primary brand color
    splashScreenBackground = "#FFFFFF", // Splash screen background
    statusBarColor = "#6200EE"         // Status bar color
)
```

#### Feature Toggles

Control features per flavor:

```kotlin
AppConfig(
    enableAnalytics = true,              // Firebase Analytics
    enablePushNotifications = true,      // Push notifications
    enableDeepLinking = true,            // Deep link support
    enableJavaScriptBridge = true,       // JS-Native bridge
    allowedDomains = listOf("..."),      // Restrict navigation
)
```

#### WebView Settings

Customize WebView behavior in `WebViewConfig`:

```kotlin
WebViewConfig(
    initialUrl = "https://yourapp.example.com",
    allowedDomains = listOf("yourapp.example.com"),
    enableJavaScript = true,
    enableDomStorage = true,
    enableCache = true,
    enableZoom = false,
    userAgentSuffix = "YourApp/1.0.0"
)
```

### 5. Advanced: Multi-Environment Support

For staging/production environments, extend flavors:

```kotlin
productFlavors {
    create("yourappStaging") {
        dimension = "app"
        applicationId = "com.yourcompany.yourapp.staging"
        buildConfigField("String", "BASE_URL", "\"https://staging.yourapp.example.com\"")
        resValue("string", "app_name", "YourApp Staging")
    }

    create("yourappProduction") {
        dimension = "app"
        applicationId = "com.yourcompany.yourapp"
        buildConfigField("String", "BASE_URL", "\"https://yourapp.example.com\"")
        resValue("string", "app_name", "YourApp")
    }
}
```

### 6. Checklist

Before releasing a new flavor:

- [ ] Unique `applicationId`/Bundle ID
- [ ] App name configured
- [ ] Base URL set correctly
- [ ] App icons added
- [ ] Deep links configured
- [ ] Push notification certificates (iOS)
- [ ] google-services.json (Android, if using Firebase)
- [ ] GoogleService-Info.plist (iOS, if using Firebase)
- [ ] Domain whitelisting configured
- [ ] Analytics tracking ID
- [ ] Test deep linking
- [ ] Test push notifications
- [ ] Test JavaScript bridge
- [ ] Production signing keys configured

## Maintenance

### Adding a Feature to All Flavors

Since core functionality is shared, adding features to the shared/core modules automatically applies to all flavors.

### Flavor-Specific Features

To add flavor-specific code:

1. Create flavor source sets:
   ```
   androidApp/src/yourapp/kotlin/...
   ```

2. Add flavor-specific implementations

### Removing a Flavor

1. Remove from `productFlavors` block in `build.gradle.kts`
2. Delete flavor resource directories
3. Sync Gradle

## Best Practices

1. **Consistent Naming**: Use lowercase for flavor names in Gradle
2. **Resource Separation**: Keep flavor-specific resources in flavor directories
3. **Testing**: Test each flavor independently before release
4. **Version Control**: Use branches or tags for flavor-specific releases
5. **Documentation**: Document flavor-specific configurations

## Troubleshooting

### Flavor not appearing in Build Variants

- Sync Gradle
- Invalidate caches and restart Android Studio

### Build conflicts between flavors

- Ensure unique `applicationId` for each flavor
- Check for conflicting resources

### Deep links not working

- Verify `AndroidManifest.xml` configuration
- Test with `adb shell am start -a android.intent.action.VIEW -d "https://yourapp.example.com"`
- Check domain verification

## Examples

See the `duoplay` flavor as a reference implementation:
- `androidApp/build.gradle.kts` - Flavor configuration
- `androidApp/src/duoplay/` - Flavor resources
- `shared/src/iosMain/kotlin/com/projectduoplay/shared/KoinIOS.kt` - iOS config
