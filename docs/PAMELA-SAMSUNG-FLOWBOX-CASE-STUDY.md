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
