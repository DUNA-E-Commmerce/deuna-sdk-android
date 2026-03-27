package com.deuna.sdkexample.integration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.UiSelector
import com.deuna.sdkexample.integration.helpers.TestEventObserver
import com.deuna.sdkexample.testing.TestEvent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PaymentWidgetIntegrationTest : BaseDeunaSDKIntegrationTest() {

    @Test
    fun testPaymentWidgetSuccess() {
        Log.d(tag, "🧪 Starting testPaymentWidgetSuccess - orderToken: $orderToken")

        assert(orderToken != null) { "Order token should not be null" }
        assert(publicApiKey != null) { "Public API key should not be null" }

        val paymentMethodsWaiter = TestEventObserver.createWaiter(TestEvent.PAYMENT_METHODS_ENTERED)
        val paymentSuccessWaiter = TestEventObserver.createWaiter(TestEvent.PAYMENT_SUCCESS)

        val scenario = launchActivity()
        Thread.sleep(2000)

        val showButton = device.findObject(UiSelector().text("Show Widget"))
        if (showButton.waitForExists(5000)) {
            Log.d(tag, "✅ Tapping Show Widget button")
            showButton.click()
        } else {
            throw AssertionError("Show Widget button not found")
        }

        if (!webViewHelper.waitForWebView(15000)) {
            throw AssertionError("WebView should open after tapping Show Widget")
        }
        Log.d(tag, "✅ WebView appeared")

        if (!TestEventObserver.waitFor(paymentMethodsWaiter, timeoutSeconds = 30)) {
            throw AssertionError("Timeout waiting for paymentMethodsEntered event")
        }
        Log.d(tag, "✅ Received paymentMethodsEntered event")

        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        fillIdentityDocumentOrFail(flowName = "Payment")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)
        webViewHelper.buttonTap("Pagar")

        if (!TestEventObserver.waitFor(paymentSuccessWaiter, timeoutSeconds = 60)) {
            throw AssertionError("Timeout waiting for PAYMENT_SUCCESS event")
        }
        Log.d(tag, "✅ Payment successful!")

        scenario.close()
    }
}
