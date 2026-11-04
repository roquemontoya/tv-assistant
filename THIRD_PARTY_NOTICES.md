# Runtime dependency notices

The APK currently bundles no third-party runtime JARs.

The Java-WebSocket 1.6.0 and SLF4J 2.0.13 runtime dependencies were removed while
CVE-2026-90584 remains unresolved upstream. The app uses Android HTTPS/SSE transport
until a fixed and independently verified WebSocket release is available.

The org.json host-test dependency is used only for offline tests and is not bundled
in the APK. Its notice is maintained with the host-test dependency metadata.
