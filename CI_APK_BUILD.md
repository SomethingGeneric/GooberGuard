# CI APK Build Configuration

This document explains the CI configuration changes made to enable APK artifact generation.

## Changes Made

### GitHub Actions Workflow Updates

The `.github/workflows/android.yml` file has been updated with the following changes:

1. **Changed build command**: 
   - From: `./gradlew build` 
   - To: `./gradlew assembleDebug`

2. **Added APK artifact upload**:
   - Uses `actions/upload-artifact@v4`
   - Artifact name: `gooberguard-debug-apk-{run_number}`
   - Path: `app/build/outputs/apk/debug/app-debug.apk`
   - Retention: 30 days

## How It Works

1. **Build Step**: The `assembleDebug` task builds a debug APK of the application
2. **Upload Step**: The generated APK is uploaded as a GitHub Actions artifact
3. **Artifact Access**: The APK can be downloaded from the Actions run page

## Expected Behavior

When the CI runs successfully:

- A debug APK will be built at `app/build/outputs/apk/debug/app-debug.apk`
- The APK will be uploaded as an artifact named `gooberguard-debug-apk-{run_number}`
- Users can download the APK from the GitHub Actions run page
- The artifact will be retained for 30 days

## APK Details

The generated APK will be:
- **Type**: Debug build (not production-ready)
- **Package**: `cloud.goober.gooberguard`
- **Version**: As defined in `app/build.gradle` (currently 1.0)
- **Min SDK**: API 29 (Android 10)
- **Target SDK**: API 34 (Android 14)

## Manual Testing

To test the APK generation locally (when Android SDK is available):

```bash
./gradlew assembleDebug
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.