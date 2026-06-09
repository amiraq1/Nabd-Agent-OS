#!/bin/bash
set -e

echo "Verifying Nabd OS Release build..."

APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "Error: Release APK not found at $APK_PATH"
    exit 1
fi

echo "Checking native libraries..."
# Use unzip/tar to inspect lib directory
if ! unzip -l "$APK_PATH" | grep -q "lib/arm64-v8a/libllama.so"; then
    echo "Error: libllama.so missing from APK"
    exit 1
fi

if ! unzip -l "$APK_PATH" | grep -q "lib/arm64-v8a/libnabd.so"; then
    echo "Error: libnabd.so missing from APK"
    exit 1
fi

echo "Verification complete: Native libraries present."
exit 0
