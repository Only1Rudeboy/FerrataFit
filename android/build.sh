#!/bin/bash
# FerrataFit für Android bauen.
#
# Aufruf:  bash android/build.sh
#
# Das Skript findet seinen eigenen Ordner selbst und läuft daher von jedem Ort aus.
# Die Zwischenergebnisse landen bewusst auf dem Linux-Dateisystem — unter WSL sind
# Gradle-Builds direkt auf /mnt/c um ein Vielfaches langsamer.
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
