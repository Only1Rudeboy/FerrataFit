#!/bin/bash
# FerrataFit neu bauen.
#
# Aufruf aus WSL:  bash /mnt/c/Users/Rudeboy/Documents/FerrataFit/build.sh
#
# Der Quellcode liegt hier in Documents, die Zwischenergebnisse landen bewusst auf dem
# Linux-Dateisystem — Gradle-Builds direkt auf /mnt/c sind um ein Vielfaches langsamer.
set -e

PROJECT="/mnt/c/Users/Rudeboy/Documents/FerrataFit"
export JAVA_HOME="$HOME/android/jdk"
export ANDROID_HOME="$HOME/android/sdk"
export FERRATAFIT_BUILD_DIR="$HOME/.ferratafit-build"
GRADLE="$HOME/android/tools/gradle-8.11.1/bin/gradle"

cd "$PROJECT"
"$GRADLE" :app:assembleRelease "$@"

APK="$FERRATAFIT_BUILD_DIR/app/outputs/apk/release/app-release.apk"
if [ -f "$APK" ]; then
  cp "$APK" "$PROJECT/FerrataFit.apk"
  echo
  echo "Fertig: $PROJECT/FerrataFit.apk"
  ls -lh "$PROJECT/FerrataFit.apk" | awk '{print "Größe: " $5}'
else
  echo "APK nicht gefunden unter $APK" >&2
  exit 1
fi
