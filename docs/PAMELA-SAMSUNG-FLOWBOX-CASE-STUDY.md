# Pamela: Samsung + Flowbox F1 + LG TV

## Purpose

This document records an exploratory compatibility and integration study based on the upstream **TV Assistant** project. The goal is to evaluate whether a Samsung phone can provide voice input while a Flowbox F1 connected to an LG television runs the TV interface and displays responses.

This is a test plan, not a claim that the integration already works. No APK has been built or installed as part of this initial setup.

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

Exact device models, Android/API versions, CPU ABI, network topology, and audio routing must be recorded from device diagnostics before selecting a build or implementation.

## Work plan

1. Record the upstream commit and inspect the complete build configuration and dependency sources.
2. Audit permissions, authentication/token handling, network transport, WebView/browser behavior, accessibility actions, screen capture, storage, and tool-execution approvals.
3. Determine the Flowbox CPU ABI and Android compatibility; do not assume that Android version alone establishes APK compatibility.
4. Build from a pinned source revision in a reproducible environment; record toolchain versions and artifact hashes.
5. Run static checks and available unit/emulator tests before any physical-device installation.
6. Evaluate a phone-side voice-to-text path and a narrowly scoped local-network relay. Do not reuse ChatGPT subscription credentials as an API, scrape private app tokens, or upload audio through unsupported plan-sharing interfaces.
7. Test on the Flowbox only after the APK, permissions, and rollback procedure have been reviewed.
8. Publish measured results, limitations, and any patches; offer useful changes upstream through a clearly attributed pull request when appropriate.

## Evidence and status

- Fork created under `roquemontoya/tv-assistant`.
- Working branch: `feature/pamela-samsung-flowbox`.
- Initial status: planning and source audit pending.
- No compatibility result, successful build, APK, or working remote-microphone integration is claimed yet.

## Change log

- Initial case-study document added to establish attribution, scope, and a testable plan before changing application code.
