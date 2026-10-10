# App profile guide

Each app is a TOML document under `profiles/`. Do not add Gradle product-flavor blocks or hardcode a second iOS configuration.

## Required fields

- `id`: lowercase generator key (`[a-z][a-z0-9_]*`)
- `mode`: `fixed` for one allowlisted site or `launcher` for Portal-style confirmed HTTPS selection
- `display_name`, `subtitle`: user-facing branding
- `start_url`: absolute HTTPS URL whose origin is in `allowed_origins`
- `allowed_origins`: exact canonical HTTPS origins; no path, query, fragment, credentials, or wildcard
- `icon`: relative path inside `profiles/` to an opaque 1024×1024 PNG
- `version_name`: SemVer
- `version_code`: positive integer used for Android and iOS build versions
- `required_capabilities`: only capabilities the runtime actually supports
- Android `application_id`
- iOS `bundle_id`, `scheme`, `url_scheme`, and `associated_domain`

Supported capability declarations in schema v1 are `dynamic_https_destination`, `external_navigation`, `web_history`, `offline_retry`, `persistent_session`, and `settings_reset`. `dynamic_https_destination` is for `launcher` mode; a launcher starts with a blank `start_url`, an empty `allowed_origins` list, and no associated domain.

Requests for OAuth specialization, uploads/downloads, camera, microphone, push, analytics, or a native JavaScript API require implementation and review before they can be declared.

## Onboarding workflow

1. Copy an existing TOML file, opaque 1024px icon, and `profiles/android/<id>/res` icon-resource directory.
2. Replace every identity, scheme, domain, URL, name, and icon reference.
3. Validate all profiles together so collisions are detected.
4. Generate both platforms.
5. Build both debug artifacts.
6. Install both the old and new app together to prove identifiers do not collide.
7. Test startup, allowed navigation, disallowed/external navigation, Android system back, offline/reconnect, authentication/session persistence, and site-specific input on real devices.
8. Configure signing outside the repository.
9. Complete store, privacy, legal, and website-association checks.

The Android resource directory must include density-correct legacy and round PNGs, adaptive foreground/background resources, and an Android 13 monochrome drawable. Validation fails if any required icon variant or size is missing. The iOS generator derives its asset catalog from the opaque 1024px profile icon.

```bash
./wrapper validate --profile all
./wrapper generate --profile example --platform all
./wrapper build --profile example --platform android --configuration debug --artifact apk
./wrapper build --profile example --platform ios --configuration debug --artifact simulator-app
```

The A→B→A and concurrent generator tests guard against stale or cross-profile output:

```bash
python3 -m unittest tools.tests.test_wrapper.GeneratorIntegrationTest -v
```

## Release gates

### Android

- Supply an ignored `keystore.properties` file and keystore using the existing local release-signing convention.
- Install `bundletool` before producing an AAB; the wrapper fails rather than claiming packaged identity verification without it.
- Build `apk` for device testing and `aab` for Play distribution.
- Verify the packaged application ID/version and test the release build.
- Complete Play target-SDK, data-safety, content-rating, privacy-policy, and account requirements.

### iOS

- Use `simulator-app` only for simulator testing.
- An unsigned `xcarchive` proves a device archive can be assembled but cannot be installed or submitted.
- Configure an Apple team, certificate/profile management, capabilities, and export options before producing an IPA.
- Verify bundle ID/version/entitlements and test an Apple-signed build on a physical device.
- Complete App Store privacy, export compliance, review, and account requirements.

### Website

- Confirm the site is mobile-ready in both Android System WebView and iOS WKWebView.
- Host App/Universal Link association files for the exact identifiers.
- Confirm authentication providers allow embedded user agents or intentionally open system browser flows.
- Test cookies, local storage, redirects, service workers, downloads/uploads, external links, viewport/safe areas, keyboard behavior, and offline recovery.

Do not mark a profile production-ready solely because debug builds compile.
