#!/bin/bash

# Script to build signed Android App Bundle (AAB) for Play Store release
# AAB is the recommended format for Play Store uploads

echo "📦 Building Signed Android App Bundle (AAB)"
echo "==========================================="
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
echo "🏗️  Building ${FLAVOR} release AAB..."
echo "   Task: :androidApp:bundle${FLAVOR_CAPITALIZED}Release"
echo ""

# Clean previous builds
./gradlew clean

# Build the AAB
./gradlew ":androidApp:bundle${FLAVOR_CAPITALIZED}Release" --console=plain

if [ $? -eq 0 ]; then
    # Find the generated AAB
    AAB_PATH="androidApp/build/outputs/bundle/${FLAVOR}Release/androidApp-${FLAVOR}-release.aab"

    if [ -f "$AAB_PATH" ]; then
        # Copy to AndroidRelease/builds
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        OUTPUT_FILE="AndroidRelease/builds/${FLAVOR}-v1.0.0-${TIMESTAMP}.aab"
        cp "$AAB_PATH" "$OUTPUT_FILE"

        # Get file size
        SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)

        echo ""
        echo "✅ Build successful!"
        echo ""
        echo "📦 Output file:"
        echo "   $OUTPUT_FILE"
        echo "   Size: $SIZE"
        echo ""
        echo "📤 Next steps:"
        echo "   1. Upload the AAB to Google Play Console"
        echo "   2. Create a new release in Play Console"
        echo "   3. Fill in release notes"
        echo "   4. Submit for review"
        echo ""
        echo "🔍 To verify the AAB signature:"
        echo "   jarsigner -verify -verbose -certs $OUTPUT_FILE"
        echo ""
    else
        echo "❌ Error: AAB file not found at $AAB_PATH"
        exit 1
    fi
else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
