# Pamela permanent Android signing

This repository supports two signing modes:

- `development` (default): uses a disposable/local development keystore. Never distribute these builds as stable updates.
- `release`: requires an explicitly supplied permanent keystore and passwords. It fails closed if any input is missing.

## 1. Generate the permanent key on the owner's Windows PC

Run PowerShell from a trusted Windows account with a JDK installed:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.scriptswindowscreate-pamela-release-key.ps1
```

The script creates:

- `%USERPROFILE%\PamelaSigning\pamela-release.jks` — private signing key.
- `%USERPROFILE%\PamelaSigning\PAMELA_KEYSTORE_BASE64.txt` — Base64 representation for a GitHub Actions secret.

Keytool interactively requests the keystore and private-key passwords. Use strong, unique passwords and keep them in a password manager. The alias is `pamela-release`.

**Never commit, upload as a normal workflow artifact, or send the JKS or passwords in chat.** Keep at least two encrypted offline backups of the JKS. Loss of this file means future in-place Android updates cannot use this signing identity.

Record the SHA-256 certificate fingerprint shown by keytool. It is public certificate information, not the private key.

## 2. Add repository Actions secrets

In GitHub, open Settings → Secrets and variables → Actions → New repository secret. Add:

| Secret name | Value |
|---|---|
| `PAMELA_KEYSTORE_BASE64` | Entire one-line content of `PAMELA_KEYSTORE_BASE64.txt` |
| `PAMELA_STORE_PASSWORD` | Keystore password |
| `PAMELA_KEY_ALIAS` | `pamela-release` |
| `PAMELA_KEY_PASSWORD` | Private-key password |

Do not put these values in source files, workflow YAML, issues, or logs. The release workflow only reads them as Actions secrets.

## 3. Create a signed release

The workflow is `.github/workflows/pamela-release.yml`. It builds using release mode, verifies the APK signature, records the certificate fingerprint and SHA-256, and publishes a GitHub Release.

It can also be run manually from the Actions tab. Select this workflow, use source ref `feature/pamela-samsung-flowbox`, and provide a new version tag such as `v0.3.0`. The workflow creates the tag/release if it does not exist.

Do not run a release until all four secrets have been added and the app's versionCode/versionName have been deliberately updated for that release.

## 4. Local release build

For a trusted local environment, set:

- `PAMELA_SIGNING_MODE=release`
- `PAMELA_KEYSTORE_PATH`
- `PAMELA_KEY_ALIAS`
- `PAMELA_STORE_PASSWORD`
- `PAMELA_KEY_PASSWORD`

Then run `bash build.sh`. Missing release settings cause the build to stop; there is no silent fallback to the development key.

## Important

A keystore is not a Google Play developer registration. Direct APK distribution and Android's package-installer confirmation do not require a Play Console account. Android's separate developer-verification requirements may apply to broader off-Play distribution as those rules roll out; evaluate that before public distribution.

The signing identity is permanent operational infrastructure. Keep the encrypted backups, passwords, and fingerprint together in a secure recovery plan, but never store the private key in this repository.
