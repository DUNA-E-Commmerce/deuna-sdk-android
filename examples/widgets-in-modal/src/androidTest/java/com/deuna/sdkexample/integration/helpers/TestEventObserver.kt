package com.deuna.sdkexample.integration.helpers

import android.util.Log
import com.deuna.sdkexample.testing.EventWaiter
import com.deuna.sdkexample.testing.TestEvent
import com.deuna.sdkexample.testing.TestEventBroadcaster

/**
 * Helper object for waiting on test events broadcast from the app.
 * Similar to iOS TestNotificationObserver.
 */
object TestEventObserver {

    private const val TAG = "TestEventObserver"

    /**
     * Creates a waiter for a specific test event.
     * Call this BEFORE the action that triggers the event.
     * @param event The test event to wait for.
     * @return An EventWaiter that can be used to wait for the event.
     */
    fun createWaiter(event: TestEvent): EventWaiter {
        Log.d(TAG, "📝 Creating waiter for event: ${event.name}")
        return TestEventBroadcaster.createWaiter(event)
    }

    /**
     * Waits for an event using the provided waiter.
     * @param waiter The EventWaiter created by createWaiter.
     * @param timeoutSeconds The maximum time to wait in seconds.
     * @return true if the event was received, false if timeout occurred.
     */
    fun waitFor(waiter: EventWaiter, timeoutSeconds: Long = 15): Boolean {
        Log.d(TAG, "⏳ Waiting for event: ${waiter.event.name} (timeout: ${timeoutSeconds}s)")

        val received = waiter.await(timeoutSeconds)

        if (received) {
            Log.d(TAG, "✅ Received event: ${waiter.event.name}")
        } else {
            Log.w(TAG, "⚠️ Timeout waiting for event: ${waiter.event.name}")
            TestEventBroadcaster.removeWaiter(waiter)
        }

        return received
    }

    /**
     * Clears all registered waiters. Call this in test teardown if needed.
     */
    fun clear() {
        TestEventBroadcaster.clear()
    }
}
