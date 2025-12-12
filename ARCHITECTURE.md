# Architecture Documentation

## Overview

ProjectDuoPlay follows Clean Architecture principles with a modularized structure designed for scalability, testability, and reusability.

## Architecture Layers

### 1. Presentation Layer (UI)
- **Location**: `shared/src/commonMain`, `androidApp`, `iosApp`
- **Responsibility**: UI components, ViewModels, user interaction
- **Technologies**: Compose Multiplatform, SwiftUI interop

### 2. Domain Layer
- **Location**: `feature/*`
- **Responsibility**: Business logic, use cases, domain models
- **Technologies**: Pure Kotlin, platform-agnostic

### 3. Data Layer
- **Location**: `core/network`, `core/analytics`
- **Responsibility**: Data sources, repositories, external APIs
- **Technologies**: Ktor, platform-specific implementations

### 4. Core Layer
- **Location**: `core/common`
- **Responsibility**: Shared utilities, base classes, configuration
- **Technologies**: Pure Kotlin with expect/actual

## Module Structure

```
┌─────────────────────────────────────────────────┐
│              androidApp / iosApp                │
│           (Platform Applications)               │
└────────────┬────────────────────────────────────┘
             │ depends on
             ▼
┌─────────────────────────────────────────────────┐
│                   shared                        │
│         (Shared KMM Module)                     │
│  • DI Configuration                             │
│  • App Composition                              │
└────────────┬────────────────────────────────────┘
             │ depends on
             ▼
┌─────────────────────────────────────────────────┐
│              feature:webview                    │
│         (WebView Feature Module)                │
│  • WebViewController                            │
│  • JavaScriptBridge                             │
│  • WebViewViewModel                             │
└────────────┬────────────────────────────────────┘
             │ depends on
             ▼
┌─────────────────────────────────────────────────┐
│          core:common, core:network,             │
│             core:analytics                      │
│         (Core Modules)                          │
│  • Common utilities                             │
│  • Network monitoring                           │
│  • Analytics abstraction                        │
└─────────────────────────────────────────────────┘
```

## Design Patterns

### 1. MVVM (Model-View-ViewModel)
- **ViewModel**: `WebViewViewModel` manages UI state
- **View**: Compose functions for UI rendering
- **Model**: Data classes and domain models

### 2. Repository Pattern
- Network Monitor provides abstracted data access
- Platform-specific implementations

### 3. Dependency Injection (Koin)
- Centralized DI configuration in `shared/di/`
- Platform-specific modules
- ViewModel injection

### 4. Observer Pattern
- StateFlow for reactive state management
- Collect state in Composables
- Unidirectional data flow

### 5. Strategy Pattern
- Platform-specific implementations via expect/actual
- `PlatformWebViewController` for WebView
- `PlatformNetworkMonitor` for connectivity

## Data Flow

```
User Action
    │
    ▼
┌──────────────┐
│   UI Layer   │ (Composable)
└──────┬───────┘
       │ Event
       ▼
┌──────────────┐
│  ViewModel   │ (WebViewViewModel)
└──────┬───────┘
       │ Business Logic
       ▼
┌──────────────┐
│  WebView     │ (PlatformWebViewController)
│  Controller  │
└──────┬───────┘
       │ Platform API
       ▼
┌──────────────┐
│Native WebView│ (Android WebView / WKWebView)
└──────────────┘
       │
       │ State Update
       ▼
   StateFlow
       │
       │ Collect
       ▼
┌──────────────┐
│   UI Layer   │ (Re-composition)
└──────────────┘
```

## Key Components

### WebViewController
**Purpose**: Abstraction layer for WebView operations

**Responsibilities**:
- Load URLs
- Navigation (back, forward, reload)
- JavaScript execution
- State management

**Platform Implementations**:
- Android: `PlatformWebViewController` (WebView)
- iOS: `PlatformWebViewController` (WKWebView)

### JavaScriptBridge
**Purpose**: Bidirectional web-native communication

**Features**:
- Message handling from web
- JavaScript function calling from native
- Command pattern for actions
- Serialization with kotlinx.serialization

### NetworkMonitor
**Purpose**: Network connectivity detection

**Implementations**:
- Android: ConnectivityManager
- iOS: NWPathMonitor

