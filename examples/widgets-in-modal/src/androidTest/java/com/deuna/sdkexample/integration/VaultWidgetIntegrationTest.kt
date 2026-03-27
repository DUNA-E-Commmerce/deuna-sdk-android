package com.deuna.sdkexample.integration

import android.util.Log
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.UiSelector
import com.deuna.sdkexample.integration.helpers.TestEventObserver
import com.deuna.sdkexample.testing.TestEvent
import com.deuna.sdkexample.ui.screens.main.MainScreenTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class VaultWidgetIntegrationTest : BaseDeunaSDKIntegrationTest() {

    @Test
    fun testVaultWidgetSuccess() {
        Log.d(tag, "🧪 Starting testVaultWidgetSuccess - orderToken: $orderToken")

        assert(orderToken != null) { "Order token should not be null" }
        assert(publicApiKey != null) { "Public API key should not be null" }

        val vaultSuccessWaiter = TestEventObserver.createWaiter(TestEvent.VAULT_SUCCESS)
        val vaultErrorWaiter = TestEventObserver.createWaiter(TestEvent.VAULT_ERROR)

        val scenario = launchActivity()
        Thread.sleep(2000)

        selectWidget("Vault Widget")

        runCatching {
            composeTestRule.onNodeWithTag(MainScreenTestTags.SHOW_WIDGET_BUTTON).performClick()
        }.getOrElse {
            val showButton = device.findObject(UiSelector().text("Show Widget"))
            if (showButton.waitForExists(5000)) {
                Log.d(tag, "✅ Tapping Show Widget button for Vault")
                showButton.click()
            } else {
                throw AssertionError("Show Widget button not found")
            }
        }

        if (!webViewHelper.waitForWebView(15000)) {
            throw AssertionError("Vault WebView should open after tapping Show Widget")
        }
        Log.d(tag, "✅ Vault WebView appeared")

        Thread.sleep(4000)

        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        fillIdentityDocumentOrFail(flowName = "Vault")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)

        val submitTapped = webViewHelper.buttonTap("Pagar") ||
            webViewHelper.buttonTap("Guardar") ||
            webViewHelper.buttonTap("Save")

        if (!submitTapped) {
            throw AssertionError("Vault submit button not found (Pagar/Guardar/Save)")
        }

        val successReceived = TestEventObserver.waitFor(vaultSuccessWaiter, timeoutSeconds = 60)
        if (!successReceived) {
            val errorReceived = TestEventObserver.waitFor(vaultErrorWaiter, timeoutSeconds = 2)
            if (errorReceived) {
                throw AssertionError("Vault flow emitted VAULT_ERROR")
            }
            throw AssertionError("Timeout waiting for VAULT_SUCCESS event")
        }

        Log.d(tag, "✅ Vault flow successful!")
        scenario.close()
    }
}
