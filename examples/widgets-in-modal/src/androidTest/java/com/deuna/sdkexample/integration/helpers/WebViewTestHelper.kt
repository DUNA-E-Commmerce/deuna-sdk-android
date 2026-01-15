package com.deuna.sdkexample.integration.helpers

import android.util.Log
import android.view.KeyEvent
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
     * Fills a text field by finding it via placeholder/hint text.
     * Works with WebView by using accessibility nodes.
     * @param text The text to enter into the field.
     * @param placeholderContains List of possible placeholder texts to match.
     * @param timeout Timeout in milliseconds to wait for the element.
     */
    fun fillTextField(
        text: String,
        placeholderContains: List<String>,
        timeout: Long = 10000
    ): Boolean {
        Log.d(TAG, "🔍 Looking for field with placeholders: $placeholderContains")

        // First, log all available elements for debugging
        logAvailableElements()

        for (placeholder in placeholderContains) {
            // Try using By selector with text contains (works better with WebView)
            var element = device.wait(
                Until.findObject(By.textContains(placeholder)),
                timeout
            )

            if (element != null) {
                Log.d(TAG, "✅ Found element with text containing: $placeholder")
                return enterTextInElement(element, text)
            }

            // Try with description
            element = device.wait(
                Until.findObject(By.descContains(placeholder)),
                timeout / 2
            )

            if (element != null) {
                Log.d(TAG, "✅ Found element with description containing: $placeholder")
                return enterTextInElement(element, text)
            }

        }

        Log.w(TAG, "⚠️ Could not find field with placeholders: $placeholderContains")
        return false
    }

    /**
     * Fills a text field by finding the label and then the EditText below it.
     * Uses visual coordinates to find the correct field.
     * @param text The text to enter into the field.
     * @param labelContains List of possible label texts to match.
     * @param timeout Timeout in milliseconds to wait for the element.
     */
    fun fillTextFieldByLabel(
        text: String,
        labelContains: List<String>,
        timeout: Long = 10000
    ): Boolean {
        Log.d(TAG, "🔍 Looking for field with label: $labelContains")

        for (label in labelContains) {
            // Find the label element
            val labelElement = device.wait(
                Until.findObject(By.textContains(label)),
                timeout
            )

            if (labelElement != null) {
                Log.d(TAG, "✅ Found label: '$label' at y=${labelElement.visibleBounds.top}")

                // Get all EditText elements
                val allEditTexts = device.findObjects(By.clazz("android.widget.EditText"))
                Log.d(TAG, "Found ${allEditTexts.size} EditText elements")

                // Find the EditText that is closest below the label (by Y coordinate)
                val labelBottom = labelElement.visibleBounds.bottom
                val labelCenterX = labelElement.visibleBounds.centerX()

                var closestEditText: UiObject2? = null
                var closestDistance = Int.MAX_VALUE

                for (editText in allEditTexts) {
                    val editTextTop = editText.visibleBounds.top
                    val editTextCenterX = editText.visibleBounds.centerX()

                    // EditText must be below the label
                    if (editTextTop >= labelBottom - 50) {
                        val verticalDistance = editTextTop - labelBottom
                        val horizontalDistance = kotlin.math.abs(editTextCenterX - labelCenterX)

                        // Prefer EditTexts that are close vertically and horizontally aligned
                        val totalDistance = verticalDistance + (horizontalDistance / 2)

                        if (totalDistance < closestDistance) {
                            closestDistance = totalDistance
                            closestEditText = editText
                        }
                    }
                }

                if (closestEditText != null) {
                    Log.d(TAG, "✅ Found EditText below label '$label' at y=${closestEditText.visibleBounds.top}, distance=$closestDistance")
                    return enterTextInElement(closestEditText, text)
                }
            }
        }

        Log.w(TAG, "⚠️ Could not find field with label: $labelContains")
        return false
    }

    private fun enterTextInElement(element: UiObject2, text: String): Boolean {
        return try {
            element.click()
            Thread.sleep(300) // Wait for focus

            // Clear existing text
            element.clear()
            Thread.sleep(100)

            // Enter new text
            element.text = text
            Log.d(TAG, "✅ Entered text: $text")

            Thread.sleep(200) // Wait for input to register
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to enter text: ${e.message}")
            // Fallback: try using keyboard input
            try {
                element.click()
                Thread.sleep(300)
                device.pressKeyCode(KeyEvent.KEYCODE_MOVE_END)
                repeat(50) { device.pressKeyCode(KeyEvent.KEYCODE_DEL) }
                for (char in text) {
                    device.pressKeyCode(getKeyCode(char))
                }
                Log.d(TAG, "✅ Entered text via keyboard: $text")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "❌ Keyboard fallback also failed: ${e2.message}")
                false
            }
        }
    }

    private fun getKeyCode(char: Char): Int {
        return when (char) {
            in '0'..'9' -> KeyEvent.KEYCODE_0 + (char - '0')
            in 'a'..'z' -> KeyEvent.KEYCODE_A + (char - 'a')
            in 'A'..'Z' -> KeyEvent.KEYCODE_A + (char - 'A')
            ' ' -> KeyEvent.KEYCODE_SPACE
            else -> KeyEvent.KEYCODE_UNKNOWN
        }
    }

    /**
     * Taps a button that contains specific label text.
     * @param labelContains List of possible button labels to match.
     * @param timeout Timeout in milliseconds to wait for the element.
     */
    fun buttonTap(
        labelContains: List<String>,
        timeout: Long = 5000
    ): Boolean {
        for (label in labelContains) {
            // Try with text contains
            var element = device.wait(
                Until.findObject(By.textContains(label).clickable(true)),
                timeout
            )

            if (element != null) {
                Log.d(TAG, "✅ Found clickable element with text: $label")
                element.click()
                return true
            }

            // Try without clickable constraint (some WebView buttons aren't marked clickable)
            element = device.wait(
                Until.findObject(By.textContains(label)),
                timeout / 2
            )

            if (element != null) {
                Log.d(TAG, "✅ Found element with text: $label (clicking anyway)")
                element.click()
                return true
            }

            // Try by description
            element = device.wait(
                Until.findObject(By.descContains(label)),
                timeout / 2
            )

            if (element != null) {
                Log.d(TAG, "✅ Found element with description: $label")
                element.click()
                return true
            }
        }

        Log.w(TAG, "⚠️ Could not find button with labels: $labelContains")
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
     * Performs a swipe down gesture on the screen.
     */
    fun swipeDown() {
        val displayHeight = device.displayHeight
        val displayWidth = device.displayWidth

        device.swipe(
            displayWidth / 2,
            displayHeight / 4,
            displayWidth / 2,
            displayHeight * 3 / 4,
            10
        )
        Log.d(TAG, "✅ Performed swipe down")
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

    /**
     * Logs all available UI elements for debugging.
     */
    fun logAvailableElements() {
        Log.d(TAG, "=== Available UI Elements ===")

        // Log all text elements
        val textElements = device.findObjects(By.textStartsWith(""))
        Log.d(TAG, "Text elements found: ${textElements.size}")
        textElements.take(20).forEachIndexed { index, element ->
            try {
                Log.d(
                    TAG,
                    "  [$index] class=${element.className}, text='${element.text}', desc='${element.contentDescription}'"
                )
            } catch (e: Exception) {
                Log.d(TAG, "  [$index] (unable to read)")
            }
        }

        // Log focusable elements
        val focusableElements = device.findObjects(By.focusable(true))
        Log.d(TAG, "Focusable elements found: ${focusableElements.size}")
        focusableElements.take(10).forEachIndexed { index, element ->
            try {
                Log.d(TAG, "  [$index] class=${element.className}, text='${element.text}'")
            } catch (e: Exception) {
                Log.d(TAG, "  [$index] (unable to read)")
            }
        }

        Log.d(TAG, "=== End UI Elements ===")
    }
}
