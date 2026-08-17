#!/bin/bash
# FerrataFit für Android bauen.
#
# Aufruf aus WSL:  bash /mnt/c/Users/Rudeboy/Documents/FerrataFit/android/build.sh
#
# Der Quellcode liegt in Documents, die Zwischenergebnisse landen bewusst auf dem
# Linux-Dateisystem — Gradle-Builds direkt auf /mnt/c sind um ein Vielfaches langsamer.
set -e

ANDROID_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(dirname "$ANDROID_DIR")"

export JAVA_HOME="$HOME/android/jdk"
export ANDROID_HOME="$HOME/android/sdk"
export FERRATAFIT_BUILD_DIR="$HOME/.ferratafit-build"
GRADLE="$HOME/android/tools/gradle-8.11.1/bin/gradle"

cd "$ANDROID_DIR"
"$GRADLE" :app:assembleRelease "$@"

APK="$FERRATAFIT_BUILD_DIR/app/outputs/apk/release/app-release.apk"
if [ -f "$APK" ]; then
  cp "$APK" "$ROOT/FerrataFit.apk"
  echo
  echo "Fertig: $ROOT/FerrataFit.apk"
  ls -lh "$ROOT/FerrataFit.apk" | awk '{print "Größe: " $5}'
else
  echo "APK nicht gefunden unter $APK" >&2
  exit 1
fi
