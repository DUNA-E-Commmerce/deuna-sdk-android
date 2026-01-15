package com.deuna.sdkexample.integration.helpers

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until

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
        val element = device.wait(Until.findObject(By.textContains(label)), timeout)
            ?: device.wait(Until.findObject(By.descContains(label)), timeout / 2)

        return if (element != null) {
            element.click()
            Log.d(TAG, "✅ Tapped: $label")
            true
        } else {
            Log.w(TAG, "⚠️ Button not found: $label")
            false
        }
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