### AppConfigProvider
**Purpose**: Centralized app configuration

**Sources**:
- Android: BuildConfig from product flavors
- iOS: Info.plist or hardcoded config

## State Management

### UI State
```kotlin
sealed class WebViewUiState {
    object Idle : WebViewUiState()
    object Loading : WebViewUiState()
    data class Success(val url: String) : WebViewUiState()
    data class Error(val message: String, val isNetworkError: Boolean) : WebViewUiState()
}
```

### Loading State
```kotlin
sealed class WebViewLoadingState {
    object Idle : WebViewLoadingState()
    data class Loading(val progress: Int) : WebViewLoadingState()
    data class Loaded(val url: String) : WebViewLoadingState()
    data class Error(...) : WebViewLoadingState()
}
```

### State Flows
- `loadingState: StateFlow<WebViewLoadingState>`
- `currentUrl: StateFlow<String?>`
- `canGoBack: StateFlow<Boolean>`
- `canGoForward: StateFlow<Boolean>`

## Threading Model

### Coroutines
- **Main**: UI updates, ViewModel operations
- **IO**: Network operations (via Ktor)
- **Default**: CPU-intensive operations

### Platform-Specific
- **Android**: Main thread for WebView operations
- **iOS**: Main queue for WKWebView operations

## Error Handling

### Layers of Error Handling

1. **Network Layer**
   - Connection errors
   - Timeout handling
   - Offline detection

2. **WebView Layer**
   - Page load errors
   - SSL errors
   - Resource errors

3. **ViewModel Layer**
   - Business logic errors
   - State transformation

4. **UI Layer**
   - Error display
   - User feedback
   - Retry mechanisms

### Result Wrapper
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

## Security Architecture

### WebView Security
- JavaScript enabled with restrictions
- Domain whitelisting
- No file access
- SSL enforcement
- Mixed content blocking

### Network Security
- HTTPS only
- Certificate pinning (optional)
- No cleartext traffic

### Data Security
- No sensitive data in BuildConfig (use secure storage)
- ProGuard/R8 obfuscation for Android
- Keychain for iOS secrets

## Testing Strategy

### Unit Tests
- ViewModels
- Business logic
- Data transformations

### Integration Tests
- Repository layer
- Network calls
- DI configuration

### UI Tests
- Compose UI testing
- Platform-specific UI tests

## Performance Considerations

### WebView Optimization
- Hardware acceleration enabled
- Cache management
- Memory leak prevention (dispose properly)

### State Management
- Minimize recompositions
- Remember expensive operations
- LaunchedEffect for side effects

### Memory Management
- Proper lifecycle handling
- Clear WebView on destroy
- Cancel coroutines in onCleared()

## Scalability

### Adding Features
1. Create new feature module
2. Define interface in common
3. Implement platform-specific code
4. Add to DI module
5. Integrate in shared module

### Supporting New Platforms
1. Add target in `build.gradle.kts`
2. Implement expect/actual classes
3. Create platform module
4. Configure native app

## Future Enhancements

### Potential Additions
- Offline caching with Service Workers
- Biometric authentication
- File upload/download support
- PDF viewing
- Camera/photo library access
- Share sheet integration
- App shortcuts

### Architectural Improvements
- Multi-module navigation
- Feature flags system
- A/B testing framework
- Crash reporting
- Performance monitoring

## Dependencies

### Core Dependencies
- Kotlin 2.0.21
- Compose Multiplatform
- Koin (DI)
- Kotlinx.coroutines
- Kotlinx.serialization

### Platform Dependencies
- Android: AndroidX, Lifecycle
- iOS: CocoaPods

### Network
- Ktor client

## Build Configuration

### Gradle Modules
- Kotlin Multiplatform
- Android Application/Library
- Compose Compiler
- Kotlin Serialization

### Version Catalog
Centralized dependency management in `gradle/libs.versions.toml`

## Conclusion

This architecture provides:
- **Separation of Concerns**: Clear layer boundaries
- **Testability**: Mockable interfaces, DI
- **Reusability**: Shared code, modular design
- **Maintainability**: Clear structure, documentation
- **Scalability**: Easy to add features/platforms
- **Flexibility**: Product flavors for white-labeling
