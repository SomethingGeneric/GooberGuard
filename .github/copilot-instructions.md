# GooberGuard
Android parental control VPN application for DNS-based domain blocking. This app creates a local VPN service to intercept and filter network traffic, blocking access to specified domains.

**ALWAYS reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Build Requirements and Setup
**CRITICAL**: This Android app requires Android SDK and cannot be built in environments without it.

- Java 17 (OpenJDK Temurin) - available via system package manager
- Android SDK API 29+ (required for Android Gradle Plugin)
- Android Gradle Plugin 8.2.2
- Gradle 8.14.3 (downloaded automatically via gradlew)

### Installing Android SDK
**REQUIRED FOR ALL BUILD OPERATIONS**: Android SDK must be installed before any Gradle tasks can run.

Install via Ubuntu/Debian package manager:
```bash
sudo apt update
sudo apt install google-android-cmdline-tools-9.0-installer google-android-licenses
```

**NETWORK DEPENDENCY WARNING**: The Android SDK installation requires internet access to download components from dl.google.com. If network access is restricted, the installation will fail with "No address associated with hostname" errors.

**Alternative installation**: If apt packages fail, manually download and install Android command line tools:
```bash
# Download from https://developer.android.com/studio#command-line-tools-only
# Extract to /opt/android-sdk/cmdline-tools/latest/
export ANDROID_HOME=/opt/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

### Build Process
**PREREQUISITE CHECK**: Verify environment first:
```bash
# Check network access to required repositories
curl -s https://maven.google.com > /dev/null && echo "Google repo accessible" || echo "Google repo BLOCKED"
curl -s https://repo1.maven.org/maven2 > /dev/null && echo "Maven Central accessible" || echo "Maven Central BLOCKED"
```

**DO NOT attempt build commands if either repository is blocked.**

```bash
cd /home/runner/work/GooberGuard/GooberGuard
chmod +x gradlew

# Test Gradle setup (should work even without Android SDK)
./gradlew --version

# Attempt build (requires full Android environment)
./gradlew build
```

**TIMING EXPECTATIONS**:
- Repository connectivity check: 10-15 seconds
- Gradle wrapper download (first run): 1-2 minutes - NEVER CANCEL
- Android Gradle Plugin download: 2-3 minutes - NEVER CANCEL  
- Android SDK component downloads (first run): 10-15 minutes - NEVER CANCEL
- Full build: 5-8 minutes - NEVER CANCEL
- **TOTAL FIRST BUILD**: 20-30 minutes - NEVER CANCEL
- **Subsequent builds**: 2-5 minutes
- Set timeout to 45+ minutes for initial builds, 10+ minutes for subsequent builds

**BUILD FAILURE INDICATORS**:
- "Plugin [id: 'com.android.application'] was not found" = Android Gradle Plugin cannot be downloaded from repositories
- Network timeout errors = Cannot access Google/Maven repositories or SDK components
- Missing build-tools errors = Android SDK installation incomplete
- "No address associated with hostname" = Network access to dl.google.com blocked

**CRITICAL NETWORK DEPENDENCIES**: This project cannot build in network-restricted environments that block:
- google() repository (for Android Gradle Plugin)
- mavenCentral() repository (for dependencies)  
- dl.google.com (for Android SDK components)

### Testing
```bash
# Unit tests (run on development machine)
./gradlew test

# Instrumented tests (require Android device/emulator)
./gradlew connectedAndroidTest
```

**TIMING EXPECTATIONS**:
- Unit tests: 2-3 minutes - NEVER CANCEL
- Instrumented tests: 5-10 minutes (if device available) - NEVER CANCEL
- Set timeout to 15+ minutes for test commands

### Validation Requirements
**MANUAL VALIDATION SCENARIOS**: After making changes, always test these core user flows:

1. **Domain Management Flow**:
   - Launch app and verify default Instagram domains are pre-loaded
   - Add a new domain (e.g., "facebook.com") and verify it appears in the list
   - Tap a domain to remove it and confirm removal dialog works
   - Verify domain persistence after app restart

2. **VPN Protection Flow**:
   - Toggle "Start Protection" and grant VPN permission when prompted
   - Verify status changes to "Protected" and button text changes to "Stop Protection"
   - Check that VPN icon appears in system notification area
   - Toggle protection off and verify status updates

3. **Invalid Input Handling**:
   - Try adding invalid domains (e.g., "not-a-domain", "http://site.com")
   - Verify appropriate error messages appear
   - Try adding duplicate domains and verify "already blocked" message

**Cannot validate actual network filtering without Android device with VPN capability.**

## Architecture Overview

### Key Components
- **MainActivity.java**: Main UI controller with domain management and VPN toggle
- **GooberVpnService.java**: VPN service that intercepts DNS requests and blocks domains
- **DomainManager.java**: Handles persistent storage of blocked domains via SharedPreferences
- **DomainAdapter.java**: RecyclerView adapter for displaying blocked domains list

### File Structure
```
app/src/main/java/cloud/goober/gooberguard/
├── MainActivity.java          # Main activity with UI logic
├── GooberVpnService.java      # VPN service implementation
├── DomainManager.java         # Domain storage management
└── DomainAdapter.java         # RecyclerView adapter

app/src/main/res/layout/
└── activity_main.xml          # Main UI layout

