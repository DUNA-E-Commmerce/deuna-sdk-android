#!/usr/bin/env bash
set -euo pipefail

mkdir -p artifacts/test-videos
STOP_FILE="/tmp/stop_screenrecord_segments"

adb wait-for-device
rm -f "${STOP_FILE}"
adb shell rm -f /sdcard/integration-tests-*.mp4 || true

# Normalize emulator viewport to avoid tiny-screen layout differences in CI.
adb shell wm size 1080x2400 || true
adb shell wm density 420 || true

# Record the emulator screen in rolling segments to avoid the hard time limit.
(
  segment=1
  while true; do
    remote_path="/sdcard/integration-tests-${segment}.mp4"
    adb shell rm -f "${remote_path}" || true
    adb shell "screenrecord --time-limit 170 --bit-rate 6000000 ${remote_path}" >/dev/null 2>&1 || true
    if [[ -f "${STOP_FILE}" ]]; then
      break
    fi
    segment=$((segment + 1))
  done
) &
screenrecord_loop_pid=$!

set +e
./gradlew :widgets-in-modal:connectedAndroidTest
test_exit_code=$?
set -e

touch "${STOP_FILE}"
adb shell pkill -INT screenrecord >/dev/null 2>&1 || true
wait "${screenrecord_loop_pid}" || true

for segment in $(seq 1 30); do
  remote_path="/sdcard/integration-tests-${segment}.mp4"
  local_path="artifacts/test-videos/integration-tests-${segment}.mp4"
  adb pull "${remote_path}" "${local_path}" >/dev/null 2>&1 || true
  adb shell rm -f "${remote_path}" || true
done

exit "${test_exit_code}"
