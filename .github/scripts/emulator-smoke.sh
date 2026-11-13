#!/usr/bin/env bash
# Runs on the CI emulator:
#  1. launches the debug build and counts the rates shown on the landing screen,
#  2. runs the instrumented tests (including the rate sources report),
#  3. installs the minified (R8) production build signed with the debug key, launches it and
#     compares the rates it shows with the debug build.
# Fails if a test fails, the app crashes, or the minified build shows no rates while the debug
# build does (a sign that shrinking broke networking or parsing).
set -euo pipefail

DEBUG_PKG=dynoapps.exchange_rates.debug
PROD_PKG=dynoapps.exchange_rates
ACTIVITY=dynoapps.exchange_rates.LandingActivity
OUT=emulator-output
mkdir -p "$OUT"
status=0

# Launches the landing screen of $1, waits for rates, saves a screenshot and prints how many rate
# values are on screen.
count_rates_on_landing() {
  local pkg=$1 name=$2
  adb shell am force-stop "$pkg"
  adb shell am start -W -n "$pkg/$ACTIVITY" > /dev/null
  sleep 20
  adb exec-out screencap -p > "$OUT/$name.png" || true
  adb shell uiautomator dump /sdcard/ui.xml > /dev/null
  adb pull /sdcard/ui.xml "$OUT/$name-ui.xml" > /dev/null
  adb shell am force-stop "$pkg"
  # Rate values look like "41,85 TL" or "1.234,56 $".
  { grep -oE 'text="[0-9][0-9.,]* (TL|\$)"' "$OUT/$name-ui.xml" || true; } | wc -l | tr -d ' '
}

APP_APK=$(ls app/build/outputs/apk/debug/*.apk | head -n 1)
TEST_APK=$(ls app/build/outputs/apk/androidTest/debug/*.apk | head -n 1)
PROD_UNSIGNED=$(ls app/build/outputs/apk/production/*.apk | head -n 1)

# -g grants runtime permissions, so the notification permission dialog doesn't cover the screens.
adb install -r -g "$APP_APK"
adb install -r "$TEST_APK"
adb logcat -c

echo "::group::Debug build"
debug_rates=$(count_rates_on_landing "$DEBUG_PKG" landing-debug)
echo "Rates shown by the debug build: $debug_rates"
echo "::endgroup::"

echo "::group::Instrumented tests"
set +e
adb shell am instrument -w "$DEBUG_PKG.test/androidx.test.runner.AndroidJUnitRunner" | tee "$OUT/instrumentation.txt"
set -e
echo "::endgroup::"
# am instrument exits 0 even when tests fail, so check its summary line.
if ! grep -q "^OK (" "$OUT/instrumentation.txt"; then
  echo "::error::Instrumented tests failed; see the output above."
  status=1
fi

adb pull "/sdcard/Android/data/$DEBUG_PKG/files/screenshots" "$OUT/" || echo "No screenshots found"
adb pull "/sdcard/Android/data/$DEBUG_PKG/files/sources-report.txt" "$OUT/" || true
echo "::group::Rate sources report"
cat "$OUT/sources-report.txt" 2>/dev/null || echo "No sources report"
echo "::endgroup::"

echo "::group::Minified production build"
BUILD_TOOLS=$(ls -d "$ANDROID_HOME"/build-tools/* | sort -V | tail -n 1)
"$BUILD_TOOLS/zipalign" -f -p 4 "$PROD_UNSIGNED" "$OUT/production-aligned.apk"
"$BUILD_TOOLS/apksigner" sign --ks signing/debug.keystore --ks-pass pass:android \
  --ks-key-alias androiddebugkey --key-pass pass:android \
  --out "$OUT/production-debugsigned.apk" "$OUT/production-aligned.apk"
adb install -r -g "$OUT/production-debugsigned.apk"
prod_rates=$(count_rates_on_landing "$PROD_PKG" landing-production)
echo "Rates shown by the minified build: $prod_rates"
echo "::endgroup::"
rm -f "$OUT/production-aligned.apk" "$OUT/production-debugsigned.apk"

if [ "$debug_rates" -gt 0 ] && [ "$prod_rates" -eq 0 ]; then
  echo "::error::The minified build shows no rates while the debug build shows $debug_rates. R8 probably broke networking or parsing; check the logcat."
  status=1
elif [ "$debug_rates" -eq 0 ]; then
  echo "::warning::Neither build showed rates; the default sources may be down. See the rate sources report."
fi

adb logcat -d > "$OUT/logcat.txt"
adb logcat -d -b crash > "$OUT/crash.txt" || true
echo "::group::Crash log"
cat "$OUT/crash.txt"
echo "::endgroup::"
if grep -q "FATAL EXCEPTION" "$OUT/crash.txt"; then
  echo "::error::The app crashed on the emulator; see the crash log above."
  status=1
fi

exit $status
