# xToRayHD

Premium Android Xray/V2Ray client with a Telegram/Tonkeeper-inspired obsidian interface.

## Architecture

- Kotlin + Jetpack Compose Material 3 UI
- Android `VpnService` for the system tunnel
- Xray core through `AndroidLibXrayLite` (`libv2ray.aar`)
- Xray TUN inbound using the Android VPN file descriptor
- Local profile storage
- Subscription/share-link parsing
- VLESS, VMess, Trojan and Shadowsocks import
- Full-device IPv4/IPv6 routing
- Dark glass/tonal UI with 0.5x-feeling motion
- GitHub Actions builds the APK without Android Studio on your machine

The Android TUN integration follows Xray's documented Android model: the app obtains the `VpnService` file descriptor and passes it to the Xray library. Xray's TUN inbound is responsible for handling layer-3 traffic.

## Build from GitHub on a phone

1. Create a new empty GitHub repository named `xToRayHD`.
2. Upload this project to the repository.
3. Open the repository on GitHub.
4. Open **Actions**.
5. Select **Build xToRayHD APK**.
6. Tap **Run workflow**.
7. Wait for the workflow to finish.
8. Open the completed workflow run.
9. Download the `xToRayHD-debug` artifact.
10. Extract the APK and install it on Android.

No Android Studio is required for the GitHub build.

## First run

1. Launch xToRayHD.
2. Open **Profiles** or tap **Add subscription / config**.
3. Paste a subscription URL, a base64 subscription body, or a supported share link.
4. Import it.
5. Select the desired profile.
6. Return to **Home**.
7. Tap **Connect**.
8. Android will show the VPN permission dialog the first time.
9. Accept it.
10. xToRayHD establishes the system VPN and starts Xray.

## Supported import formats in v1

- `vless://`
- `vmess://`
- `trojan://`
- `ss://`
- newline-separated subscription bodies
- base64-encoded subscription bodies

## Important production work before publishing

The current repository is the functional foundation, not a claim that every provider-specific Xray transport is covered. Before a public release, expand the parser/config builder for the transports and options used by your target subscriptions, especially Reality, gRPC, WebSocket edge cases, TCP header settings, mux, routing rules, DNS modes, and subscription metadata.

Also add:

- signed release builds
- release keystore stored only in GitHub Actions secrets
- CI unit/instrumentation tests
- connection health monitoring
- automatic reconnect
- network handover handling
- per-app VPN selection
- traffic statistics
- latency testing
- QR import/export
- profile editor
- subscription auto-update
- subscription HTTP headers
- custom DNS
- routing presets
- kill switch
- always-on VPN compatibility
- backup/restore
- logs and diagnostics
- Play Store/F-Droid metadata as appropriate
- complete third-party license notices

## Security

Never commit private subscription URLs containing credentials or tokens. Do not put signing keys, passwords, or release secrets in the repository.

## Licensing

xToRayHD application code in this repository is original project code. The Xray Android library is obtained from the upstream AndroidLibXrayLite project and remains under its upstream license. Keep the upstream license and attribution when distributing the application.
