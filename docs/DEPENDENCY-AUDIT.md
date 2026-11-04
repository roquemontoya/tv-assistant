# Dependency security checks

The repository has an automated dependency audit in `.github/workflows/pamela-checks.yml`.

## What it checks

- `dependencies.json`: third-party libraries packaged into the APK.
- `host-dependencies.json`: libraries used only by local/CI tests and build helpers.
- Every declared JAR must have a safe filename, an HTTPS source URL and a 64-character SHA-256 pin.
- Maven coordinates are derived from Maven Central URLs. For another Maven host or a nonstandard URL, declare `ecosystem: "Maven"`, `name: "group:artifact"` and `version` explicitly. Unknown coordinates fail closed rather than being silently skipped.
- Versions are queried against the OSV vulnerability database. A known vulnerability makes the audit job fail and prints the advisory IDs and references.

The audit does not automatically upgrade dependencies. A failing result requires reviewing the advisory, checking whether the affected code path is used, selecting a fixed version or mitigation, updating the pinned checksum and notices, and rerunning the build/tests.

## When it runs

- Every push to any branch.
- Pull requests targeting `main`.
- Manually through GitHub Actions.
- Weekly on Monday at 08:17 UTC. GitHub scheduled workflows run from the repository's default branch, so the weekly schedule becomes active after this workflow is merged into that default branch. Push and pull-request checks work on this feature branch already.

The second CI job builds the development APK with Android SDK platform 36/build-tools 36.0.0 and runs `test-local.sh`. It does not install the APK on a TV or emulator and does not run the device test script.

## Scope and limitations

This is a direct, manifest-based Maven dependency audit, not a complete security certification. It does not detect vulnerabilities in Android/Fire OS firmware, the host operating system/JDK, unlisted libraries, arbitrary source code, or remote services. The actual Flowbox firmware/TLS provider and the JDK on the developer workstation must be checked separately. Keep dependency manifests complete and SHA-256 pins current.

At present, `dependencies.json` is empty because Java-WebSocket was removed; `host-dependencies.json` still declares the checksum-pinned `org.json:json:20240303` test dependency.
