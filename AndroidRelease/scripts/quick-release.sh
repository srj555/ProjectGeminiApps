#!/bin/bash

# Quick Release Script - Build signed AAB in one command
# This is the simplest way to create a Play Store release

echo "🚀 Quick Release - DuoPlay"
echo "=========================="
echo ""

# Navigate to project root
cd "$(dirname "$0")/../.."

FLAVOR="duoplay"
VERSION="1.0.0"

echo "📋 Build Configuration:"
echo "   Flavor: $FLAVOR"
echo "   Version: $VERSION"
echo "   Output: AAB (Play Store format)"
echo ""

# Check prerequisites
if [ ! -f "keystore.properties" ]; then
    echo "❌ Error: keystore.properties not found!"
    exit 1
fi

KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Error: Keystore file not found at $KEYSTORE_FILE"
    exit 1
fi

echo "✅ Prerequisites checked"
echo ""

# Build
echo "🏗️  Building release AAB..."
./gradlew clean :androidApp:bundleDuoplayRelease --console=plain

if [ $? -eq 0 ]; then
    # Copy to releases folder
    AAB_PATH="androidApp/build/outputs/bundle/duoplayRelease/androidApp-duoplay-release.aab"
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    OUTPUT_FILE="AndroidRelease/builds/duoplay-v${VERSION}-${TIMESTAMP}.aab"
    cp "$AAB_PATH" "$OUTPUT_FILE"

    SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)

    echo ""
    echo "✅ Release build complete!"
    echo ""
    echo "📦 Output: $OUTPUT_FILE"
    echo "   Size: $SIZE"
    echo ""
    echo "📤 Upload to Play Console:"
    echo "   https://play.google.com/console"
    echo ""
else
    echo "❌ Build failed!"
    exit 1
fi
