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
class VaultWidgetIdentityDocumentPayloadIntegrationTest : BaseExploreIntegrationTest() {

    override fun merchantSetup(): TestMerchantSetup {
        val checkoutModules = """
            [
              {
                "name": "CardPattern",
                "props": {
                  "fields": [
                    {
                      "name": "identity_document",
                      "validation": { "required": true },
                      "supportedDocumentTypes": {
                        "CO": ["CC", "NIT", "CE", "PASSPORT"]
                      }
                    }
                  ]
                }
              },
              {
                "name": "BillingPattern",
                "props": {
                  "fields": [
                    { "name": "address1", "validation": { "required": true } },
                    { "name": "state_name", "validation": { "required": true } },
                    { "name": "city", "validation": { "required": true } },
                    {
                      "name": "identity_document",
                      "validation": { "required": false }
                    }
                  ],
                  "show_in_main_view": false
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
    fun testVaultIdentityDocument() {
        val preCreatedOrderToken = TestMerchantKeysProvider.createOrderTokenForCountry(
            privateKey = privateKey,
            countryIso = "CO",
            currencyIso3 = "COP"
        )
        preConfigureSavedConfig(
            widget = ExploreWidget.VAULT_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
            orderToken = preCreatedOrderToken
        )
        val scenario = launchActivity()

        configureDrawerAndApply(
            widget = ExploreWidget.VAULT_WIDGET,
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

        // Fill card details
        webViewHelper.fillTextFieldByLabel("5474925432670366", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1130", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")

        // Fill cardholder name first as requested (complete card form first)
        webViewHelper.fillTextFieldByLabel("APRO APRO", "Nombre como aparece en la tarjeta")
        Thread.sleep(1000)

        // Enter document ID first (Cédula/CC)
        val docFilled = webViewHelper.fillTextFieldByLabel("1234567890", "Cédula / Doc de identidad") ||
                webViewHelper.fillTextFieldByLabel("1234567890", "Cédula") ||
                webViewHelper.fillTextFieldByLabel("1234567890", "Número de documento")
        if (!docFilled) {
            throw AssertionError("Could not fill identity document field")
        }

        // Open Billing details
        val billingButton = device.wait(Until.findObject(By.textContains("Agregar datos")), 10000)
            ?: device.wait(Until.findObject(By.textContains("Dirección")), 2000)
            ?: throw AssertionError("Billing button 'Agregar datos' not found")
        billingButton.click()
        Thread.sleep(2000)

        // Fill Billing Address
        val addressFilled = webViewHelper.fillTextFieldByLabel("Calle 123", "Dirección") ||
                webViewHelper.fillTextFieldByLabel("Calle 123", "Dirección 1")
        if (!addressFilled) {
            throw AssertionError("Could not fill address field")
        }

        // Fill identity document inside billing form if present
        webViewHelper.fillTextFieldByLabel("1234567890", "Cédula / Doc de identidad")

        dismissKeyboardSafely()

        // Select State (Departamento / Estado) using robust helper
        selectDropdownOption(listOf("Departamento", "Estado"), listOf("Atlant", "Antioquia", "Valle", "Bogot", "Cundinamarca"))

        // Select City (Municipio / Ciudad) using robust helper
        selectDropdownOption(listOf("Ciudad", "Municipio"), listOf("Barranquill", "Bogot", "Agua", "Alban", "Anapoima", "Apulo", "Abejorral", "Alcala"))

        // Save Billing Address
        val saveBillingSuccess = webViewHelper.buttonTap("Guardar", timeout = 8000)
        if (!saveBillingSuccess) {
            throw AssertionError("Could not tap Save/Guardar button in billing details")
        }
        Thread.sleep(2000)

        dismissKeyboardSafely()
        webViewHelper.swipeUp()
        Thread.sleep(1000)

        // Click Save Card (pay/submit)
        val submitButton = device.wait(Until.findObject(By.text("Guardar")), 10000)
            ?: device.wait(Until.findObject(By.textContains("Pagar")), 5000)
            ?: device.wait(Until.findObject(By.textContains("Save")), 2000)
            ?: throw AssertionError("Submit/Save button not found")
        submitButton.click()

        // Wait for Vault success screen
        if (!waitForVaultSuccess(maxTimeoutMs = 60000)) {
            throw AssertionError("Vault success screen was not shown")
        }

        scenario.close()
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

    private fun selectDropdownOption(triggers: List<String>, targetOptions: List<String>) {
        var labelNode: androidx.test.uiautomator.UiObject2? = null
        for (trigger in triggers) {
            labelNode = device.wait(Until.findObject(By.textContains(trigger)), 3000)
            if (labelNode != null) break
        }
        if (labelNode == null) {
            throw AssertionError("Dropdown trigger label containing any of $triggers not found")
        }
        
        // Find the closest View below the label (which represents the dropdown interactive box)
        val labelBottom = labelNode.visibleBounds.bottom
        val labelCenterX = labelNode.visibleBounds.centerX()
        
        val dropdownBox = device.findObjects(By.clazz("android.view.View"))
            .filter { it.visibleBounds.top >= labelBottom - 20 && it.visibleBounds.height() > 20 }
            .minByOrNull { view ->
                val verticalDist = view.visibleBounds.top - labelBottom
                val horizontalDist = kotlin.math.abs(view.visibleBounds.centerX() - labelCenterX)
                verticalDist + horizontalDist / 2
            }
            ?: throw AssertionError("Dropdown interactive box not found below label $triggers")
            
        dropdownBox.click()
        Thread.sleep(2000)
        
        // Wait up to 5 seconds for any of the target options to become visible in the UI
        var option: androidx.test.uiautomator.UiObject2? = null
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline) {
            for (target in targetOptions) {
                val pattern = java.util.regex.Pattern.compile(".*$target.*", java.util.regex.Pattern.CASE_INSENSITIVE)
                option = device.findObject(By.text(pattern))
                if (option != null) break
            }
            if (option != null) break
            Thread.sleep(300)
        }

        if (option == null) {
            throw AssertionError("Could not find any of $targetOptions in dropdown $triggers")
        }
        option.click()
        Thread.sleep(1500)
    }
}
