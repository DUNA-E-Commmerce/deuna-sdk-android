package com.deuna.explore.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.domain.ExploreWidget
import com.deuna.explore.presentation.ExploreTestTags
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class VaultWidgetModalIntegrationTest : BaseExploreIntegrationTest() {

    @Test
    fun testModalVaultWidgetSuccessUsingExploreFlow() {
        preConfigureSavedConfig(
            widget = ExploreWidget.VAULT_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
        )
        val scenario = launchActivity()

        configureDrawerAndApply(
            widget = ExploreWidget.VAULT_WIDGET,
            presentationMode = ExplorePresentationMode.MODAL,
        )

        clickByResTagOrFail(ExploreTestTags.FIRST_PRODUCT_ADD_BUTTON, fallbackText = "Add", timeoutMs = 30000)
        clickByResTagOrFail(ExploreTestTags.SHOW_WIDGET_BUTTON, fallbackText = "Show Widget", timeoutMs = 30000)
        if (!waitForVaultForm(maxTimeoutMs = 30000)) {
            throw AssertionError("Vault form was not shown after tapping Show Widget")
        }

        Thread.sleep(2000)
        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1230", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        fillIdentityDocumentOrFail(flowName = "Vault")

        webViewHelper.dismissKeyboard()
        webViewHelper.swipeUp()
        Thread.sleep(1000)

        val submitTapped = webViewHelper.buttonTap("Pagar") ||
            webViewHelper.buttonTap("Guardar") ||
            webViewHelper.buttonTap("Save")
        if (!submitTapped) {
            throw AssertionError("Vault submit button not found (Pagar/Guardar/Save)")
        }

        if (!waitForVaultSuccess(maxTimeoutMs = 70000)) {
            throw AssertionError("Vault success screen was not shown")
        }

        scenario.close()
    }

    private fun waitForVaultForm(maxTimeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + maxTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val hasCardNumber =
                device.findObject(By.textContains("Número de tarjeta")) != null ||
                    device.findObject(By.textContains("Card number")) != null
            val hasCardName =
                device.findObject(By.textContains("Nombre como aparece en la tarjeta")) != null ||
                    device.findObject(By.textContains("Name on card")) != null
            if (hasCardNumber || hasCardName) return true
            Thread.sleep(300)
        }
        return false
    }
}
