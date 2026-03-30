package com.deuna.sdkexample.integration

import android.content.Intent
import android.util.Log
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.deuna.sdkexample.MainActivity
import com.deuna.sdkexample.integration.data.dataSources.MerchantDataSource
import com.deuna.sdkexample.integration.data.helpers.DeunanowOrderBuilder
import com.deuna.sdkexample.integration.data.helpers.TokenizeOrderResponse
import com.deuna.sdkexample.integration.domain.requests.BaseProcessor
import com.deuna.sdkexample.integration.domain.requests.StripeProcessorConfig
import com.deuna.sdkexample.integration.helpers.WebViewTestHelper
import com.deuna.sdkexample.ui.screens.main.MainScreenTestTags
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import org.junit.Before
import org.junit.Rule

abstract class BaseDeunaSDKIntegrationTest {

    protected val tag: String get() = this::class.java.simpleName

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val merchantDataSource = MerchantDataSource(IntegrationTestConstants.env)
    protected var orderToken: String? = null
    protected var publicApiKey: String? = null

    protected lateinit var device: UiDevice
    protected lateinit var webViewHelper: WebViewTestHelper

    protected open fun testCountry() = IntegrationTestConstants.country

    protected open fun paymentProcessorConfig(): BaseProcessor {
        return StripeProcessorConfig.stripeProcessorAuthorize(country = testCountry())
    }

    protected open fun render3dsStrategy(): String? = null

    @Before
    fun setUp() {
        Log.d(tag, "🚀 Starting test setup...")

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        webViewHelper = WebViewTestHelper(device)

        val orderRequest = DeunanowOrderBuilder.createOrder(country = testCountry())
            ?: throw AssertionError("Failed to create order request")
        Log.d(tag, "✅ Order request created")

        val setup = try {
            merchantDataSource.setupMerchantSync(country = testCountry())
        } catch (e: Exception) {
            Log.e(tag, "❌ Merchant setup failed: $e")
            throw AssertionError("Merchant setup failed: $e")
        }
        Log.d(tag, "✅ Merchant setup completed - merchantId: ${setup.merchant.id}")

        try {
            merchantDataSource.configureVaultWidgetSync(
                merchantId = setup.merchant.id,
                merchantToken = setup.merchantToken,
                render3dsStrategy = render3dsStrategy()
            )
        } catch (e: Exception) {
            Log.e(tag, "❌ Vault widget merchant configuration failed: $e")
            throw AssertionError("Vault widget merchant configuration failed: $e")
        }
        Log.d(tag, "✅ Vault widget merchant configuration completed")

        val orderTokenResponse: TokenizeOrderResponse = try {
            merchantDataSource.tokenizeOrderSync(
                privateApiKey = setup.privateApiKey,
                orderData = orderRequest
            )
        } catch (e: Exception) {
            Log.e(tag, "❌ Order tokenization failed: $e")
            throw AssertionError("Order tokenization failed: $e")
        }
        Log.d(tag, "✅ Order tokenized - token: ${orderTokenResponse.token}")

        try {
            val processorId = merchantDataSource.createPaymentProcessorSync(
                merchantId = setup.merchant.id,
                merchantToken = setup.merchantToken,
                processorData = paymentProcessorConfig()
            )
            Log.d(tag, "✅ Payment processor created - processorId: $processorId")
        } catch (e: Exception) {
            Log.e(tag, "❌ Payment processor creation failed: $e")
            throw AssertionError("Payment processor creation failed: $e")
        }

        orderToken = orderTokenResponse.token
        publicApiKey = setup.publicApiKey

        Log.d(tag, "✅ Test setup completed - orderToken: $orderToken")
        Log.d(tag, "✅ Test setup completed - publicApiKey: $publicApiKey")
    }

    protected fun launchActivity(): ActivityScenario<MainActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DEUNA_ENV, IntegrationTestConstants.env.value)
            putExtra(MainActivity.EXTRA_DEUNA_API_KEY, publicApiKey)
            putExtra(MainActivity.EXTRA_ORDER_TOKEN, orderToken)
        }
        return ActivityScenario.launch(intent)
    }

    protected fun selectWidget(widgetLabel: String) {
        val widget = WidgetToShow.entries.firstOrNull { it.label == widgetLabel }
            ?: throw AssertionError("Unknown widget label '$widgetLabel'")

        runCatching {
            composeTestRule.onNodeWithTag(MainScreenTestTags.WIDGET_PICKER_BUTTON).performClick()
            composeTestRule.onNodeWithTag(MainScreenTestTags.widgetOption(widget)).performClick()
            return
        }

        val openMenuButton = device.findObject(UiSelector().descriptionContains("Open menu"))
        if (!openMenuButton.waitForExists(5000)) {
            throw AssertionError("Widget dropdown button not found")
        }
        openMenuButton.click()
        Thread.sleep(500)

        val popupOption = device.findObject(
            UiSelector().className("android.widget.TextView").text(widgetLabel).instance(1)
        )
        val fallbackOption = device.findObject(
            UiSelector().className("android.widget.TextView").text(widgetLabel).instance(0)
        )

        val widgetOption = when {
            popupOption.waitForExists(3000) -> popupOption
            fallbackOption.waitForExists(3000) -> fallbackOption
            else -> null
        } ?: throw AssertionError("Widget option '$widgetLabel' not found")

        widgetOption.click()
        Thread.sleep(300)
    }

    protected fun fillFieldWithRetry(
        value: String,
        labels: List<String>,
        attempts: Int = 3
    ): Boolean {
        repeat(attempts) { index ->
            for (label in labels) {
                if (webViewHelper.fillTextFieldByLabel(value, label)) {
                    Log.d(tag, "✅ Filled field '$label' (attempt ${index + 1})")
                    return true
                }
            }
            webViewHelper.swipeUp()
            Thread.sleep(500)
        }
        return false
    }

    protected fun fillIdentityDocumentOrFail(flowName: String) {
        val identityFilled = fillFieldWithRetry(
            value = "GODE561231GR8",
            labels = listOf(
                "Número de RFC",
                "RFC",
                "Número de documento",
                "Documento de identidad",
                "Identity document",
            ),
            attempts = 4
        )
        if (!identityFilled) {
            throw AssertionError("Could not fill identity document field in $flowName flow")
        }
    }
}
