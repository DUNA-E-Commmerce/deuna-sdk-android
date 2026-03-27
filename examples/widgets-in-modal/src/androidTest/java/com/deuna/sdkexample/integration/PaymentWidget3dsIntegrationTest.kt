package com.deuna.sdkexample.integration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.UiSelector
import com.deuna.sdkexample.integration.domain.requests.BaseProcessor
import com.deuna.sdkexample.integration.domain.requests.StripeProcessorConfig
import com.deuna.sdkexample.integration.helpers.TestEventObserver
import com.deuna.sdkexample.testing.TestEvent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PaymentWidget3dsIntegrationTest : BaseDeunaSDKIntegrationTest() {

    override fun paymentProcessorConfig(): BaseProcessor {
        return StripeProcessorConfig.stripe3dsProcessor(country = IntegrationTestConstants.country)
    }

    override fun render3dsStrategy(): String = "redirect"

    @Test
    fun testPaymentWidgetStripe3dsSuccess() {
        Log.d(tag, "🧪 Starting testPaymentWidgetStripe3dsSuccess - orderToken: $orderToken")

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

        webViewHelper.fillTextFieldByLabel("4000000000003220", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1228", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        fillIdentityDocumentOrFail(flowName = "Payment 3DS")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)
        webViewHelper.buttonTap("Pagar")

        val challengeCompleted = webViewHelper.completeStripe3dsChallenge(timeout = 35000)
        if (!challengeCompleted) {
            throw AssertionError("Stripe 3DS challenge was not completed (COMPLETE button not tapped)")
        }

        if (!TestEventObserver.waitFor(paymentSuccessWaiter, timeoutSeconds = 120)) {
            throw AssertionError("Timeout waiting for PAYMENT_SUCCESS event after 3DS challenge")
        }
        Log.d(tag, "✅ Payment with Stripe 3DS successful!")

        scenario.close()
    }
}
