#!/bin/bash

# ProjectDuoPlay Setup Verification Script
# This script checks if the project is properly set up

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     ProjectDuoPlay - Setup Verification Script            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
WARNINGS=0

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $2"
        ((FAILED++))
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

echo "Checking prerequisites..."
echo ""

# Check Java
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        print_status 0 "Java $JAVA_VERSION installed"
    else
        print_status 1 "Java version must be 17 or higher (found: $JAVA_VERSION)"
    fi
else
    print_status 1 "Java not found"
fi

# Check Gradle
if [ -f "./gradlew" ]; then
    print_status 0 "Gradle wrapper found"
else
    print_status 1 "Gradle wrapper not found"
fi

# Check Android SDK (optional but recommended)
if [ -n "$ANDROID_HOME" ]; then
    print_status 0 "ANDROID_HOME is set: $ANDROID_HOME"
else
    print_warning "ANDROID_HOME not set (required for Android development)"
fi

# Check for iOS tools (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo ""
    echo "Checking iOS prerequisites..."

    if command_exists xcodebuild; then
        XCODE_VERSION=$(xcodebuild -version | head -1 | awk '{print $2}')
        print_status 0 "Xcode $XCODE_VERSION installed"
    else
        print_status 1 "Xcode not found"
    fi

    if command_exists pod; then
        POD_VERSION=$(pod --version)
        print_status 0 "CocoaPods $POD_VERSION installed"
    else
        print_status 1 "CocoaPods not found (install: sudo gem install cocoapods)"
    fi
else
    print_warning "Not on macOS - iOS development not available"
fi

echo ""
echo "Checking project structure..."
echo ""

# Check for essential directories
DIRS=(
    "androidApp"
    "androidApp/src/main"
    "androidApp/src/duoplay"
    "iosApp"
    "shared"
    "core/common"
    "core/network"
    "core/analytics"
    "feature/webview"
)

for dir in "${DIRS[@]}"; do
    if [ -d "$dir" ]; then
        print_status 0 "Directory exists: $dir"
    else
        print_status 1 "Directory missing: $dir"
    fi
done

echo ""
echo "Checking essential files..."
echo ""

# Check for essential files
FILES=(
    "settings.gradle.kts"
    "build.gradle.kts"
    "gradle.properties"
    "gradle/libs.versions.toml"
    "androidApp/build.gradle.kts"
    "shared/build.gradle.kts"
    "README.md"
    "QUICKSTART.md"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        print_status 0 "File exists: $file"
    else
        print_status 1 "File missing: $file"
    fi
done

echo ""
echo "Checking Kotlin source files..."
echo ""

# Count Kotlin files
KOTLIN_FILES=$(find . -name "*.kt" -not -path "*/build/*" | wc -l | xargs)
if [ "$KOTLIN_FILES" -gt 20 ]; then
    print_status 0 "Found $KOTLIN_FILES Kotlin source files"
else
    print_warning "Only found $KOTLIN_FILES Kotlin files (expected 25+)"
fi

echo ""
echo "Testing Gradle build..."
echo ""

# Try to run Gradle tasks
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew

    echo "Running: ./gradlew tasks --quiet"
    if ./gradlew tasks --quiet > /dev/null 2>&1; then
        print_status 0 "Gradle build system working"
    else
        print_status 1 "Gradle build system has errors"
    fi
else
    print_status 1 "Cannot test Gradle (wrapper not found)"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    Verification Summary                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}Passed:${NC}   $PASSED"
echo -e "${RED}Failed:${NC}   $FAILED"
echo -e "${YELLOW}Warnings:${NC} $WARNINGS"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ Project setup looks good!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Run: ./gradlew build"
    echo "2. Open in Android Studio or"
    echo "3. Run: ./gradlew :androidApp:installDuoplayDebug"
    echo ""
    echo "For iOS (macOS only):"
    echo "cd iosApp && pod install && open iosApp.xcworkspace"
    exit 0
else
    echo -e "${RED}✗ Project setup has issues${NC}"
    echo ""
    echo "Please fix the failed checks above before continuing."
    echo "See README.md or QUICKSTART.md for setup instructions."
    exit 1
fi
