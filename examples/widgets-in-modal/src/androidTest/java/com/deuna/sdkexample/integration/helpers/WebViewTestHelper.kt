package com.deuna.sdkexample.integration.helpers

import android.util.Log
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector

/**
 * Helper class for interacting with WebView elements during UI tests.
 * Uses UI Automator to find and interact with elements inside WebViews.
 */
class WebViewTestHelper(private val device: UiDevice) {

    companion object {
        private const val TAG = "WebViewTestHelper"
    }

    /**
     * Fills a text field that contains a specific placeholder text.
     * @param text The text to enter into the field.
     * @param placeholderContains List of possible placeholder texts to match.
     * @param timeout Timeout in milliseconds to wait for the element.
     */
    fun fillTextField(
        text: String,
        placeholderContains: List<String>,
        timeout: Long = 5000
    ): Boolean {
        for (placeholder in placeholderContains) {
            // Try to find by description (accessibility)
            var field = device.findObject(
                UiSelector()
                    .className("android.widget.EditText")
                    .descriptionContains(placeholder)
            )

            if (field.waitForExists(timeout)) {
                Log.d(TAG, "✅ Found field with description containing: $placeholder")
                field.clearTextField()
                field.setText(text)
                return true
            }

            // Try to find by text
            field = device.findObject(
                UiSelector()
                    .className("android.widget.EditText")
                    .textContains(placeholder)
            )

            if (field.waitForExists(timeout / 2)) {
                Log.d(TAG, "✅ Found field with text containing: $placeholder")
                field.clearTextField()
                field.setText(text)
                return true
            }
        }

        Log.w(TAG, "⚠️ Could not find field with placeholders: $placeholderContains")
        return false
    }

    /**
     * Fills a text field by index within the WebView.
     * @param text The text to enter into the field.
     * @param index The index of the EditText (0-based).
     * @param timeout Timeout in milliseconds to wait for the element.
     */
    fun fillTextFieldByIndex(
        text: String,
        index: Int,
        timeout: Long = 5000
    ): Boolean {
        val field = device.findObject(
            UiSelector()
                .className("android.widget.EditText")
                .instance(index)
        )

        if (field.waitForExists(timeout)) {
            Log.d(TAG, "✅ Found EditText at index: $index")
            field.clearTextField()
            field.setText(text)
            return true
        }

        Log.w(TAG, "⚠️ Could not find EditText at index: $index")
        return false
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
            // Try Button class
            var button = device.findObject(
                UiSelector()
                    .className("android.widget.Button")
                    .textContains(label)
            )

            if (button.waitForExists(timeout)) {
                Log.d(TAG, "✅ Found button with text: $label")
                button.click()
                return true
            }

            // Try any clickable element with text
            button = device.findObject(
                UiSelector()
                    .clickable(true)
                    .textContains(label)
            )

            if (button.waitForExists(timeout / 2)) {
                Log.d(TAG, "✅ Found clickable element with text: $label")
                button.click()
                return true
            }

            // Try by description
            button = device.findObject(
                UiSelector()
                    .clickable(true)
                    .descriptionContains(label)
            )

            if (button.waitForExists(timeout / 2)) {
                Log.d(TAG, "✅ Found clickable element with description: $label")
                button.click()
                return true
            }
        }

        Log.w(TAG, "⚠️ Could not find button with labels: $labelContains")
        return false
    }

    /**
     * Performs a swipe up gesture on the screen.
     * Useful for scrolling content in WebViews.
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
     * Gets all EditText fields currently visible.
     * Useful for debugging which fields are available.
     */
    fun logAvailableEditTexts() {
        var index = 0
        while (true) {
            val field = device.findObject(
                UiSelector()
                    .className("android.widget.EditText")
                    .instance(index)
            )
            if (!field.exists()) break

            try {
                Log.d(TAG, "EditText[$index]: text='${field.text}', desc='${field.contentDescription}'")
            } catch (e: Exception) {
                Log.d(TAG, "EditText[$index]: (unable to read properties)")
            }
            index++
        }
        Log.d(TAG, "Total EditText fields found: $index")
    }
}
