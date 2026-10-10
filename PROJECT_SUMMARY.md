# Revival summary

The original single-app KMM WebView shell has been converted into a validated, profile-driven Android/iOS wrapper without modifying the original dirty checkout.

Implemented:

- TOML profiles as the shared source of truth for identity, name, URL, allowlist, version, icon, and supported capabilities
- fail-closed schema, URL, identity, collision, icon, path, and capability validation
- deterministic, concurrent-safe Android/iOS generation
- explicit `doctor`, `list`, `validate`, `generate`, `build`, and `run` commands
- independent Android package IDs and iOS bundle IDs/schemes/assets
- generated iOS Xcode host and CocoaPods integration
- packaged artifact identity checks
- exact-origin WebView navigation policy and tests
- external-link delegation, unsafe-scheme blocking, Android back history, network retry, safe areas, and lifecycle fixes
- JavaScript bridge disabled by default; Android file/content and mixed-content access disabled
- two-profile Android and iOS CI build matrices
- DuoPlay and VMan debug builds and local emulator/simulator smoke evidence

Not represented as complete:

- Apple-signed IPA export, Android release signing, or store submission
- OAuth-specific flows, file upload/download, camera/microphone, push, native bridge APIs, production analytics, or certificate pinning
- website App/Universal Link association files
- full real-device/accessibility/store-review certification for every future profile
- toolchain upgrades needed to remove current AGP/compileSdk and Kotlin/Xcode compatibility warnings

Authoritative entry point: `./wrapper`. Historical material under `AndroidRelease/` is not the current build pipeline.
