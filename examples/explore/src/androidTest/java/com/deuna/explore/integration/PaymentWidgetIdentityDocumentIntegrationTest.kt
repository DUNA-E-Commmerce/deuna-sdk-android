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
class PaymentWidgetIdentityDocumentIntegrationTest : BaseExploreIntegrationTest() {

    override fun merchantSetup(): TestMerchantSetup {
        val checkoutModules = """
            [
              {
                "name": "CardPattern",
                "props": {
                  "fields": [
                    {
                      "name": "identity_document",
                      "validation": {
                        "required": true
                      },
                      "supportedDocumentTypes": {
                        "CO": ["CC", "NIT", "CE"]
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        return TestMerchantSetup(
            processorType = TestProcessorType.GLOBALPAY,
            countryIso = "CO",
            checkoutModulesJson = checkoutModules
        )
    }

    @Test
    fun testIdentityDocumentTypeDropdownAndValidation() {
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

        // Verify card form is visible
        val cardInputFound = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 15000) != null
        if (!cardInputFound) {
            throw AssertionError("Card form inputs not visible")
        }

        // Verify dropdown trigger is visible by looking for "C.C." (default for CO)
        device.wait(Until.findObject(By.textContains("C.C.")), 10000)
            ?: throw AssertionError("Dropdown trigger (C.C. / C.C. Down) not found")

        // Fill card form except identity document
        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")

        // Trigger pay button without document number to force validation error
        dismissKeyboardSafely()
        Thread.sleep(1000)
        val payButton = device.wait(Until.findObject(By.res("payment-button")), 10000)
            ?: device.wait(Until.findObject(By.textContains("Pagar")), 5000)
            ?: throw AssertionError("Pay button not found")
        payButton.click()

        // Dump all visible texts to understand what labels exist on screen
        Thread.sleep(2000)
        val allTextViews = device.findObjects(By.clazz("android.widget.TextView")).map { it.text }
        val allViews = device.findObjects(By.clazz("android.view.View")).map { it.text ?: it.contentDescription }
        val allTexts = (allTextViews + allViews).filter { !it.isNullOrBlank() }.distinct()
        android.util.Log.d("TEST_DUMP", "Visible texts: $allTexts")

        // Verify validation error is displayed
        val errorTextVisible = device.wait(Until.findObject(By.textContains("requerido")), 10000) != null ||
                device.wait(Until.findObject(By.textContains("documento")), 2000) != null ||
                device.wait(Until.findObject(By.textContains("válido")), 2000) != null
        if (!errorTextVisible) {
            throw AssertionError("Identity document validation error message not displayed. Visible texts: $allTexts")
        }

        // Select CC and fill valid document number
        selectDocumentType("CC")
        webViewHelper.fillTextFieldByLabel("1234567890", "Cédula / Doc de identidad")
        blurDocumentField()
        dismissKeyboardSafely()

        // Error should disappear
        val errorGone = device.wait(Until.gone(By.textContains("requerido")), 5000) &&
                device.wait(Until.gone(By.textContains("documento")), 2000) &&
                device.wait(Until.gone(By.textContains("válido")), 2000)
        if (!errorGone) {
            throw AssertionError("Validation error remained after entering valid document number")
        }

        // Change document type to NIT, verify input field resets (becomes empty)
        selectDocumentType("NIT")
        val inputEmptyAfterNit = verifyDocumentInputFieldIsEmpty()
        if (!inputEmptyAfterNit) {
            throw AssertionError("Document number input did not reset after changing to NIT")
        }

        // Validate NIT rules: fill invalid NIT (9 digits) and verify error
        webViewHelper.fillTextFieldByLabel("123456789", "Cédula / Doc de identidad")
        blurDocumentField()
        dismissKeyboardSafely()
        val errorShownForInvalidNit = device.wait(Until.findObject(By.textContains("válido")), 5000) != null ||
                device.wait(Until.findObject(By.textContains("inválido")), 2000) != null ||
                device.wait(Until.findObject(By.textContains("documento")), 2000) != null
        if (!errorShownForInvalidNit) {
            throw AssertionError("Error not shown for invalid NIT format")
        }

        // Fill valid NIT (10 digits) and verify error disappears
        webViewHelper.fillTextFieldByLabel("9001234567", "Cédula / Doc de identidad")
        blurDocumentField()
        dismissKeyboardSafely()
        val errorGoneForValidNit = device.wait(Until.gone(By.textContains("válido")), 5000) &&
                device.wait(Until.gone(By.textContains("inválido")), 2000) &&
                device.wait(Until.gone(By.textContains("documento")), 2000)
        if (!errorGoneForValidNit) {
            throw AssertionError("Error stayed for valid NIT format")
        }

        // Change document type to CE, verify reset
        selectDocumentType("CE")
        val inputEmptyAfterCe = verifyDocumentInputFieldIsEmpty()
        if (!inputEmptyAfterCe) {
            throw AssertionError("Document number input did not reset after changing to CE")
        }

        // Enter a valid CE number and verify no errors
        webViewHelper.fillTextFieldByLabel("1234567", "Cédula / Doc de identidad")
        blurDocumentField()
        dismissKeyboardSafely()
        val noErrorForCe = device.wait(Until.findObject(By.textContains("documento")), 3000) == null
        if (!noErrorForCe) {
            throw AssertionError("Error shown for valid CE format")
        }

        scenario.close()
    }

    private fun blurDocumentField() {
        val editText = device.findObjects(By.clazz("android.widget.EditText")).firstOrNull()
        editText?.click()
        Thread.sleep(1000)
    }

    private fun dismissKeyboardSafely() {
        val isKeyboardVisible = runCatching {
            device.executeShellCommand("dumpsys input_method | grep mInputShown")
                .contains("mInputShown=true")
        }.getOrDefault(false)
        if (isKeyboardVisible) {
            device.pressBack()
            Thread.sleep(500)
        }
    }

    private fun selectDocumentType(type: String) {
        val trigger = device.wait(Until.findObject(By.textContains("C.C.")), 5000)
            ?: device.wait(Until.findObject(By.textContains("NIT")), 2000)
            ?: device.wait(Until.findObject(By.textContains("C.E.")), 2000)
        if (trigger == null) {
            val allTextViews = device.findObjects(By.clazz("android.widget.TextView")).map { it.text }
            val allViews = device.findObjects(By.clazz("android.view.View")).map { it.text ?: it.contentDescription }
            val allTexts = (allTextViews + allViews).filter { !it.isNullOrBlank() }.distinct()
            throw AssertionError("Document type dropdown trigger not found for type: $type. Visible texts: $allTexts")
        }
        trigger.click()
        Thread.sleep(1000)

        val mappedType = when (type) {
            "CC" -> "C.C."
            "CE" -> "C.E."
            else -> type
        }
        val option = device.wait(Until.findObject(By.text(mappedType)), 5000)
            ?: device.wait(Until.findObject(By.textContains(mappedType)), 2000)
            ?: throw AssertionError("Dropdown option $mappedType not found")
        option.click()
        Thread.sleep(1000)
    }

    private fun verifyDocumentInputFieldIsEmpty(): Boolean {
        val labelElement = device.wait(Until.findObject(By.textContains("Cédula / Doc de identidad")), 5000)
            ?: device.wait(Until.findObject(By.textContains("documento")), 2000)
            ?: return false

        val labelBottom = labelElement.visibleBounds.bottom
        val labelCenterX = labelElement.visibleBounds.centerX()

        val editText = device.findObjects(By.clazz("android.widget.EditText"))
            .filter { it.visibleBounds.top >= labelBottom - 50 }
            .minByOrNull {
                val verticalDist = it.visibleBounds.top - labelBottom
                val horizontalDist = kotlin.math.abs(it.visibleBounds.centerX() - labelCenterX)
                verticalDist + horizontalDist / 2
            } ?: return false

        return editText.text.isNullOrEmpty()
    }
}
