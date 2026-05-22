package com.deuna.explore.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.domain.ExploreWidget
import com.deuna.explore.presentation.ExploreTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PaymentWidgetModalIntegrationTest : BaseExploreIntegrationTest() {

    @Test
    fun testModalPaymentWidgetSuccessUsingExploreFlow() {
        val scenario = launchActivity()

        configureDrawerAndApply(
            widget = ExploreWidget.PAYMENT_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
        )

        clickByResTagOrFail(ExploreTestTags.FIRST_PRODUCT_ADD_BUTTON, fallbackText = "Add", timeoutMs = 30000)
        clickByResTagOrFail(ExploreTestTags.SHOW_WIDGET_BUTTON, fallbackText = "Show Widget", timeoutMs = 30000)

        if (!webViewHelper.waitForWebView(20000)) {
            throw AssertionError("WebView should open after tapping Show Widget")
        }

        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        fillIdentityDocumentOrFail(flowName = "Payment")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)
        webViewHelper.buttonTap("Pagar")

        if (!waitForPaymentSuccess(maxTimeoutMs = 60000)) {
            throw AssertionError("Payment success screen was not shown")
        }

        scenario.close()
    }
}
