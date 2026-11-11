#!/usr/bin/env bash
# Runs the instrumented smoke tests on the running emulator and collects screenshots and logs.
# Fails if any test fails or the app crashed.
set -euo pipefail

PKG=dynoapps.exchange_rates.debug
OUT=emulator-output
mkdir -p "$OUT"

APP_APK=$(ls app/build/outputs/apk/debug/*.apk | head -n 1)
TEST_APK=$(ls app/build/outputs/apk/androidTest/debug/*.apk | head -n 1)

# -g grants runtime permissions, so the notification permission dialog doesn't cover the screens.
adb install -r -g "$APP_APK"
adb install -r "$TEST_APK"
adb logcat -c

set +e
adb shell am instrument -w "$PKG.test/androidx.test.runner.AndroidJUnitRunner" | tee "$OUT/instrumentation.txt"
set -e

adb pull "/sdcard/Android/data/$PKG/files/screenshots" "$OUT/" || echo "No screenshots found"
adb logcat -d > "$OUT/logcat.txt"
adb logcat -d -b crash > "$OUT/crash.txt" || true

echo "::group::Crash log"
cat "$OUT/crash.txt"
echo "::endgroup::"

status=0
if grep -q "FATAL EXCEPTION" "$OUT/crash.txt"; then
  echo "::error::The app crashed on the emulator; see the crash log above."
  status=1
fi
# am instrument exits 0 even when tests fail, so check its summary line.
if ! grep -q "^OK (" "$OUT/instrumentation.txt"; then
  echo "::error::Instrumented tests failed; see the output above."
  status=1
fi
exit $status
