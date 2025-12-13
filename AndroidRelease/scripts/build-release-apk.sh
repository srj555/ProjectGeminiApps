#!/bin/bash

# Script to build signed APK for testing or distribution outside Play Store
# Note: Play Store requires AAB format, use build-release-aab.sh for store uploads

echo "📦 Building Signed APK"
echo "====================="
echo ""

# Navigate to project root
cd "$(dirname "$0")/../.."

# Check if keystore.properties exists
if [ ! -f "keystore.properties" ]; then
    echo "❌ Error: keystore.properties not found!"
    echo "   Please create keystore.properties with your signing credentials"
    echo "   See AndroidRelease/keystore/keystore.properties.template for reference"
    exit 1
fi

# Check if keystore file exists
KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Error: Keystore file not found at $KEYSTORE_FILE"
    echo "   Please run AndroidRelease/scripts/generate-keystore.sh first"
    exit 1
fi

# Prompt for flavor
echo "Available flavors:"
echo "  1. duoplay"
echo ""
read -p "Enter flavor name [duoplay]: " FLAVOR
FLAVOR=${FLAVOR:-duoplay}

# Convert to proper case for Gradle task
FLAVOR_CAPITALIZED="$(tr '[:lower:]' '[:upper:]' <<< ${FLAVOR:0:1})${FLAVOR:1}"

echo ""
echo "🏗️  Building ${FLAVOR} release APK..."
echo "   Task: :androidApp:assemble${FLAVOR_CAPITALIZED}Release"
echo ""

# Clean previous builds
./gradlew clean

# Build the APK
./gradlew ":androidApp:assemble${FLAVOR_CAPITALIZED}Release" --console=plain

if [ $? -eq 0 ]; then
    # Find the generated APK
    APK_PATH="androidApp/build/outputs/apk/${FLAVOR}/release/androidApp-${FLAVOR}-release.apk"

    if [ -f "$APK_PATH" ]; then
        # Copy to AndroidRelease/builds
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        OUTPUT_FILE="AndroidRelease/builds/${FLAVOR}-v1.0.0-${TIMESTAMP}.apk"
        cp "$APK_PATH" "$OUTPUT_FILE"

        # Get file size
        SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)

        echo ""
        echo "✅ Build successful!"
        echo ""
        echo "📦 Output file:"
        echo "   $OUTPUT_FILE"
        echo "   Size: $SIZE"
        echo ""
        echo "📲 Install on device:"
        echo "   adb install $OUTPUT_FILE"
        echo ""
        echo "🔍 To verify the APK signature:"
        echo "   jarsigner -verify -verbose -certs $OUTPUT_FILE"
        echo ""
        echo "⚠️  Note: For Play Store upload, use build-release-aab.sh instead"
        echo ""
    else
        echo "❌ Error: APK file not found at $APK_PATH"
        exit 1
    fi
else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
