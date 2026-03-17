#!/bin/bash
set -e

echo "🚀 Starting DEUNA Android SDK Integration Tests"
echo "================================================"

# Configuration
AVD_NAME="test_avd"
EMULATOR_WAIT_TIME=300  # 5 minutes max wait
ADB_PORT=5037
EMULATOR_PORT=5554

# Function to check if emulator is ready
wait_for_emulator() {
    echo "⏳ Waiting for emulator to be ready..."
    local timeout=$EMULATOR_WAIT_TIME
    local elapsed=0

    while [ $elapsed -lt $timeout ]; do
        # Check if device is online
        if adb devices | grep -q "emulator-$EMULATOR_PORT.*device"; then
            # Check if boot is completed
            local boot_completed=$(adb -s emulator-$EMULATOR_PORT shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
            if [ "$boot_completed" = "1" ]; then
                echo "✅ Emulator is ready!"
                return 0
            fi
        fi

        echo "   Still waiting... ($elapsed/$timeout seconds)"
        sleep 5
        elapsed=$((elapsed + 5))
    done

    echo "❌ Timeout waiting for emulator to be ready"
    return 1
}

# Function to cleanup
cleanup() {
    echo "🧹 Cleaning up..."
    if [ -n "$EMULATOR_PID" ]; then
        echo "   Killing emulator (PID: $EMULATOR_PID)"
        kill $EMULATOR_PID 2>/dev/null || true
    fi
    adb kill-server 2>/dev/null || true
}

# Set trap to cleanup on exit
trap cleanup EXIT INT TERM

# Start ADB server
echo "🔧 Starting ADB server..."
adb start-server

# Start emulator in background
echo "📱 Starting Android emulator..."

# Check if KVM is available
if [ -e /dev/kvm ]; then
    echo "   ✅ KVM detected - using hardware acceleration"
    KVM_ACCEL=""
else
    echo "   ⚠️  KVM not available - using software acceleration (slower)"
    KVM_ACCEL="-accel off"
fi

emulator \
    -avd $AVD_NAME \
    -no-window \
    -gpu swiftshader_indirect \
    -noaudio \
    -no-boot-anim \
    -camera-back none \
    -no-snapshot-save \
    -memory 2048 \
    -partition-size 4096 \
    -port $EMULATOR_PORT \
    $KVM_ACCEL \
    > /tmp/emulator.log 2>&1 &

EMULATOR_PID=$!
echo "   Emulator started with PID: $EMULATOR_PID"

# Wait for emulator to be ready
if ! wait_for_emulator; then
    echo "❌ Failed to start emulator"
    echo "📋 Emulator logs:"
    cat /tmp/emulator.log
    exit 1
fi

# Disable animations
echo "🎨 Disabling animations..."
adb -s emulator-$EMULATOR_PORT shell settings put global window_animation_scale 0
adb -s emulator-$EMULATOR_PORT shell settings put global transition_animation_scale 0
adb -s emulator-$EMULATOR_PORT shell settings put global animator_duration_scale 0

# Grant permissions
echo "🔓 Granting permissions..."
adb -s emulator-$EMULATOR_PORT shell pm grant com.deuna.sdkexample android.permission.INTERNET || true
adb -s emulator-$EMULATOR_PORT shell pm grant com.deuna.sdkexample android.permission.ACCESS_NETWORK_STATE || true

# Display environment info
echo ""
echo "📊 Environment Information:"
echo "   DEUNA_API_ENDPOINT: ${DEUNA_API_ENDPOINT:-not set}"
echo "   DEUNA_ENV: ${DEUNA_ENV:-not set}"
echo "   ADMIN_USERNAME: ${ADMIN_USERNAME:-not set}"
echo ""

# Run tests
echo "🧪 Running integration tests..."
echo "================================================"

cd /app
./gradlew :widgets-in-modal:connectedAndroidTest \
    --no-daemon \
    --stacktrace \
    --info

TEST_EXIT_CODE=$?

# Copy test results
echo ""
echo "📄 Copying test results..."
if [ -d "examples/widgets-in-modal/build/reports/androidTests" ]; then
    mkdir -p /test-results
    cp -r examples/widgets-in-modal/build/reports/androidTests/* /test-results/ 2>/dev/null || true
    echo "   Test results copied to /test-results/"
else
    echo "   ⚠️  No test results found"
fi

# Copy test artifacts
if [ -d "examples/widgets-in-modal/build/outputs/androidTest-results" ]; then
    mkdir -p /test-results/artifacts
    cp -r examples/widgets-in-modal/build/outputs/androidTest-results/* /test-results/artifacts/ 2>/dev/null || true
    echo "   Test artifacts copied to /test-results/artifacts/"
fi

# Print summary
echo ""
echo "================================================"
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Tests failed with exit code: $TEST_EXIT_CODE"
    echo ""
    echo "📋 Check test results in /test-results/ for details"
fi
echo "================================================"

exit $TEST_EXIT_CODE
