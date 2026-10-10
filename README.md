# ProjectGeminiApps

ProjectGeminiApps is a Kotlin Multiplatform Android/iOS shell for packaging an HTTPS web app as independently branded mobile apps. A validated TOML profile is the only per-app source of truth; the generator derives Android resources and build constants, iOS build settings, schemes, entitlements, and icon sets from it.

The repository currently contains two working examples:

| Profile | Android ID | iOS bundle ID | Start URL |
| --- | --- | --- | --- |
| `duoplay` | `com.duoplay.app` | `com.duoplay.app` | `https://duoplay-589720827506.us-west1.run.app` |
| `vman` | `com.vman.app` | `com.vman.app` | `https://v-man-shadow-of-the-last-ninja-589720827506.us-west1.run.app` |

Both profiles have been built as debug APKs and unsigned iOS Simulator apps and smoke-tested on local emulators. That does not make a new profile store-ready by itself: signing, website association files, store metadata/privacy review, real-device coverage, and any site-specific capabilities remain per-app release gates.

## Quick start

Requirements are JDK 17, Python 3.11+, Android SDK/ADB for Android, and Xcode, XcodeGen, and CocoaPods for iOS. AAB builds additionally require `bundletool` so the packaged manifest can be inspected with the format's supported tooling before success is reported.

```bash
./wrapper doctor --platform android
./wrapper doctor --platform ios
./wrapper list
./wrapper validate --profile all

./wrapper build --profile duoplay --platform android --configuration debug --artifact apk
./wrapper build --profile duoplay --platform ios --configuration debug --artifact simulator-app
```

Run on an already available device or simulator:

```bash
./wrapper run --profile duoplay --platform android --screenshot evidence/android-duoplay.png
./wrapper run --profile duoplay --platform ios --screenshot evidence/ios-duoplay.png
```

`wrapper` resolves the repository from its own location, so these commands also work when invoked from another working directory. Generated files are profile-scoped, locked, atomically replaced, and ignored by Git.

## Add an app

Copy a profile under `profiles/`, supply one opaque 1024×1024 PNG without alpha under `profiles/icons/`, then change every identity and URL field:

```toml
schema_version = 1
id = "example"
display_name = "Example"
subtitle = ""
start_url = "https://app.example.com"
allowed_origins = ["https://app.example.com"]
icon = "icons/example.png"
version_name = "1.0.0"
version_code = 1
required_capabilities = ["external_navigation", "web_history", "offline_retry", "persistent_session"]

[android]
application_id = "com.example.app"

[ios]
bundle_id = "com.example.app"
scheme = "Example"
url_scheme = "example"
associated_domain = "app.example.com"
```

Then run:

```bash
./wrapper validate --profile all
./wrapper generate --profile example --platform all
./wrapper build --profile example --platform android --configuration debug --artifact apk
./wrapper build --profile example --platform ios --configuration debug --artifact simulator-app
```

Validation fails closed on unknown fields, duplicate app IDs/bundle IDs/schemes, invalid or non-HTTPS origins, userinfo/path-bearing origins, unsafe paths, invalid icons, or unsupported capability declarations. See [FLAVOR_GUIDE.md](FLAVOR_GUIDE.md) for the full onboarding and release checklist.

## Artifact model

| Platform | Artifact | Command value | Signing status |
| --- | --- | --- | --- |
| Android | APK | `apk` | Debug is SDK-signed; release requires local signing properties |
| Android | App Bundle | `aab` | Release requires local signing properties |
| iOS | Simulator app | `simulator-app` | Simulator-only, not an IPA |
| iOS | Device archive | `xcarchive` | Generated with code signing disabled; not distributable |
| iOS | IPA | `ipa` | Intentionally rejected until an Apple-signed archive and export options are supplied |

Artifacts are copied to `dist/<profile>/<platform>/`. The wrapper inspects the packaged Android application ID and signature and the iOS bundle/profile fields before reporting success. APK verification uses Android SDK tools; AAB verification uses `bundletool` plus `jarsigner`. Android release commands fail before Gradle unless a complete ignored signing configuration resolves to a real keystore, and they never fall back to debug signing.

## Runtime behavior and security

- Android uses `WebView`; iOS uses `WKWebView`.
- Only exact canonical HTTPS origins in `allowed_origins` stay inside the app. Host suffix lookalikes and cleartext URLs are not allowed.
- Main-frame HTTP(S), `mailto:`, and `tel:` destinations outside the allowlist are delegated to the operating system when reached through web navigation; unsafe schemes are blocked.
- Cross-origin subframes are cancelled instead of being allowed to launch the system browser. Android also applies a main-frame request fallback for POST navigations, which are not delivered to the normal navigation override callback.
- Android file/content access and mixed content are disabled; safe browsing is enabled.
- The native JavaScript bridge is disabled by generated profiles. JavaScript remains enabled because the wrapped sites require it.
- Web history is wired to Android system back. Network loss shows a retry surface and reconnect triggers reload.
- WebView instances are retained across Compose recomposition and disposed with their screen lifecycle.
- Safe-area insets are applied on both platforms.

The origin policy is a top-level navigation boundary, not a network or subresource firewall. Wrapped pages can still request their normal third-party assets; enforce API/content policy with the website's CSP, CORS, authentication, and server controls.

OAuth redirects, file upload/download handling, camera/microphone permissions, push notifications, a native bridge API, certificate pinning, and analytics SDK integration are **not** implemented capabilities. The profile validator rejects capabilities outside the currently supported set instead of silently producing a partially working app.

Universal/App Links also require hosting the correct `apple-app-site-association` and `assetlinks.json` files on each profile's domain. The generated app declarations alone are not sufficient.

## Architecture

```text
profiles/*.toml + profiles/icons/*.png
                    |
                    v
             tools/wrapper.py
              /           \
 Android generated config  iOS xcconfig/project/icon sets
              \           /
          shared KMP app + feature:webview
```

The Android application is one generated build target selected with `-PappProfile=<id>`, not a manually duplicated Gradle product-flavor block. The iOS project is generated with one target/scheme per profile. See [ARCHITECTURE.md](ARCHITECTURE.md).

## Verification

```bash
python3 -m unittest discover -s tools/tests -v
./gradlew --no-parallel -PappProfile=duoplay :feature:webview:testDebugUnitTest
```

CI runs profile/generator tests, navigation-policy tests, and an Android/iOS debug-build matrix for both example profiles. Local builds currently emit compatibility warnings because Android Gradle Plugin 8.5.2 predates compileSdk 35 support and Kotlin 2.0.21 predates Xcode 26 validation. Builds pass, but upgrading those toolchains from official compatibility guidance is a release gate rather than a warning to suppress.

During the October 2026 Pixel 9 emulator smoke test, both APKs installed and launched and the DuoPlay WebView reported page completion, but the current websites displayed a black surface. The same DuoPlay URL also displayed black in stock Chrome on that emulator. Android rendering/browser compatibility therefore remains an open release issue even though the wrapper processes and builds succeeded.

## Repository hygiene

Signing keys, credential property files, provisioning data, generated Xcode projects, Pods, build directories, and `dist/` are ignored. Do not commit secrets. The older `AndroidRelease/` material is historical store guidance; use `./wrapper` as the authoritative build entry point.
