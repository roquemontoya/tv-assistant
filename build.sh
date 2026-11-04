#!/bin/bash
set -euo pipefail
app_root="$(cd "$(dirname "$0")" && pwd)"
workspace_root="$app_root"
app_sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
app_tools="$app_sdk/build-tools/36.0.0"
app_android="$app_sdk/platforms/android-36/android.jar"
app_build="$workspace_root/build"
mkdir -p "$app_build" "$workspace_root/dist"
python3 "$app_root/fetch-dependencies.py"
rm -rf "$app_build/classes" "$app_build/dex"
mkdir -p "$app_build/classes" "$app_build/dex"
"$app_tools/aapt2" compile --dir "$app_root/res" -o "$app_build/resources.zip"
"$app_tools/aapt2" link -o "$app_build/unsigned.apk" -I "$app_android" --manifest "$app_root/AndroidManifest.xml" "$app_build/resources.zip"
find "$app_root/src" -name '*.java' > "$app_build/sources.txt"
javac --release 8 -classpath "$app_android" -d "$app_build/classes" @"$app_build/sources.txt"
jar cf "$app_build/classes.jar" -C "$app_build/classes" .
"$app_tools/d8" --min-api 23 --lib "$app_android" --output "$app_build/dex" "$app_build/classes.jar"
python3 - "$app_build/unsigned.apk" "$app_build/dex/classes.dex" "$app_root/THIRD_PARTY_NOTICES.md" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1], 'a', compression=zipfile.ZIP_DEFLATED) as archive:
    archive.write(sys.argv[2], 'classes.dex')
    archive.write(sys.argv[3], 'assets/THIRD_PARTY_NOTICES.md')
PY
SIGNING_MODE="${PAMELA_SIGNING_MODE:-development}"
"$app_tools/zipalign" -f 4 "$app_build/unsigned.apk" "$app_build/aligned.apk"

if [ "$SIGNING_MODE" = "release" ]; then
  : "${PAMELA_KEYSTORE_PATH:?PAMELA_KEYSTORE_PATH is required for release signing}"
  : "${PAMELA_KEY_ALIAS:?PAMELA_KEY_ALIAS is required for release signing}"
  : "${PAMELA_STORE_PASSWORD:?PAMELA_STORE_PASSWORD is required for release signing}"
  : "${PAMELA_KEY_PASSWORD:?PAMELA_KEY_PASSWORD is required for release signing}"
  if [ ! -f "$PAMELA_KEYSTORE_PATH" ]; then
    echo "Release keystore not found: $PAMELA_KEYSTORE_PATH" >&2
    exit 1
  fi
  "$app_tools/apksigner" sign --ks "$PAMELA_KEYSTORE_PATH" --ks-key-alias "$PAMELA_KEY_ALIAS" \
    --ks-pass "env:PAMELA_STORE_PASSWORD" --key-pass "env:PAMELA_KEY_PASSWORD" \
    --out "$workspace_root/dist/tv-assistant.apk" "$app_build/aligned.apk"
elif [ "$SIGNING_MODE" = "development" ]; then
  if [ ! -f "$app_build/debug.keystore" ]; then
    keytool -genkeypair -keystore "$app_build/debug.keystore" -storepass android -keypass android -alias tvassistant -keyalg RSA -keysize 2048 -validity 3650 -dname 'CN=TV Assistant Development'
  fi
  "$app_tools/apksigner" sign --ks "$app_build/debug.keystore" --ks-key-alias tvassistant \
    --ks-pass pass:android --key-pass pass:android --out "$workspace_root/dist/tv-assistant.apk" "$app_build/aligned.apk"
else
  echo "Unknown PAMELA_SIGNING_MODE: $SIGNING_MODE" >&2
  exit 1
fi
"$app_tools/apksigner" verify "$workspace_root/dist/tv-assistant.apk"
printf 'Built %s\n' "$workspace_root/dist/tv-assistant.apk"
