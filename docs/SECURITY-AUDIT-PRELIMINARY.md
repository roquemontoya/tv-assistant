# Preliminary security and build audit

Date: 2026-11-04
Repository: roquemontoya/tv-assistant
Working branch: feature/pamela-samsung-flowbox
Scope: read-only inspection of repository metadata, README, AndroidManifest.xml, build.sh, dependency lock data, network security config, accessibility-service config, and third-party notices.

## Executive status

**Preliminary review only — not a security certification and not a build approval.** No source code was changed, no APK was built, and nothing was installed on the Flowbox F1.

The fork and working branch are present. The inspected build is a custom shell/JDK/Android-SDK pipeline rather than a conventional Gradle project. It targets Android API 28, declares minimum API 23, and uses Android SDK platform/build-tools 36.0.0.

## Confirmed positive controls

- The manifest sets `allowBackup=false`.
- The manifest sets `usesCleartextTraffic=false` and references a network security configuration.
- The inspected network configuration trusts system certificates and permits cleartext only for loopback address 127.0.0.1.
- The build dependency manifest pins three runtime JARs by SHA-256; the fetch script checks both cached and newly downloaded bytes against those hashes.
- The README documents bounded AI request/tool budgets, explicit screen-capture consent, local-only microphone diagnostics, and confirmation gates for certain sensitive actions. These are documented design claims; source-level verification remains pending.
- The manifest's accessibility and notification-listener services declare the corresponding Android binding permissions.
- The original project notices and third-party notices are present in the inspected fork. Keep all upstream and dependency notices in any redistributed build.

## Security-sensitive areas requiring source-level verification

1. **Accessibility service:** the service can retrieve window content, perform gestures, and filter key events. This is powerful access to other apps' interfaces. Review every action, package/origin restriction, password-field handling, and confirmation boundary.
2. **Screen capture:** the app declares media-projection foreground-service support. Verify consent lifecycle, notification behavior, screenshot retention, destinations, and cancellation paths.
3. **Microphone:** RECORD_AUDIO is declared. The README says capture is an explicit local diagnostic and does not retain audio or send it over the network. Verify the complete implementation and every call site before relying on that statement.
4. **Authentication and tokens:** inspect OAuth PKCE/state validation, issuer/audience/nonce/expiry checks, refresh rotation, revocation, Android Keystore usage, and account isolation.
5. **Networking/WebSocket:** inspect TLS/hostname verification, redirect handling, endpoint allowlists, message size/time limits, cancellation, and whether any user content or screenshots can reach unexpected destinations.
6. **Tool execution:** review schemas, argument validation, bounded loops, URL/package scoping, untrusted page content handling, and user confirmations for destructive or external actions.
7. **Exported components and intents:** confirm that exported services are callable only through the intended system permissions and that activities/intents cannot be abused by another app.

## Build-chain findings and cautions

- `build.sh` invokes aapt2, javac, jar, d8, zipalign, apksigner, and keytool directly.
- It fetches dependencies before compilation and verifies SHA-256 hashes.
- It creates a development keystore in the ignored `build/` directory with a fixed development password. This is suitable only for a local development/test artifact; it is not an acceptable production signing-key strategy. Never publish the development keystore.
- The resulting APK is signed and verified by apksigner, but signature verification alone does not establish that the code is safe.
- Reproducibility still needs to be demonstrated by recording exact JDK, SDK, build-tools, dependency hashes, source commit, and final APK hash.

## Compatibility implications for Flowbox F1

The manifest declares minSdk 23 and targetSdk 28. Android 12 satisfies the declared minimum API, but that alone does **not** establish compatibility. The Flowbox CPU ABI, installed Android runtime ABI, available system features, TV launcher behavior, permissions, WebSocket/TLS support, memory, and actual installation/runtime behavior remain unknown.

Do not select an APK variant or install until the Flowbox ABI and runtime details are measured. The inspected build script does not visibly select separate ARM/ARM64 variants; verify the generated DEX/APK contents and native-library inventory after a build.

## Next audit sequence

1. Complete source-level review of authentication, vault, networking, microphone, accessibility, capture, and tool execution.
2. Inspect all dependency metadata and build inputs; verify upstream license provenance and commit ancestry.
3. Record the Flowbox ABI and Android build fingerprint without changing device settings.
4. Build in a clean, isolated environment from a pinned commit; run offline tests and static checks.
5. Inspect APK manifest, DEX, packaged libraries, certificate, permissions, and SHA-256.
6. Only then decide whether a physical-device installation is justified, with a rollback/uninstall plan.

## Current result

No APK exists from this audit. No changes were made to application code. This report records verified manifest/build facts and clearly labels the remaining review work.
