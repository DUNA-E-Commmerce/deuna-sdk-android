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
class PaymentWidget3dsModalIntegrationTest : BaseExploreIntegrationTest() {

    override fun merchantSetup(): TestMerchantSetup =
        TestMerchantSetup(processorType = TestProcessorType.STRIPE_3DS, countryIso = "MX")

    @Test
    fun testModalPaymentWidgetStripe3dsSuccessUsingExploreFlow() {
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

        webViewHelper.fillTextFieldByLabel("4000000000003220", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        fillIdentityDocumentOrFail(flowName = "Payment 3DS")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)
        webViewHelper.buttonTap("Pagar")

        val challengeCompleted = webViewHelper.completeStripe3dsChallenge(timeout = 35000)
        if (!challengeCompleted) {
            throw AssertionError("Stripe 3DS challenge was not completed")
        }

        if (!waitForPaymentSuccess(maxTimeoutMs = 120000)) {
            throw AssertionError("Payment success screen was not shown after 3DS challenge")
        }

        scenario.close()
    }
}
