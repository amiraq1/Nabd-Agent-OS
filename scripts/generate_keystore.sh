#!/bin/bash
set -e

KEYSTORE_PATH="nabd-release.keystore"
ALIAS="nabd_key"

echo "Generating Release Keystore..."

if [ -f "$KEYSTORE_PATH" ]; then
    echo "Keystore already exists at $KEYSTORE_PATH"
    exit 0
fi

keytool -genkey -v \
    -keystore $KEYSTORE_PATH \
    -alias $ALIAS \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Nabd Agent OS, OU=Engineering, O=Nabd, C=US"

echo "Keystore generated successfully: $KEYSTORE_PATH"
