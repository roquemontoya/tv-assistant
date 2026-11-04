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


## Release channel decision — Pre-release 1

**User decision:** designate the already-installed development build v0.2.4 as the first pre-release milestone (Pre-release 1), then continue development through the agreed feature set until a stable release is justified.

**Versioning clarification:** the existing APK was built with version v0.2.4. It must not be silently relabeled as a different binary version. For release presentation, record it as **Pamela Pre-release 1 (based on build v0.2.4)**. The next newly built prerelease should use an explicit SemVer prerelease identifier (for example, `0.2.5-beta.1` or the next version selected after inspecting the app's actual versionCode/versionName configuration). Do not change versionCode/versionName until the Gradle/build configuration and installed package metadata have been inspected.

**Publication status:** this decision is recorded, but a formal GitHub Release/tag is not claimed as published here. The available repository integration can read and commit files but does not expose a GitHub Releases create/publish action. The existing CI artifact is temporary and is not a substitute for a durable GitHub Pre-release.

## Roadmap to stable release

Work remains on `feature/pamela-samsung-flowbox`; do not merge into `main` without explicit approval.

1. **Baseline and inventory:** preserve v0.2.4 as the known-good physical-install baseline; inspect package ID, versionCode/versionName, signing certificate continuity, target/min Android versions, ABI, permissions, and the actual source/build configuration.
2. **Functional inventory:** verify the documented 40 tools against implementation and tests; distinguish implemented, partially supported, unavailable, and unverified behavior. Do not treat a tool being listed as proof it works on Flowbox.
3. **UX and i18n:** retain language-neutral internal tool identifiers; add user-facing labels/descriptions; implement automatic locale detection, manual override with persistence, and es-419 fallback. Test interface strings separately from AI-generated response language.
4. **Device capability and voice integration:** implement and validate the Samsung-to-Flowbox local-network voice path, with explicit pairing, authentication, narrow network permissions, clear connection status, and user-controlled microphone activation. Do not assume the TV box microphone works.
5. **AI integration:** implement/configure a supported AI connection with explicit consent, secure credential handling, bounded tool execution, visible action status, and no unsupported reuse of subscription credentials as an API.
6. **GitHub update mechanism:** publish durable, signed GitHub Releases with APK, release notes, SHA-256, signing-certificate fingerprint, compatibility metadata, and inspection report. Add stable/prerelease channel selection, release lookup, compatible-version checks, verified download, certificate continuity check, and Android's user-confirmed package installer handoff. No silent installation or security bypass.
7. **Release automation:** extend CI so tests, dependency audit, reproducible build checks, APK inspection, signing, checksum generation, and release packaging must all pass before publication. Keep secrets/signing keys out of the repository and CI logs.
8. **Physical acceptance:** test installation and in-place update on Flowbox F1; test Samsung voice relay and LG output; exercise offline/no-GitHub, interrupted download, invalid checksum/signature, incompatible APK, denied permissions, and recovery to the known-good build.
9. **Stable gate:** publish stable only after all critical tests pass, known limitations are documented, update/rollback recovery is demonstrated, and the user accepts the release candidate.

**Completion rule:** each milestone is reported as implemented, tested in CI, tested on physical hardware, or still pending. A successful build is not a substitute for a physical-device test. The current v0.2.4 installation remains the baseline; no claim is made that the updater, multilingual UI, Samsung relay, or full AI workflow is already implemented.


## P-002 / P-003 progress — 2026-11-04

- **P-002 static tool inventory:** completed. Confirmed 40 tool identifiers are registered in `Tools.register()`; the detailed grouped inventory is in [P-002-TOOLS-INVENTORY.md](P-002-TOOLS-INVENTORY.md). This is a source inventory, not a claim that all 40 tools have passed physical-device tests.
- **P-003 internationalization foundation:** initial code committed. Added `LocaleText.java` with automatic locale selection (device locale), manual persisted choices for automatic, Spanish, English, and Brazilian Portuguese, core interface translations, and translated display labels for all 40 tool IDs. The internal tool IDs and execution contracts remain unchanged.
- **P-003 limitations:** this is a foundation, not complete localization. Tool descriptions, many dialogs/status messages, notifications, and AI-generated answers are not yet fully localized. The AI response layer does not yet receive a locale instruction. These remain explicit follow-up work.
- **Build/test status:** no successful CI result is claimed in this document until the new commit's workflow has completed. A clean build and tests must validate the new Java class and Activity changes before another APK is offered for installation.


## Localization catalog — P-003 implementation progress

- Added `localization/PAMELA_STRING_CATALOG.txt` as a draft inventory extracted from the current Java sources. It records source text, originating file, locale columns (`es-419`, `en`, `pt-BR`), and review status.
- The initial extraction is intentionally marked `[PENDING]`; it includes candidate strings from UI, tool descriptions, and assistant logic. It is an inventory for human review, not yet a certified list of user-visible strings. Some entries may be internal prompts, technical diagnostics, or duplicates with different contexts.
- The catalog is not loaded by the APK at runtime. Android runtime localization still needs a reviewed migration into Android resources or a generated resource layer. Do not edit the catalog and assume the running app has changed.
- Updated locale resolution: manual Spanish selection is stored as `es-419`; automatic device Spanish resolves to `es-419`, Portuguese to `pt-BR`, English to `en`, and unsupported locales fall back to `es-419`. Existing legacy `es` preference values remain compatible.
- The language selector now exposes Latin American Spanish explicitly. This is a partial i18n foundation, not full localization: hard-coded strings in dialogs, notifications, diagnostics, tool outputs, and assistant-generated responses still require systematic migration and verification.
- Next required checks: validate catalog entries and stable identifiers, complete the translation review, migrate UI strings into runtime resources, pass the selected locale into the assistant response instructions, then run CI and device regression tests.
