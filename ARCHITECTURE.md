# Architecture

## Configuration pipeline

`profiles/*.toml` and their referenced icon PNGs are the only app-specific inputs. `tools/wrapper.py` validates the complete profile set before writing anything, then generates profile-scoped Android properties/resources and iOS xcconfig/icon sets. Writes use an advisory lock and atomic replacement.

Generated paths are ignored and disposable:

```text
config/generated/profiles/<id>/...
iosApp/Config/Generated/<id>.xcconfig
iosApp/iosApp/Assets.xcassets/<scheme>AppIcon.appiconset/...
iosApp/project.yml
iosApp/ProjectGeminiApps.xcodeproj
iosApp/ProjectGeminiApps.xcworkspace
```

Android selects exactly one profile with `-PappProfile=<id>` and isolates build output under `.build/android/<id>`. iOS generation emits one target/scheme per profile and isolates derived data under `.build/ios/<id>`. Packaged artifacts move to `dist/<id>/...` only after metadata inspection passes.

## Runtime modules

```text
androidApp / generated SwiftUI host
                 |
               shared
                 |
          feature:webview
          /      |       \
 core:common core:network core:analytics
```

- `core:common` defines the typed `AppConfig` contract.
- Android implements it from generated `BuildConfig` fields.
- iOS implements it from generated `Info.plist` substitutions.
- `shared` composes DI, safe-area layout, splash, and WebView feature.
- `feature:webview` owns shared state/policy and Android WebView/iOS WKWebView implementations.
- `core:network` exposes platform network status.
- `core:analytics` is currently a logging abstraction, not a production analytics SDK.

## Navigation policy

The shared policy parses a target as a URL and compares its canonical origin to an exact allowlist. It does not use substring or suffix checks.

```text
exact HTTPS allowlist origin -> load in WebView
other HTTP(S)/mailto/tel     -> operating-system external handler
unsafe/malformed scheme      -> block
```

Programmatic loads and deep links use the same allow decision. Android system back consumes WebView history before leaving the activity. The controller is stable across recomposition and platform views are disposed with their composition.

## Security boundary

The wrapped website remains remote, mutable application code. Mobile packaging does not make that code trusted. The shell minimizes its native surface:

- no committed credentials or signing assets
- no cleartext navigation or mixed content
- no Android file/content access
- exact allowlist for in-app navigation
- native JavaScript bridge disabled
- no camera/microphone/storage permissions
- no push or native analytics SDK

File upload/download, OAuth, camera/microphone, push, native bridge commands, certificate pinning, and secure credential storage need explicit designs, threat review, platform permission behavior, and tests before profile support is added.

## Tests

- Python unit/integration tests cover schema rejection, origin canonicalization, path traversal, cross-profile determinism, concurrent generation, invocation outside the repo, and shell-injection-resistant subprocess usage.
- Common Kotlin tests cover exact origin allow, case normalization, lookalike/subdomain rejection, external cleartext handling, and unsafe/malformed URL blocking.
- CI builds both example profiles on Android and iOS.
- Release qualification still requires real-device, signed-build, site behavior, accessibility, and store-policy tests.
