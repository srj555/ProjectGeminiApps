# Android release checklist

Use this checklist separately for each profile. A successful debug build is not a release approval.

## Identity and website

- [ ] `./wrapper validate --profile all` passes.
- [ ] Display name, icon, Android application ID, start URL, and exact HTTPS origin allowlist are correct.
- [ ] `version_name` and monotonically increasing `version_code` are correct in the profile.
- [ ] The website's `assetlinks.json` contains the final application ID and release signing certificate fingerprint when App Links are required.
- [ ] External navigation, authentication redirects, cookies/session persistence, Android back, offline/retry, and error states were exercised on a real device.
- [ ] File uploads/downloads, camera/microphone, push, or a native bridge are not assumed; they are not implemented capabilities in this version.

## Signing and build

- [ ] The ignored root `keystore.properties` resolves the intended release key without exposing secrets.
- [ ] The key and credentials have a secure, tested backup.
- [ ] Build the profile explicitly:

```bash
./wrapper build --profile <profile> --platform android --configuration release --artifact aab
```

- [ ] Inspect `dist/<profile>/android/<profile>-release.aab` and verify the signing certificate.
- [ ] Install a release-derived APK set on a physical device and complete the critical user journey.
- [ ] Confirm no debug endpoints, test accounts, credentials, or private third-party code are packaged.

## Store and policy

- [ ] Store listing, screenshots, feature graphic, category, contact details, and support/privacy URLs are specific to this app.
- [ ] Complete Data safety and content-rating forms from the actual website/app behavior.
- [ ] Confirm target API, WebView behavior, accessibility, account deletion, payments, age-rating, and regional-policy obligations.
- [ ] Upload to a closed/internal track first and review automated pre-launch reports.
- [ ] Record the profile file, Git commit, artifact checksum, signing fingerprint, and Play Console release for traceability.

No script in this repository submits an app to Google Play.
