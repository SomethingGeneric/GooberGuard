# GooberGuard
Android parental control app via DNS filtering using VPN service

## Status: ✅ Complete and Ready for Testing

GooberGuard is now fully functional with all core features implemented:
- ✅ VPN service with proper DNS packet parsing
- ✅ Domain blocking with NXDOMAIN responses
- ✅ UI for managing blocked domains
- ✅ Persistent storage of blocked domains
- ✅ Default Instagram and Facebook domain blocks
- ✅ All unit tests passing

## Overview

GooberGuard is an Android parental control application that helps parents and guardians manage what websites and apps can be accessed on an Android device. It works by creating a VPN tunnel and filtering DNS requests to block access to specified domains.

## How It Works

1. **VPN Service**: The app creates a local VPN connection that routes all device traffic through the GooberGuard service
2. **DNS Filtering**: When apps or browsers try to resolve domain names, GooberGuard intercepts these DNS requests
3. **Packet Parsing**: The app parses IPv4/UDP/DNS packets to extract domain names from queries
4. **Domain Blocking**: If a domain matches the blocked list (exact or subdomain match), the app responds with NXDOMAIN (domain not found)
5. **Persistent Protection**: The VPN service runs in the background, providing continuous protection

## Features

- **Real-time Protection**: Toggle VPN protection on/off with visual status indicators
- **Custom Domain Blocking**: Add any domain to the blocked list
- **Pre-configured Blocks**: Comes with Instagram and common social media domains pre-blocked
- **Persistent Storage**: Blocked domains are saved and persist across app restarts
- **User-friendly Interface**: Simple controls for managing protection and blocked domains

## Target Use Cases

- **Instagram Blocking**: Prevents both the Instagram app and website from loading
- **Social Media Control**: Block access to various social media platforms
- **Website Filtering**: Block any website by adding its domain
- **App Blocking**: Since apps use the same domains as their websites, blocking the domain blocks both

## Technical Implementation

- **VPN Service**: Uses Android's `VpnService` API to intercept network traffic
- **DNS Packet Parsing**: Custom `DnsPacketParser` class that:
  - Identifies DNS query packets (IPv4/UDP/port 53)
  - Extracts domain names from DNS QNAME format
  - Creates NXDOMAIN responses for blocked domains
  - Supports domain compression and subdomain matching
- **SharedPreferences**: Stores blocked domains persistently on device
- **Background Service**: Continues protection even when app is closed
- **Domain Matching**: Supports both exact matches and subdomain wildcarding

## Setup and Usage

### Installation
1. Build the APK using `./gradlew assembleDebug` (or install via Android Studio)
2. Install the APK on the target Android device
3. Launch the GooberGuard app

### First Time Setup
1. The app comes pre-configured with Instagram and Facebook domains blocked
2. Grant VPN permission when prompted (required for the app to work)
3. You can add or remove domains as needed

### Using the App
1. **View Blocked Domains**: The main screen shows all currently blocked domains
2. **Add a Domain**: 
   - Enter a domain name in the text field (e.g., `tiktok.com`)
   - Tap "Add Domain"
   - The domain supports subdomain matching (blocking `tiktok.com` also blocks `www.tiktok.com`, `api.tiktok.com`, etc.)
3. **Remove a Domain**: Tap on any domain in the list and confirm removal
4. **Start Protection**: 
   - Tap "Start Protection" button
   - Grant VPN permission if prompted
   - Status will change to "Protected" with green text
5. **Stop Protection**: Tap "Stop Protection" to disable filtering

### Testing the App
To verify the app is working:
1. Start Protection
2. Ensure Instagram domains are in the blocked list
3. Try to open Instagram app or visit instagram.com in a browser
4. The app/site should fail to load (connection error or "can't find server")
5. Check Android system logs (`adb logcat | grep GooberVpnService`) to see blocked domains in real-time

## Security Notes

- All traffic filtering happens locally on the device
- No data is sent to external servers
- Uses Android's built-in VPN framework for security
- Requires explicit user permission to create VPN connection

## Development

Built with:
- Android SDK (API 29+, target API 34)
- Java 17
- Gradle 9.3.1
- RecyclerView for domain list management
- SharedPreferences for data persistence
- VpnService for network interception
- Custom DNS packet parsing (IPv4/UDP)

### Building
```bash
./gradlew assembleDebug  # Build debug APK
./gradlew test           # Run unit tests
```

### Testing
```bash
# Unit tests (DomainManager logic)
./gradlew test

# Manual testing requires Android device with VPN capability
# Cannot be tested in emulator without full network stack
```

## Limitations and Known Issues

1. **IPv4 Only**: Currently only parses IPv4 packets, IPv6 is forwarded without filtering
2. **DNS Only**: Only blocks DNS-based requests; apps using hardcoded IPs will not be blocked
3. **No HTTPS Interception**: Cannot inspect encrypted HTTPS traffic (by design for privacy)
4. **Split Tunneling**: Some apps may use alternative DNS or split tunneling to bypass VPN
5. **Performance**: All traffic goes through the VPN; minimal overhead but measurable on slow devices

## Future Enhancements

Potential improvements for future versions:
- IPv6 support
- Scheduled blocking (time-based rules)
- App-specific blocking (block specific apps rather than domains)
- Statistics dashboard (blocked requests counter, most accessed blocked domains)
- Import/export blocked domain lists
- Category-based blocking (social media, gaming, etc.)
