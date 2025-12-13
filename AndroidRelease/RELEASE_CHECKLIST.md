# 🚀 DuoPlay Release Checklist

Complete step-by-step checklist for releasing your app to Google Play Store.

---

## Phase 1: Pre-Release Preparation

### 1.1 Development Complete
- [ ] All features implemented and working
- [ ] App thoroughly tested on multiple devices
- [ ] No critical bugs or crashes
- [ ] Performance optimized (smooth, responsive)
- [ ] WebView loads correctly
- [ ] Deep linking tested (if applicable)

### 1.2 Version Information
- [ ] Update version code in `androidApp/build.gradle.kts`
  ```kotlin
  versionCode = 1
  versionName = "1.0.0"
  ```
- [ ] Release notes prepared (see `documentation/PLAY_STORE_LISTING.md`)

### 1.3 Testing
- [ ] Test on real device (not just emulator)
- [ ] Test on different screen sizes
- [ ] Test on different Android versions (min API 24)
- [ ] Test offline behavior
- [ ] Test app startup and loading
- [ ] Test all user flows
- [ ] Memory and battery usage acceptable

---

## Phase 2: Build Preparation

### 2.1 Keystore Setup ✅
- [x] Keystore generated (`AndroidRelease/keystore/duoplay-release.keystore`)
- [x] Keystore properties configured (`keystore.properties`)
- [x] Credentials documented (`AndroidRelease/keystore/CREDENTIALS.md`)
- [ ] Keystore backed up to secure location
- [ ] Passwords stored in password manager

**⚠️ CRITICAL**: Never lose your keystore! Backup now!

### 2.2 Build Configuration ✅
- [x] Release signing configured in `build.gradle.kts`
- [x] ProGuard/R8 enabled for code minification
- [x] Resource shrinking enabled
- [ ] Verify `applicationId` is correct: `com.duoplay.app`
- [ ] Verify `BASE_URL` is correct in build config

### 2.3 Build Release
- [ ] Clean project: `./gradlew clean`
- [ ] Build AAB: Run `AndroidRelease/scripts/quick-release.sh`
- [ ] Verify AAB created: `AndroidRelease/builds/duoplay-v1.0.0-*.aab`
- [ ] Test the release build on device
- [ ] Verify app signing:
  ```bash
  jarsigner -verify -verbose -certs AndroidRelease/builds/[your-aab-file].aab
  ```

**Build Commands**:
```bash
# Quick way (recommended)
./AndroidRelease/scripts/quick-release.sh

# Or manual
./gradlew :androidApp:bundleDuoplayRelease
```

---

## Phase 3: Graphics Assets

### 3.1 App Icon ✅
- [x] App icon created and optimized
- [x] Multiple densities generated
- [x] Adaptive icon with foreground/background
- [x] Located in: `androidApp/src/duoplay/res/mipmap-*/`

### 3.2 Feature Graphic
- [ ] Create 1024 × 500 px feature graphic
- [ ] Use black background with white branding
- [ ] Include app icon and "DUO PLAY" text
- [ ] Save as: `AndroidRelease/graphics/feature-graphic.png`

