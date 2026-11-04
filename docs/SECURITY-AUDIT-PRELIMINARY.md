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


## Source-level review — 2026-11-04

The following observations come from reading the actual Java implementations in `src/dev/mert/tvassistant/`. They are static-review findings, not runtime test results.

### Authentication and token storage

- `ChatAuth` creates cryptographically random state, nonce, and PKCE verifier values; uses S256; binds the callback listener to 127.0.0.1 on an ephemeral port; checks callback path/state; exchanges the code with the verifier; and verifies the ID-token RS256 signature against the provider JWKS.
- The ID-token checks include issuer, audience (and `azp` when multiple audiences are present), expiry, future `iat`/ `nbf` bounds, nonce, and non-empty subject. Reconnecting an existing account also checks subject continuity.
- `Vault` encrypts account JSON with AES-GCM and an Android Keystore AES key. The ciphertext and IV are stored in app-private SharedPreferences.
- Refresh is synchronized and saves the returned token set. Sign-out attempts remote revocation and reports when it could only sign out locally.
- **Still to test/review:** malformed/oversized callback inputs, concurrent sign-in/cancel/sign-out, key invalidation/device restore behavior, refresh-token rotation responses, and whether every diagnostic/error path remains free of credentials. The callback accepts loopback HTTP by design; state validation is the main callback binding control and must remain mandatory.

### Network and WebSocket

- `Net.open` rejects non-HTTPS URLs, uses platform TLS defaults, disables automatic redirects, applies connect/read timeouts, and bounds response bodies. Authenticated requests attach the bearer token only after the HTTPS check.
- `ResponseSocket` connects only to the literal `wss://api.openai.com/v1/responses` endpoint with a bearer header. It bounds message queue size, message size, total response size, and task duration, and deliberately does not replay a request after an ambiguous disconnect.
- On API levels below 24 the code uses HTTPS/SSE instead of the WebSocket library because the project documents a hostname-verification limitation in that library on older Android.
- **Still to test/review:** dependency source/version behavior for TLS hostname verification on API 24+, certificate failures, redirect/error handling, cancellation under blocked reads, and confirmation that tokens never enter logs or exception text. The WebSocket library behavior must be independently verified rather than inferred from the URL scheme.

### Microphone and screen capture

- `MicrophoneProbe` checks runtime RECORD_AUDIO permission, uses a bounded eight-second foreground capture, computes aggregate levels, zeroes sample buffers, releases AudioRecord, and does not construct an audio upload request in the inspected implementation.
- Bluetooth SCO is optional. Cleanup restores the prior SCO state in the remaining teardown path; routing success is reported separately from the mere presence of a route flag.
- `CaptureService` obtains a MediaProjection grant, runs as a foreground service with a visible stop action, retains one latest frame in memory, JPEG-encodes only after an explicit capture request, and closes the frame/reader/projection on teardown. The returned screenshot is explicitly marked as untrusted visual content.
- **Privacy limitation:** a screenshot can contain account details or other visible sensitive information. The implementation's password-field masking in accessibility nodes does not itself redact pixels in a screenshot. The user must treat screen vision as a separate, explicit disclosure to the AI service.
- **Still to test/review:** lifecycle races (stop while capture is pending), process death, repeated consent, protected/secure windows, whether the foreground service type is accepted on target OS versions, and verifying that all capture entry points require the user-approved session.

### Accessibility and tool execution

- `NavigationService` walks only visible nodes and bounds tree depth/node count. It masks password text/description, rejects typing into password fields, checks snapshot freshness, and recycles node references. Gestures are bounded to the current UI snapshot.
- The service can still read non-password text from other apps and click, type, scroll, focus, navigate Home/Back/Recents/notifications, and dispatch gestures. This is inherently high-trust access and should be enabled only by a user who understands the scope.
- `Tools` uses declared schemas and a step budget; the inspected action-plan/workflow code verifies final results before learning/replaying reusable paths. Some operations explicitly require host approval (for example note removal and sensitive actions); the full dispatch paths still need systematic verification.
- **Still to test/review:** every tool's side effects and approval requirement, package/origin enforcement, prompt-injection resistance from page/node/screenshot content, stale-node behavior during app changes, exported-component exposure, and Android-version-specific behavior.

### Findings and disposition

No confirmed exploitable vulnerability has been established by this static pass. The reviewed code contains several meaningful safeguards, but static inspection cannot prove that they work on the Flowbox or that every execution path uses them correctly.

No code changes, APK build, device installation, or network inference test was performed during this source review. The next safe step is offline source/test review and a clean build in an isolated environment, followed by Flowbox hardware/ABI identification before any installation decision.
