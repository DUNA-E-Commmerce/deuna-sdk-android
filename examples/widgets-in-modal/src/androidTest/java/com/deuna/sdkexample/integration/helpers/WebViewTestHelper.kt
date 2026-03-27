package com.deuna.sdkexample.integration.helpers

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import kotlin.math.max

/**
 * Helper class for interacting with WebView elements during UI tests.
 * Uses UI Automator with accessibility nodes to interact with WebView content.
 */
class WebViewTestHelper(private val device: UiDevice) {

    companion object {
        private const val TAG = "WebViewTestHelper"
    }

    /**
     * Fills a text field by finding the label and then the closest EditText below it.
     * @param text The text to enter into the field.
     * @param label The label text to find.
     * @param timeout Timeout in milliseconds.
     */
    fun fillTextFieldByLabel(
        text: String,
        label: String,
        timeout: Long = 10000
    ): Boolean {
        Log.d(TAG, "🔍 Looking for field with label: $label")

        val labelElement = device.wait(Until.findObject(By.textContains(label)), timeout)
            ?: return false.also { Log.w(TAG, "⚠️ Label not found: $label") }

        // Find the EditText closest below the label using coordinates
        val labelBottom = labelElement.visibleBounds.bottom
        val labelCenterX = labelElement.visibleBounds.centerX()

        val editText = device.findObjects(By.clazz("android.widget.EditText"))
            .filter { it.visibleBounds.top >= labelBottom - 50 }
            .minByOrNull { editText ->
                val verticalDist = editText.visibleBounds.top - labelBottom
                val horizontalDist = kotlin.math.abs(editText.visibleBounds.centerX() - labelCenterX)
                verticalDist + horizontalDist / 2
            }
            ?: return false.also { Log.w(TAG, "⚠️ EditText not found for label: $label") }

        Log.d(TAG, "✅ Found EditText for '$label'")
        return enterText(editText, text)
    }

    private fun enterText(element: UiObject2, text: String): Boolean {
        return try {
            element.click()
            Thread.sleep(200)
            element.clear()
            element.text = text
            Log.d(TAG, "✅ Entered: $text")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed: ${e.message}")
            false
        }
    }

    /**
     * Taps a button that contains specific label text.
     * @param label The button label to find.
     * @param timeout Timeout in milliseconds.
     */
    fun buttonTap(label: String, timeout: Long = 5000): Boolean {
        val deadline = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < deadline) {
            val remaining = max(250L, deadline - System.currentTimeMillis())
            device.wait(Until.findObject(By.textContains(label)), minOf(1000L, remaining))

            val textMatches = device.findObjects(By.textContains(label))
            val descMatches = device.findObjects(By.descContains(label))
            val candidates = (textMatches + descMatches)
                .distinctBy { it.hashCode() }
                .sortedByDescending { scoreCandidate(it) }

            candidates.firstNotNullOfOrNull { candidate ->
                clickCandidate(candidate, label)
            }?.let { return true }

            Thread.sleep(200)
        }

        Log.w(TAG, "⚠️ Button not found: $label")
        return false
    }

    private fun scoreCandidate(candidate: UiObject2): Int {
        var score = 0
        if (candidate.isClickable) score += 4
        if (candidate.className == "android.widget.Button") score += 3
        if (candidate.className == "android.view.View") score += 1
        if (candidate.visibleBounds.height() > 36) score += 1
        return score
    }

    private fun clickCandidate(candidate: UiObject2, label: String): Boolean? {
        return try {
            if (candidate.isClickable) {
                candidate.click()
                Log.d(TAG, "✅ Tapped: $label")
                true
            } else {
                var parent = candidate.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        parent.click()
                        Log.d(TAG, "✅ Tapped via parent: $label")
                        return true
                    }
                    parent = parent.parent
                }
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed tapping candidate for '$label': ${e.message}")
            null
        }
    }

    /**
     * Completes Stripe 3DS challenge by tapping COMPLETE in the challenge page.
     * Returns true when the challenge view was detected and completed.
     */
    fun completeStripe3dsChallenge(timeout: Long = 30000): Boolean {
        val challengeDetected =
            device.wait(Until.findObject(By.textContains("3D Secure")), timeout / 2) != null ||
                device.wait(Until.findObject(By.textContains("Complete a required action")), timeout / 2) != null

        if (!challengeDetected) {
            Log.w(TAG, "⚠️ Stripe 3DS challenge was not detected")
            return false
        }

        repeat(6) { _ ->
            val clicked = buttonTap("COMPLETE", timeout = 1500) ||
                buttonTap("Complete", timeout = 1500) ||
                buttonTap("AUTHORIZE", timeout = 1500) ||
                buttonTap("Authorize", timeout = 1500)
            if (clicked) {
                Log.d(TAG, "✅ Completed Stripe 3DS challenge")
                return true
            }
            swipeUp()
            Thread.sleep(400)
        }

        Log.w(TAG, "⚠️ Could not tap COMPLETE on Stripe 3DS challenge")
        return false
    }

    /**
     * Performs a swipe up gesture on the screen.
     */
    fun swipeUp() {
        val displayHeight = device.displayHeight
        val displayWidth = device.displayWidth

        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            10
        )
        Log.d(TAG, "✅ Performed swipe up")
    }

    /**
     * Dismisses the keyboard if it's visible.
     */
    fun dismissKeyboard() {
        device.pressBack()
        Log.d(TAG, "✅ Dismissed keyboard")
    }

    /**
     * Waits for a WebView to be present on screen.
     * @param timeout Timeout in milliseconds.
     * @return true if WebView is found, false otherwise.
     */
    fun waitForWebView(timeout: Long = 15000): Boolean {
        val webView = device.findObject(
            UiSelector().className("android.webkit.WebView")
        )
        val exists = webView.waitForExists(timeout)
        if (exists) {
            Log.d(TAG, "✅ WebView found")
        } else {
            Log.w(TAG, "⚠️ WebView not found within timeout")
        }
        return exists
    }
}
