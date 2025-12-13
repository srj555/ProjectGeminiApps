# 📱 Google Play Store Submission Guide

Complete guide for first-time app submission to Google Play Store.

## Prerequisites

### 1. Google Play Developer Account
- **Cost**: $25 one-time registration fee
- **Sign up**: https://play.google.com/console/signup
- **Processing time**: 24-48 hours for account verification
- **Requirements**:
  - Valid Google account
  - Credit/debit card for payment
  - Government ID (may be required)

### 2. Required Assets & Information
All assets must be prepared before starting the submission process.

## Required App Assets

### Icon
- ✅ **Already created**: `androidApp/src/duoplay/res/mipmap-*/ic_launcher.png`
- **Format**: PNG (32-bit)
- **Sizes**: Multiple densities already included in project
- **Requirements**:
  - Must not be transparent
  - Must not be rounded (Android handles rounding)

### Feature Graphic
- **Size**: 1024 x 500 pixels
- **Format**: PNG or JPG
- **Requirements**:
  - No transparency
  - Used in Play Store listing
  - Should showcase app branding

### Screenshots (REQUIRED - Minimum 2, Maximum 8)
- **Phone**: 16:9 or 9:16 aspect ratio
  - Minimum: 320px
  - Maximum: 3840px
  - **Recommended**: 1080 x 1920 or 1080 x 2340
- **Tablet** (optional but recommended): 16:9 or 9:16
  - Minimum: 1200px
  - Maximum: 7680px
- **Format**: PNG or JPG (no alpha channel)
- **Requirements**:
  - Must show actual app interface
  - No device frames required
  - High quality, clear text

### Promo Video (Optional)
- **Platform**: YouTube video URL
- **Length**: 30 seconds to 2 minutes recommended
- **Content**: App demo, features showcase

## Step-by-Step Submission Process

### Step 1: Create Application
1. Go to Google Play Console
2. Click "Create app"
3. Fill in basic information:
   - **App name**: DuoPlay
   - **Default language**: English (United States)
   - **App or game**: Game
   - **Free or paid**: Free

### Step 2: Store Listing
Complete all fields in the Store listing section:

1. **App details**
   - Short description (80 chars)
   - Full description (4000 chars)
   - See `PLAY_STORE_LISTING.md` for content

2. **Graphics**
   - App icon: ✅ (auto-imported from APK/AAB)
   - Feature graphic: Upload 1024x500 PNG
   - Phone screenshots: Upload 2-8 images
   - Tablet screenshots: Upload (optional)

3. **Categorization**
   - Application type: Game / Entertainment
   - Category: Casual / Action (choose appropriate)
   - Tags: gaming, entertainment, mobile

4. **Contact details**
   - Email: support@duoplay.app
   - Phone: (optional)
   - Website: https://duoplay-589720827506.us-west1.run.app

5. **Privacy Policy**
   - ⚠️ **REQUIRED**: URL to privacy policy
   - Must be publicly accessible
   - Should be hosted on your domain

### Step 3: Content Rating
1. Start questionnaire
2. Select category: Games
3. Answer all questions honestly
4. Common questions:
   - Violence: None
   - Mature content: No
   - User interaction: Yes (if applicable)
5. Get rating (typically Everyone or Teen)

### Step 4: App Content
1. **Privacy Policy**: Provide URL
2. **Ads**: Yes/No (if app shows ads)
3. **Target audience**: 13+ recommended
4. **News app**: No
5. **COVID-19 contact tracing/status**: No
6. **Data safety**: Fill out data collection form
   - What data is collected
   - How it's used
   - Is it shared with third parties

### Step 5: Release
1. **Select countries**: Choose all countries or specific regions
2. **Create release**:
   - Choose **Production** track for public release
   - Or **Internal/Closed/Open testing** for testing

3. **Upload AAB**:
   ```bash
   # Build the AAB
   cd ProjectDuoPlay
   AndroidRelease/scripts/quick-release.sh

   # Upload the file from:
   AndroidRelease/builds/duoplay-v1.0.0-[timestamp].aab
   ```

4. **Release name**: v1.0.0 (Initial Release)

5. **Release notes**:
   - Add in all languages
   - See `PLAY_STORE_LISTING.md` for sample text

### Step 6: Pricing & Distribution
1. **Pricing**: Free (or set price)
2. **Countries**: Select countries for distribution
3. **Device categories**: Phone, Tablet, Chrome OS
4. **User programs**: Google Play for Education (if applicable)

### Step 7: Review & Publish
1. Complete all required sections (checklist will show)
2. Click "Review release"
3. Review all information carefully
4. Click "Start rollout to Production"

## Review Process

### Timeline
- **Review time**: 1-7 days (typically 1-3 days)
- **First release**: May take longer
- **Status**: Check in Play Console dashboard

### Possible Outcomes
1. **Approved**: App goes live within hours
2. **Rejected**: Review feedback and resubmit
3. **Suspended**: Serious policy violation

### Common Rejection Reasons
- Insufficient screenshots
- Missing privacy policy
- Incorrect content rating
- App crashes on test devices
- Policy violations
- Misleading description

## After Approval

### 1. Monitor Performance
- Track installs and ratings
- Respond to user reviews
- Monitor crash reports

### 2. Updates
To release updates:
1. Update `versionCode` and `versionName` in `build.gradle.kts`
2. Build new AAB
3. Upload to Play Console
4. Add release notes
5. Review and roll out

### 3. Marketing
- Share Play Store link
- Promote on social media
- Engage with users

## Important URLs

- **Play Console**: https://play.google.com/console
- **Policy Center**: https://play.google.com/about/developer-content-policy/
- **Help Center**: https://support.google.com/googleplay/android-developer

## Troubleshooting

### "App not signing"
- Verify `keystore.properties` is configured
- Check keystore file exists
- Ensure passwords are correct

### "Bundle/APK too large"
- Enable R8 minification (already enabled)
- Use Android App Bundles (recommended)
- Remove unused resources

### "Privacy Policy required"
Create a simple privacy policy:
```markdown
# Privacy Policy for DuoPlay

Last updated: [Date]

## Data Collection
We collect [specify data types]

## Data Usage
Your data is used to [specify purposes]

## Contact
support@duoplay.app
```

Host on GitHub Pages or your website.

## Checklist Before Submission

- [ ] Google Play Developer account created
- [ ] App built and tested thoroughly
- [ ] Screenshots prepared (minimum 2)
- [ ] Feature graphic created
- [ ] Privacy Policy URL ready
- [ ] Content rating questionnaire completed
- [ ] Store listing text written
- [ ] AAB file generated and tested
- [ ] All required information filled in Play Console

## Support

If you need help:
- **Google Play Support**: https://support.google.com/googleplay/android-developer
- **Community Forums**: https://www.reddit.com/r/androiddev
