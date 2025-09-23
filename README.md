# GooberGuard
Android parental control app via DNS filtering using VPN service

## Overview

GooberGuard is an Android parental control application that helps parents and guardians manage what websites and apps can be accessed on an Android device. It works by creating a VPN tunnel and filtering DNS requests to block access to specified domains.

## How It Works

1. **VPN Service**: The app creates a local VPN connection that routes all device traffic through the GooberGuard service
2. **DNS Filtering**: When apps or browsers try to resolve domain names, GooberGuard intercepts these requests
3. **Domain Blocking**: If a domain is in the blocked list, the request is dropped, preventing the app or website from loading
4. **Persistent Protection**: The VPN service runs in the background, providing continuous protection

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
- **DNS Interception**: Processes DNS queries to filter blocked domains
- **SharedPreferences**: Stores blocked domains persistently on device
- **Background Service**: Continues protection even when app is closed

## Setup and Usage

1. Install the app on the target Android device
2. Grant VPN permission when prompted
3. Add domains to block (Instagram domains are pre-configured)
4. Toggle "Start Protection" to begin filtering
5. The app will show "Protected" status when active

## Security Notes

- All traffic filtering happens locally on the device
- No data is sent to external servers
- Uses Android's built-in VPN framework for security
- Requires explicit user permission to create VPN connection

## Development

Built with:
- Android SDK (API 29+)
- Java
- RecyclerView for domain list management
- SharedPreferences for data persistence
- VpnService for network interception
