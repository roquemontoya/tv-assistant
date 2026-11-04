# Pamela: Samsung + Flowbox F1 + LG TV

## Purpose

This document records an exploratory compatibility and integration study based on the upstream **TV Assistant** project. The goal is to evaluate whether a Samsung phone can provide voice input while a Flowbox F1 connected to an LG television runs the TV interface and displays responses.

This is an evolving test and integration record. The development APK v0.2.4 has now been built, inspected, and successfully installed and launched on the Flowbox F1 connected to the LG television. This validates the TV-side app launch on the tested hardware; it does not yet validate the Samsung-to-Flowbox voice relay or full AI connectivity.

## Upstream attribution

- Original project: [mertmr/tv-assistant](https://github.com/mertmr/tv-assistant)
- Original author/maintainer: MertMR
- Upstream license: MIT (retain the original `LICENSE` and copyright notices in any redistribution)
- This repository is a fork for independent evaluation and modifications. The upstream project remains the original work; changes made here will be documented separately.

## Intended test arrangement

- **Voice input:** Samsung phone.
- **TV-side application:** Flowbox F1.
- **Display/audio output:** LG television and its existing connection to the Flowbox.
- **Network:** local Wi-Fi, subject to a security review of the proposed phone-to-TV communication channel.

The initial physical-device test confirms the TV Assistant v0.2.4 interface launches on the Flowbox F1 and is visible on the LG television. Exact device diagnostics, network topology, and audio routing still need to be recorded before implementing the phone-to-TV relay.

## Work plan

1. Record the upstream commit and inspect the complete build configuration and dependency sources.
2. Audit permissions, authentication/token handling, network transport, WebView/browser behavior, accessibility actions, screen capture, storage, and tool-execution approvals.
3. Record and verify Flowbox CPU ABI and Android compatibility against device diagnostics; do not assume that Android version alone establishes APK compatibility. The current development APK has no native libraries, but future dependencies must be checked.
4. Build from a pinned source revision in a reproducible environment; record toolchain versions and artifact hashes.
5. Run static checks and available unit/emulator tests before any physical-device installation.
6. Evaluate a phone-side voice-to-text path and a narrowly scoped local-network relay. Do not reuse ChatGPT subscription credentials as an API, scrape private app tokens, or upload audio through unsupported plan-sharing interfaces.
7. Initial milestone completed: install and launch development APK v0.2.4 on the Flowbox F1 and display its interface on the LG television. Continue with controlled permission review and functional tests before enabling sensitive capabilities.
8. Publish measured results, limitations, and any patches; offer useful changes upstream through a clearly attributed pull request when appropriate.

## Evidence and status

- Fork created under `roquemontoya/tv-assistant`.
- Working branch: `feature/pamela-samsung-flowbox`.
- Development APK v0.2.4 built and inspected by CI; automated checks passed (44 JVM tests, 0 failures; no physical-device tests in CI).
- APK v0.2.4 successfully installed and launched on the Flowbox F1; its interface is visible on the connected LG television (user-provided photo, 2026-11-04).
- The screenshot shows the interface currently in English and reports “No AI connection”; full AI connectivity and Samsung voice relay remain unimplemented/unverified.
- This is a successful initial installation/launch milestone, not proof that every tool, permission, or integration works.

## Internationalization (i18n) decision

The user approved the following product-planning decision on 2026-11-04:

- **Initial language:** automatic detection from the device locale/configuration.
- **Translation scope:** full experience, including interface labels, system/status messages, notifications, and assistant responses wherever the response engine supports language selection or prompting.
- **Manual override:** users must be able to choose another available language; preserve that choice across restarts and let an explicit user choice take precedence over automatic detection.
- **Fallback:** Latin American Spanish (es-419; Argentina-friendly wording) when the device locale is unavailable or unsupported.
- **Initial implementation strategy:** centralize user-facing strings in locale dictionaries/resources; avoid hard-coded text in screens and components. Keep internal command identifiers and tool/action contracts language-neutral. Add further locales without rewriting application logic.
- **Initial locale candidates:** Spanish (Latin America), English, and Brazilian Portuguese; additional languages can be added later.
- **Important distinction:** translating the UI does not by itself translate model-generated responses. The response layer must receive the selected locale and be tested separately; if the current app has no AI connection, this part remains planned, not implemented.

## Change log

- Initial case-study document added to establish attribution, scope, and a testable plan before changing application code.
- 2026-11-04: recorded successful installation and launch of development APK v0.2.4 on Flowbox F1 with LG display, based on user-provided evidence.
- 2026-11-04: recorded approved i18n policy: automatic initial locale, full translation scope, manual override with persistence, and Latin American Spanish fallback.


## Product milestone P-001 — First functional TV-side installation

**Date:** 2026-11-04  
**Build:** development APK v0.2.4  
**Target hardware:** Flowbox F1 connected to LG television

The user confirmed that the APK installed and launched successfully on the Flowbox F1. This is the first physical-device functional milestone for the Pamela adaptation. It confirms installation and launch of the current TV-side interface on the tested setup; it does not yet prove that all 40 listed tools execute correctly, that microphone capture works, or that AI connectivity and Samsung-to-Flowbox communication are available.

The current build is retained as a known-good baseline for subsequent development. Future changes should be compared against this baseline and tested on the same device arrangement when practical.

## Planned feature — In-app update checker and guided updater

**Status: planned; not implemented in v0.2.4.**

The user requested a way for an installed Pamela app to discover new versions from the project's GitHub repository, avoiding repeated manual ZIP downloads and extraction. The proposed design is:

1. **Release-based distribution:** publish installable APKs as versioned GitHub Releases (not temporary GitHub Actions workflow artifacts). Each release should include a version, release notes, APK, SHA-256 checksum, and build/inspection report.
2. **Update check:** add a Settings action (“Buscar actualizaciones”) and optionally a configurable periodic check. Query the repository's public Releases API for the newest compatible release. Show current version, available version, release date, and concise change notes. Do not download or install without the user's decision.
3. **Compatibility gate:** check Android version, supported ABI where relevant, package/application ID, version code, and release channel before offering an update. A newer version number alone is not sufficient.
4. **Integrity and authenticity:** verify the downloaded APK's SHA-256 against the release-published checksum, and ensure the APK is signed with the same trusted signing certificate as the installed app. A checksum alone does not establish publisher authenticity.
5. **User-approved installation:** download the APK, verify it, then hand it to Android's package installer using the supported package-install flow. Android may require the user to authorize installs from Pamela/the selected source and confirm the update. Do not attempt silent installation or bypass Android security controls.
6. **Safe failure and rollback planning:** preserve the currently installed version until Android successfully installs the update; explain network, compatibility, signature, and storage failures clearly. Keep a documented recovery path to reinstall a known-good signed release.
7. **Release pipeline:** connect the existing CI build, tests, dependency audit, APK inspection, signing, and checksum generation to a release workflow. Publish only after all required checks pass. Never publish a development build as a stable release by accident.

**Important implementation constraint:** Android generally does not allow an ordinary app to silently replace itself. Pamela's updater should therefore be a guided, user-confirmed update flow unless the device is later enrolled in a supported managed-device arrangement. Signing-key continuity is essential: losing or changing the signing key can prevent in-place updates.

**Recommended implementation order:** first finish and stabilize the current Flowbox functionality; then add version metadata and a release manifest; then implement “Buscar actualizaciones” and verified download; finally test a signed update from one development build to the next on the Flowbox F1. Keep update checks separate from the app's AI/network features so that Pamela can still open and operate locally when GitHub is unavailable.

## Change log

- 2026-11-04: recorded product milestone P-001 — development APK v0.2.4 installed and launched on Flowbox F1 with LG display, confirmed by the user.
- 2026-11-04: added the planned GitHub Releases-based update checker and guided updater design. No updater code or silent-install capability is claimed to exist yet.
