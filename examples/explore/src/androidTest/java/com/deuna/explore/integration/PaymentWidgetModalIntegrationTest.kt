package com.deuna.explore.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.deuna.explore.presentation.ExploreTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PaymentWidgetModalIntegrationTest : BaseExploreIntegrationTest() {

    @Test
    fun testModalPaymentWidgetSuccessUsingExploreFlow() {
        val scenario = launchActivity()

        clickByResTagOrFail(ExploreTestTags.MENU_BUTTON, fallbackText = "Menu")
        clickByResTagOrFail("explore.environment.${targetEnvironment.name.lowercase()}", fallbackText = targetEnvironment.drawerTitle)
        setDrawerKeysOrFail(publicKey = publicKey, privateKey = privateKey)

        clickByResTagOrFail(ExploreTestTags.APPLY_CONFIGURATION_BUTTON, fallbackText = "Explorar")

        clickByResTagOrFail(ExploreTestTags.FIRST_PRODUCT_ADD_BUTTON, fallbackText = "Add", timeoutMs = 30000)
        clickByResTagOrFail(ExploreTestTags.SHOW_WIDGET_BUTTON, fallbackText = "Show Widget", timeoutMs = 30000)

        if (!webViewHelper.waitForWebView(20000)) {
            throw AssertionError("WebView should open after tapping Show Widget")
        }

        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")

        val identityFilled = listOf(
            "Número de RFC",
            "RFC",
            "Número de documento",
            "Documento de identidad",
            "Identity document"
        ).any { webViewHelper.fillTextFieldByLabel("GODE561231GR8", it, timeout = 2000) }
        if (!identityFilled) throw AssertionError("Could not fill identity document field")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)
        webViewHelper.buttonTap("Pagar")

        if (!waitForPaymentSuccess(maxTimeoutMs = 60000)) {
            throw AssertionError("Payment success screen was not shown")
        }

        scenario.close()
    }

    private fun waitForPaymentSuccess(maxTimeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + maxTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val successByTag = device.findObject(By.res(ExploreTestTags.PAYMENT_SUCCESS_TITLE))
            val successByText = device.findObject(By.textContains("Payment Successful"))
            if (successByTag != null || successByText != null) {
                return true
            }
            Thread.sleep(250)
        }
        return false
    }
}