app/src/main/AndroidManifest.xml # App permissions and service declarations
```

### Key Permissions (AndroidManifest.xml)
- `android.permission.BIND_VPN_SERVICE` - Required for VPN functionality
- `android.permission.INTERNET` - Required for DNS queries

## Common tasks

### Repository Analysis (No Build Required)
The following are outputs from frequently run analysis commands. Reference them instead of re-running to save time:

#### Project Structure
```bash
find app/src -name "*.java"
```
```
app/src/main/java/cloud/goober/gooberguard/GooberVpnService.java
app/src/main/java/cloud/goober/gooberguard/MainActivity.java  
app/src/main/java/cloud/goober/gooberguard/DomainManager.java
app/src/main/java/cloud/goober/gooberguard/DomainAdapter.java
app/src/test/java/cloud/goober/gooberguard/ExampleUnitTest.java
app/src/androidTest/java/cloud/goober/gooberguard/ExampleInstrumentedTest.java
```

#### SDK and Dependency Versions
```bash  
grep -E "(compileSdk|minSdk|targetSdk)" app/build.gradle
```
```
compileSdk 34
minSdk 29  
targetSdk 34
```

```bash
grep -E "implementation|testImplementation" app/build.gradle
```
```
implementation 'androidx.appcompat:appcompat:1.7.0'
implementation 'com.google.android.material:material:1.12.0' 
implementation 'androidx.activity:activity:1.9.2'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.2.1'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
```

### Adding New Blocked Domains Programmatically
Look at `DomainManager.addDefaultBlockedDomains()` method around line 23-28 in DomainManager.java.

### Modifying VPN Logic
Core VPN packet processing is in `GooberVpnService.processPackets()` method. DNS filtering logic starts around line 80 in GooberVpnService.java.

### UI Changes
Main layout is defined in `app/src/main/res/layout/activity_main.xml`. UI event handlers are in MainActivity.onCreate() around lines 70-120.

### Domain Validation
Domain validation logic is in `MainActivity.isValidDomain()` around line 175-185. Uses regex pattern matching for basic domain format validation.

## Testing and CI

### Local Development Testing
```bash
# Check code compiles
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Generate APK for manual testing
./gradlew assembleDebug
# APK will be in app/build/outputs/apk/debug/
```

### GitHub Actions CI
The repository uses `.github/workflows/android.yml` which:
- Runs on Ubuntu with JDK 17
- Executes `./gradlew build`
- Requires Android SDK to be available in CI environment

**CI LIMITATIONS**: CI will fail if Android SDK is not properly installed in the runner environment.

## Environment Compatibility

### ✅ WILL WORK IN:
- Local development machines with unrestricted internet access
- Android Studio with properly configured SDK
- GitHub Actions runners with Android SDK setup action
- CI/CD environments with network access to Google/Maven repositories

### ❌ WILL NOT WORK IN:
- Network-restricted environments (corporate firewalls blocking Google repos)
- Sandboxed environments without Android SDK
- Offline development environments
- Standard Linux containers without Android development setup

### Minimal Working Environment Requirements:
```bash
# 1. Internet access to these domains:
# - google() repository (https://maven.google.com)
# - mavenCentral() (https://repo1.maven.org/maven2)
# - dl.google.com (Android SDK components)

# 2. Android SDK installation
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# 3. Java 17+
java -version  # Should show OpenJDK 17+
```

## Working in Restricted Environments

### What CAN be done without Android SDK:
```bash
# Code analysis and exploration
find app/src -name "*.java"  # List all Java source files
grep -r "VpnService" app/src/main/java/  # Find VPN-related code
grep -A 5 -B 2 "isValidDomain" app/src/main/java/cloud/goober/gooberguard/MainActivity.java  # Domain validation logic
git log --oneline -10  # Recent commits

# File structure exploration  
ls -la app/src/main/java/cloud/goober/gooberguard/  # Main source files
cat app/src/main/AndroidManifest.xml  # App permissions and components
ls -la app/src/main/res/layout/  # UI layouts

# Check project configuration
cat build.gradle  # Root build configuration
cat app/build.gradle  # App build configuration 
cat gradle.properties  # Gradle settings
cat .github/workflows/android.yml  # CI configuration

# Analyze dependencies and versions
grep -E "(compileSdk|minSdk|targetSdk)" app/build.gradle
grep -E "implementation|testImplementation" app/build.gradle
```

### Code Review Tasks (No Build Required):
- Review Java source code in `app/src/main/java/cloud/goober/gooberguard/`
- Examine UI layouts in `app/src/main/res/layout/`
- Check permissions in `AndroidManifest.xml`
- Analyze dependencies in `build.gradle` files
- Review test files in `app/src/test/` and `app/src/androidTest/`

### What CANNOT be done without full Android environment:
- Any `./gradlew` commands
- Building APK files
- Running tests
- Code compilation verification
- Dependency resolution

## Troubleshooting Common Issues

### "Plugin not found" errors
- **Cause**: Android Gradle Plugin not available
- **Solution**: Install Android SDK command line tools first

### Network timeout during build
- **Cause**: Android SDK components downloading
- **Solution**: Wait for completion (can take 15+ minutes), ensure internet access

### VPN permission denied
- **Cause**: Android VPN permission not granted by user
- **Solution**: Manual user interaction required, cannot be automated

### App crashes on domain validation
- **Check**: MainActivity.isValidDomain() method regex pattern
- **Common issue**: Invalid regex escaping in domain pattern

## Security Considerations
- All domain filtering happens locally on device
- No external server communication for filtering logic
- SharedPreferences storage is private to app
- VPN service requires explicit user permission grant
- DNS queries are intercepted but not logged externally

## Performance Notes
- VPN service runs continuously when enabled
- Domain list stored in memory for fast lookup
- SharedPreferences I/O happens on main thread (potential improvement area)
- RecyclerView used for efficient domain list display

## Known Limitations
- Cannot build or test without Android SDK installation
- VPN functionality cannot be tested without Android device
- Network filtering effectiveness depends on Android VPN API limitations
- Some apps may bypass VPN through alternative DNS resolution methods