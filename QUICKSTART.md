# Quick start

From the repository root:

```bash
./wrapper doctor --platform android
./wrapper doctor --platform ios
./wrapper validate --profile all
./wrapper list
```

Build and run one profile explicitly:

```bash
./wrapper build --profile duoplay --platform android --configuration debug --artifact apk
./wrapper run --profile duoplay --platform android

./wrapper build --profile duoplay --platform ios --configuration debug --artifact simulator-app
./wrapper run --profile duoplay --platform ios
```

Outputs are written under `dist/duoplay/`. Substitute `vman` or another validated profile; there is no implicit default profile.

To add an app, copy `profiles/duoplay.toml`, assign unique Android/iOS identifiers and schemes, set a canonical HTTPS allowlist, and provide an opaque 1024×1024 PNG. Run `./wrapper validate --profile all` before generating or building.

Useful checks:

```bash
python3 -m unittest discover -s tools/tests -v
./gradlew --no-parallel -PappProfile=duoplay :feature:webview:testDebugUnitTest
```

`simulator-app` is not an IPA. `xcarchive` is unsigned. An IPA requires an Apple Developer team, signing assets, and export options and is deliberately not fabricated by the CLI.

If a site renders unexpectedly, first open the same URL in the emulator/simulator's browser. The wrapper can prove that a page loaded while the site itself still fails to render on a particular browser engine.
