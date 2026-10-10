# Android release notes

This directory contains historical DuoPlay store copy, graphics guidance, and a signing-property template. The root `./wrapper` command is the authoritative build entry point for every profile.

Validate and build an Android App Bundle from the repository root:

```bash
./wrapper validate --profile all
./wrapper build --profile duoplay --platform android --configuration release --artifact aab
```

The compatibility scripts require an explicit profile and delegate to the same command:

```bash
./AndroidRelease/scripts/quick-release.sh duoplay
./AndroidRelease/scripts/build-release-aab.sh vman
./AndroidRelease/scripts/build-release-apk.sh duoplay
```

Release builds fail unless the ignored root `keystore.properties` file resolves a real signing key. The wrapper never substitutes the debug key. Successful artifacts are copied to `dist/<profile>/android/`; do not upload an artifact until its ID, version, signing certificate, privacy declarations, store listing, and real-device behavior have been reviewed for that profile.

The material under `documentation/` was written for DuoPlay. Treat it as a starting point, not reusable app metadata. Never commit keystores, passwords, service-account files, or Play Console credentials.
