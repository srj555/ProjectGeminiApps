#!/bin/bash

# Script to generate Android keystore for app signing
# This keystore will be used to sign release builds for Play Store

echo "🔐 Generating Android Keystore for DuoPlay"
echo "==========================================="
echo ""

# Set keystore details
KEYSTORE_FILE="../keystore/duoplay-release.keystore"
KEY_ALIAS="duoplay"
VALIDITY_DAYS=10000

echo "📋 Keystore Configuration:"
echo "   File: $KEYSTORE_FILE"
echo "   Alias: $KEY_ALIAS"
echo "   Validity: $VALIDITY_DAYS days (~27 years)"
echo ""

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  Keystore already exists at $KEYSTORE_FILE"
    read -p "Do you want to overwrite it? (yes/no): " overwrite
    if [ "$overwrite" != "yes" ]; then
        echo "❌ Keystore generation cancelled"
        exit 1
    fi
    rm "$KEYSTORE_FILE"
fi

echo "🔑 You will be prompted for:"
echo "   1. Keystore password (keep this secure!)"
echo "   2. Key password (can be same as keystore password)"
echo "   3. Organization details (name, location, etc.)"
echo ""
echo "⚠️  IMPORTANT: Store these passwords securely!"
echo ""

# Generate keystore
keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_DAYS \
    -storetype JKS

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keystore generated successfully!"
    echo ""
    echo "📝 Next steps:"
    echo "   1. Create keystore.properties file with your passwords"
    echo "   2. Add keystore.properties to .gitignore (already done)"
    echo "   3. Never commit keystore or passwords to git!"
    echo ""
    echo "📄 Sample keystore.properties file:"
    echo "   storePassword=YOUR_KEYSTORE_PASSWORD"
    echo "   keyPassword=YOUR_KEY_PASSWORD"
    echo "   keyAlias=duoplay"
    echo "   storeFile=../AndroidRelease/keystore/duoplay-release.keystore"
    echo ""

    # List keystore info
    echo "🔍 Keystore information:"
    keytool -list -v -keystore "$KEYSTORE_FILE"
else
    echo ""
    echo "❌ Failed to generate keystore"
    exit 1
fi
