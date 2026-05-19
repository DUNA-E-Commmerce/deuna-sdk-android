package com.deuna.explore.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.domain.ExploreWidget
import com.deuna.explore.presentation.ExploreTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PaymentWidgetVoucherPayuEfectyModalIntegrationTest : BaseExploreIntegrationTest() {

    override fun merchantSetup(): TestMerchantSetup =
        TestMerchantSetup(processorType = TestProcessorType.PAYU_EFECTY, countryIso = "CO")

    @Test
    fun testModalPaymentWidgetPayuEfectySuccessUsingExploreFlow() {
        val preCreatedOrderToken = TestMerchantKeysProvider.createOrderTokenForCountry(
            privateKey = privateKey,
            countryIso = "CO",
            currencyIso3 = "COP",
        )
        val scenario = launchActivity()

        configureDrawerAndApply(
            widget = ExploreWidget.VOUCHER_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
            orderToken = preCreatedOrderToken,
        )

        clickByResTagOrFail(ExploreTestTags.SHOW_WIDGET_BUTTON, fallbackText = "Show Widget", timeoutMs = 30000)

        if (!webViewHelper.waitForWebView(20000)) {
            throw AssertionError("Voucher WebView should open after tapping Show Widget")
        }

        val payTapped = webViewHelper.buttonTap("Pagar COP", timeout = 10000) ||
            webViewHelper.buttonTap("Pagar", timeout = 10000)
        if (!payTapped) {
            throw AssertionError("Pay button not found in voucher flow")
        }

        val voucherReady = waitForVoucherDetailsReady(timeoutMs = 30000)
        if (!voucherReady) {
            throw AssertionError("Voucher details screen did not render")
        }

        val paymentDetailsTapped = webViewHelper.buttonTap("Ver datos para pago", timeout = 10000)
        if (!paymentDetailsTapped) {
            throw AssertionError("Button 'Ver datos para pago' not found")
        }

        val externalOpenedByContent = device.wait(
            Until.findObject(By.textContains("Resumen de la compra")),
            15000
        ) != null || device.wait(
            Until.findObject(By.textContains("PayU")),
            5000
        ) != null

        val externalOpenedByPackage = waitForExternalPackageSwitch(timeoutMs = 8000)
        if (!externalOpenedByContent && !externalOpenedByPackage) {
            throw AssertionError("External payment details page did not open")
        }

        if (!returnToVoucherScreenAfterExternal(timeoutMs = 15000)) {
            throw AssertionError("Could not return to voucher screen after external page")
        }

        val orderSummaryTapped = webViewHelper.buttonTap("Ver resumen del pedido", timeout = 15000)
        if (!orderSummaryTapped) {
            throw AssertionError("Button 'Ver resumen del pedido' not found")
        }

        if (!waitForPaymentSuccess(maxTimeoutMs = 45000)) {
            throw AssertionError("Payment success screen was not shown after voucher summary")
        }

        scenario.close()
    }

    private fun waitForVoucherDetailsReady(timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val ready =
                device.wait(Until.findObject(By.textContains("Ver datos para pago")), 1200) != null ||
                    device.wait(Until.findObject(By.textContains("Descargar datos para pago")), 1200) != null
            if (ready) return true
            webViewHelper.swipeUp()
            Thread.sleep(400)
        }
        return false
    }

    private fun waitForExternalPackageSwitch(timeoutMs: Long): Boolean {
        val appPackage = "com.deuna.explore"
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val current = device.currentPackageName.orEmpty()
            if (current.isNotBlank() && current != appPackage) return true
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
