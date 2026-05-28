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
class PaymentWidgetInstallmentsDeselectIntegrationTest : BaseExploreIntegrationTest() {

    override fun merchantSetup(): TestMerchantSetup =
        TestMerchantSetup(
            processorType = TestProcessorType.GLOBALPAY,
            countryIso = "CO",
            enableMsi = true
        )

    @Test
    fun testPaymentWidgetInstallmentsDeselect() {
        val preCreatedOrderToken = TestMerchantKeysProvider.createOrderTokenForCountry(
            privateKey = privateKey,
            countryIso = "CO",
            currencyIso3 = "COP"
        )
        preConfigureSavedConfig(
            widget = ExploreWidget.PAYMENT_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
            orderToken = preCreatedOrderToken
        )
        val scenario = launchActivity()

        configureDrawerAndApply(
            widget = ExploreWidget.PAYMENT_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
            orderToken = preCreatedOrderToken
        )

        clickByResTagOrFail(ExploreTestTags.SHOW_WIDGET_BUTTON, fallbackText = "Show Widget", timeoutMs = 30000)

        if (!webViewHelper.waitForWebView(20000)) {
            throw AssertionError("WebView should open after tapping Show Widget")
        }

        // Wait for card input field to be visible
        val cardInputFound = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 15000) != null
        if (!cardInputFound) {
            throw AssertionError("Card form inputs not visible")
        }

        // Step 1: Insert credit card WITHOUT installments (UATP)
        webViewHelper.fillTextFieldByLabel("135410189003949", "Número de tarjeta")
        Thread.sleep(1500)
        
        // Assert dropdown or trigger is not visible
        val dropdownTrigger = device.findObject(By.res("installments-dropdown"))
            ?: device.findObject(By.textContains("Cuotas"))
            ?: device.findObject(By.textContains("Sin cuotas"))
        if (dropdownTrigger != null) {
            throw AssertionError("Installments dropdown/trigger should not be visible for UATP card")
        }

        // Step 2: Insert credit card WITH installments campaign (Globalpay Visa Successful)
        webViewHelper.fillTextFieldByLabel("4111111111111111", "Número de tarjeta")
        Thread.sleep(2000)

        // Assert installments dropdown or select trigger is visible
        var msiDropdown = device.wait(Until.findObject(By.textContains("Sin cuotas")), 15000)
            ?: device.wait(Until.findObject(By.textContains("Cuotas")), 2000)
            ?: device.wait(Until.findObject(By.textContains("Pago")), 2000)
            ?: device.wait(Until.findObject(By.res("installments-dropdown")), 2000)
        
        if (msiDropdown == null) {
            val allTextViews = device.findObjects(By.clazz("android.widget.TextView")).map { it.text }
            throw AssertionError("Installments dropdown/trigger not found. Visible texts: $allTextViews")
        }

        // Click dropdown
        msiDropdown.click()
        Thread.sleep(1500)

        // Step 3: Select 3 installments
        val option3 = device.wait(Until.findObject(By.textContains("3")), 5000)
            ?: throw AssertionError("Option for 3 installments not found in dropdown list")
        option3.click()
        Thread.sleep(1500)

        // Step 4: Click dropdown again to deselect
        val msiDropdownActive = device.wait(Until.findObject(By.textContains("3")), 5000)
            ?: device.wait(Until.findObject(By.res("installments-dropdown")), 2000)
            ?: throw AssertionError("Active installments dropdown not found after selection")
        msiDropdownActive.click()
        Thread.sleep(1500)

        // Select option to deselect (typically "Sin cuotas" or first option)
        val optionDeselect = device.wait(Until.findObject(By.textContains("Sin cuotas")), 5000)
            ?: device.wait(Until.findObject(By.textContains("Una sola")), 2000)
            ?: device.wait(Until.findObject(By.textContains("1")), 2000)
            ?: throw AssertionError("Option to deselect installments not found")
        optionDeselect.click()
        Thread.sleep(1500)

        // Step 5: Fill the rest of the card details and pay
        webViewHelper.fillTextFieldByLabel("1228", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")

        // Enter document ID first (Colombia format fallback)
        val docFilled = webViewHelper.fillTextFieldByLabel("1234567890", "Cédula / Doc de identidad") ||
                webViewHelper.fillTextFieldByLabel("1234567890", "Cédula") ||
                webViewHelper.fillTextFieldByLabel("1234567890", "Número de documento")
        if (!docFilled) {
            throw AssertionError("Could not fill identity document field")
        }

        webViewHelper.fillTextFieldByLabel("APRO APRO", "Nombre como aparece en la tarjeta")
        Thread.sleep(1000)

        dismissKeyboardIfVisible()
        webViewHelper.swipeUp()
        Thread.sleep(1000)

        val payButton = device.wait(Until.findObject(By.res("payment-button")), 10000)
            ?: device.wait(Until.findObject(By.textContains("Pagar")), 5000)
            ?: throw AssertionError("Pay button not found")
        payButton.click()

        // Step 6: Wait for payment success screen
        if (!waitForPaymentSuccess(maxTimeoutMs = 60000)) {
            throw AssertionError("Payment success screen was not shown after deselected installments")
        }

        scenario.close()
    }
}
