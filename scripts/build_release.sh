#!/bin/bash
set -e

echo "Building Nabd OS Release APK..."

# Clean project
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Build App Bundle
./gradlew bundleRelease

echo "Release build complete!"
echo "APK located at: app/build/outputs/apk/release/app-release.apk"
echo "AAB located at: app/build/outputs/bundle/release/app-release.aab"
