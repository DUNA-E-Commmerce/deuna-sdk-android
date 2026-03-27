package com.deuna.sdkexample.integration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.deuna.sdkexample.integration.domain.CountryCode
import com.deuna.sdkexample.integration.domain.requests.BaseProcessor
import com.deuna.sdkexample.integration.domain.requests.VoucherProcessorConfig
import com.deuna.sdkexample.integration.helpers.TestEventObserver
import com.deuna.sdkexample.testing.TestEvent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PaymentWidgetVoucherPayuEfectyIntegrationTest : BaseDeunaSDKIntegrationTest() {

    override fun testCountry(): CountryCode = CountryCode.CO

    override fun paymentProcessorConfig(): BaseProcessor = VoucherProcessorConfig.payuEfectyProcessor()

    @Test
    fun testPaymentWidgetPayuEfectyExternalReceiptAndPurchaseEvent() {
        Log.d(tag, "🧪 Starting testPaymentWidgetPayuEfectyExternalReceiptAndPurchaseEvent")

        assert(orderToken != null) { "Order token should not be null" }
        assert(publicApiKey != null) { "Public API key should not be null" }

        val purchaseEventWaiter = TestEventObserver.createWaiter(TestEvent.PAYMENT_PURCHASE)
        val paymentSuccessWaiter = TestEventObserver.createWaiter(TestEvent.PAYMENT_SUCCESS)

        val scenario = launchActivity()
        Thread.sleep(2000)

        val showButton = device.findObject(UiSelector().text("Show Widget"))
        if (!showButton.waitForExists(5000)) {
            throw AssertionError("Show Widget button not found")
        }
        showButton.click()

        if (!webViewHelper.waitForWebView(15000)) {
            throw AssertionError("Payment widget WebView did not open")
        }

        // In voucher-only flow there is no card form, only disclaimer + pay button.
        val payTapped = webViewHelper.buttonTap("Pagar COP", timeout = 10000) ||
            webViewHelper.buttonTap("Pagar", timeout = 10000)
        if (!payTapped) {
            throw AssertionError("Pay button not found in voucher flow")
        }

        // Wait until voucher details actions are rendered and then open external details.
        val voucherReady = waitForVoucherDetailsReady(timeoutMs = 30000)
        if (!voucherReady) {
            throw AssertionError("Voucher details screen did not render after tapping pay")
        }

        val paymentDetailsTapped = webViewHelper.buttonTap("Ver datos para pago", timeout = 10000)
        if (!paymentDetailsTapped) {
            throw AssertionError("Button 'Ver datos para pago' not found")
        }

        // Validate that external page was opened (outside app) and looks like PayU/Efecty.
        val externalOpenedByContent = device.wait(
            Until.findObject(By.textContains("Resumen de la compra")),
            15000
        ) != null || device.wait(
            Until.findObject(By.textContains("PayU")),
            5000
        ) != null

        val externalOpenedByPackage = waitForExternalPackageSwitch(timeoutMs = 8000)

        if (!externalOpenedByContent && !externalOpenedByPackage) {
            throw AssertionError("External payment details page did not open correctly")
        }

        // Return with physical back button to voucher modal.
        if (!returnToVoucherScreenAfterExternal(timeoutMs = 15000)) {
            throw AssertionError("Could not return to voucher screen after external page")
        }

        val orderSummaryTapped = webViewHelper.buttonTap("Ver resumen del pedido", timeout = 15000)
        if (!orderSummaryTapped) {
            throw AssertionError("Button 'Ver resumen del pedido' not found after returning from external page")
        }

        if (!TestEventObserver.waitFor(purchaseEventWaiter, timeoutSeconds = 20)) {
            throw AssertionError("purchase event was not emitted after 'Ver resumen del pedido'")
        }

        if (!TestEventObserver.waitFor(paymentSuccessWaiter, timeoutSeconds = 40)) {
            throw AssertionError("Timeout waiting for PAYMENT_SUCCESS after voucher summary")
        }

        Log.d(tag, "✅ PayU Efecty voucher flow validated end-to-end")
        scenario.close()
    }

    private fun waitForVoucherDetailsReady(timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val ready =
                device.wait(Until.findObject(By.textContains("Ver datos para pago")), 1200) != null ||
                    device.wait(Until.findObject(By.textContains("Descargar datos para pago")), 1200) != null
            if (ready) {
                return true
            }
            webViewHelper.swipeUp()
            Thread.sleep(400)
        }
        return false
    }

    private fun waitForExternalPackageSwitch(timeoutMs: Long): Boolean {
        val appPackage = "com.deuna.sdkexample"
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val current = device.currentPackageName.orEmpty()
            if (current.isNotBlank() && current != appPackage) {
                Log.d(tag, "✅ External package detected: $current")
                return true
            }
            Thread.sleep(250)
        }
        return false
    }

    private fun returnToVoucherScreenAfterExternal(timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val backAtVoucher =
                device.wait(Until.findObject(By.textContains("Ver resumen del pedido")), 700) != null ||
                    device.wait(Until.findObject(By.textContains("Ver datos para pago")), 500) != null
            if (backAtVoucher) return true

            try {
                device.executeShellCommand("input keyevent 4")
            } catch (_: Exception) {
                device.pressBack()
            }
            Thread.sleep(600)
        }
        return false
    }
}