**Quick Create**:
1. Open Canva/Figma
2. Canvas: 1024 × 500 px
3. Black background (#000000)
4. Add app icon
5. Add "DUO PLAY" in white
6. Export as PNG

### 3.3 Screenshots
- [ ] Capture minimum 2 phone screenshots (recommended 4-8)
- [ ] Dimensions: 1080 × 2340 px (or 1080 × 1920 px)
- [ ] Format: PNG or JPG
- [ ] Save to: `AndroidRelease/graphics/screenshots/phone/`

**Screenshots to capture**:
1. Main screen / splash screen
2. WebView loaded with content
3. (Optional) Additional key screens

**Capture method**:
```bash
# Via adb
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Or: Power + Volume Down on device
```

### 3.4 Optional Assets
- [ ] Tablet screenshots (if supporting tablets)
- [ ] Promo video (YouTube link)

---

## Phase 4: Play Store Setup

### 4.1 Google Play Console Account
- [ ] Register for Google Play Developer account
  - URL: https://play.google.com/console/signup
  - Cost: $25 one-time fee
  - Verification: 24-48 hours
- [ ] Payment method added
- [ ] Account verified and active

### 4.2 Privacy Policy
- [ ] Privacy policy created
- [ ] Privacy policy hosted online (accessible URL)
- [ ] URL documented: ___________________________________

**Quick Privacy Policy**:
- Use a generator: https://app-privacy-policy-generator.firebaseapp.com/
- Or create simple one on GitHub Pages
- Or host on your website

### 4.3 Store Listing Content
All content prepared in `documentation/PLAY_STORE_LISTING.md`

- [ ] App name: DuoPlay - Gaming Platform
- [ ] Short description (80 chars max)
- [ ] Full description (4000 chars max)
- [ ] Category: Games / Entertainment
- [ ] Content rating questionnaire answers ready
- [ ] Contact email: support@duoplay.app
- [ ] Website URL: https://duoplay-589720827506.us-west1.run.app

---

## Phase 5: Play Console Configuration

### 5.1 Create App
1. [ ] Go to Play Console: https://play.google.com/console
2. [ ] Click "Create app"
3. [ ] Enter app details:
   - Name: DuoPlay
   - Default language: English (United States)
   - App or game: Game
   - Free or paid: Free
4. [ ] Accept declarations and create

### 5.2 Store Listing
1. [ ] Navigate to "Store listing"
2. [ ] Fill in all required fields:
   - [ ] App name
   - [ ] Short description
   - [ ] Full description
3. [ ] Upload graphics:
   - [ ] App icon (auto from AAB)
   - [ ] Feature graphic
   - [ ] Screenshots (minimum 2)
4. [ ] Categorization:
   - [ ] App category
   - [ ] Tags/keywords
5. [ ] Contact details:
   - [ ] Email
   - [ ] Website
   - [ ] Privacy policy URL
6. [ ] Save

### 5.3 Content Rating
1. [ ] Start questionnaire
2. [ ] Select category: Games
3. [ ] Answer all questions honestly:
   - Violence: None
   - Mature content: No
   - User interaction: Yes (if chat/social)
4. [ ] Submit and apply rating
5. [ ] Save

### 5.4 App Content
1. [ ] Privacy policy URL added
2. [ ] Ads declaration: Yes/No
3. [ ] Data safety form completed:
   - [ ] Data collection types
   - [ ] Data usage purposes
   - [ ] Data sharing info
4. [ ] Target audience: 13+
5. [ ] Save

### 5.5 Production Release
1. [ ] Navigate to "Production" release
2. [ ] Create new release
3. [ ] Upload AAB file:
   - File: `AndroidRelease/builds/duoplay-v1.0.0-*.aab`
4. [ ] Release name: v1.0.0
5. [ ] Release notes:
   ```
   🎉 Welcome to DuoPlay!

   - Fast and responsive gaming platform
   - Beautiful minimalist design
   - Seamless cloud integration
   - Optimized performance

   Thank you for choosing DuoPlay!
   ```
6. [ ] Review release
7. [ ] Save (don't rollout yet)

### 5.6 Pricing & Distribution
1. [ ] Set pricing: Free
2. [ ] Select countries: All (or specific)
3. [ ] Device categories: Phone, Tablet
4. [ ] Save

---

## Phase 6: Final Review

### 6.1 Pre-Submission Checklist
- [ ] All Play Console sections show green checkmarks
- [ ] Store listing complete
- [ ] Graphics uploaded
- [ ] Content rating received
- [ ] App content declarations complete
- [ ] Release created with AAB
- [ ] Release notes added
- [ ] Pricing and distribution set

### 6.2 Final Verification
- [ ] Review app listing preview
- [ ] Double-check all information
- [ ] Verify screenshots look good
- [ ] Test AAB one more time on device
- [ ] Ensure privacy policy is live

### 6.3 Submit for Review
1. [ ] Navigate to production release
2. [ ] Click "Review release"
3. [ ] Review summary carefully
4. [ ] Click "Start rollout to Production"
5. [ ] Confirm rollout

**✅ Submitted!**

---

## Phase 7: Post-Submission

### 7.1 Monitoring (Days 1-7)
- [ ] Check Play Console for review status
- [ ] Typical review time: 1-3 days
- [ ] Watch for emails from Google Play
- [ ] Check for any rejection or issues

### 7.2 If Approved
- [ ] App goes live within hours
- [ ] Share Play Store link
- [ ] Monitor ratings and reviews
- [ ] Track crash reports
- [ ] Respond to user reviews

**Your app will be at**:
```
https://play.google.com/store/apps/details?id=com.duoplay.app
```

### 7.3 If Rejected
- [ ] Read rejection reason carefully
- [ ] Fix issues identified
- [ ] Update and rebuild AAB if needed
- [ ] Resubmit through Play Console

### 7.4 Post-Launch
- [ ] Set up crash reporting monitoring
- [ ] Plan for user feedback
- [ ] Schedule regular updates
- [ ] Track analytics and performance

---

## Quick Reference Commands

### Build Release
```bash
# Quick release (recommended)
./AndroidRelease/scripts/quick-release.sh

# Or build AAB manually
./gradlew clean :androidApp:bundleDuoplayRelease

# Or build APK for testing
./AndroidRelease/scripts/build-release-apk.sh
```

### Verify Signing
```bash
jarsigner -verify -verbose -certs AndroidRelease/builds/[file].aab
```

### Capture Screenshots
```bash
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

### Install Release APK
```bash
adb install AndroidRelease/builds/[file].apk
```

---

## Important Files Reference

| File | Location | Purpose |
|------|----------|---------|
| Keystore | `AndroidRelease/keystore/duoplay-release.keystore` | App signing |
| Credentials | `AndroidRelease/keystore/CREDENTIALS.md` | Passwords |
| AAB Output | `AndroidRelease/builds/` | Release builds |
| Store Listing | `AndroidRelease/documentation/PLAY_STORE_LISTING.md` | Copy text |
| This Checklist | `AndroidRelease/RELEASE_CHECKLIST.md` | You are here |

---

## Emergency Contacts

- **Play Console**: https://play.google.com/console
- **Google Support**: https://support.google.com/googleplay/android-developer
- **Policy Center**: https://play.google.com/about/developer-content-policy/

---

## Version Tracking

| Version | Date | Status | Notes |
|---------|------|--------|-------|
| 1.0.0   | ___  | [ ] Planning<br>[ ] Building<br>[ ] Testing<br>[ ] Submitted<br>[ ] Live | Initial release |

---

## Notes

Use this space for any release-specific notes:

```





```

---

**Good luck with your release! 🚀**
