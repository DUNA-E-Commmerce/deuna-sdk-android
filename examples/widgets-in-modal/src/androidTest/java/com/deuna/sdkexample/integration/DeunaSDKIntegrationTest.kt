package com.deuna.sdkexample.integration

import android.content.Intent
import android.util.Log
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.deuna.sdkexample.MainActivity
import com.deuna.sdkexample.integration.core.TestEnvironment
import com.deuna.sdkexample.integration.data.dataSources.MerchantDataSource
import com.deuna.sdkexample.integration.data.helpers.DeunanowOrderBuilder
import com.deuna.sdkexample.integration.data.helpers.TokenizeOrderResponse
import com.deuna.sdkexample.integration.domain.CountryCode
import com.deuna.sdkexample.integration.domain.requests.StripeProcessorConfig
import com.deuna.sdkexample.integration.helpers.TestEventObserver
import com.deuna.sdkexample.integration.helpers.WebViewTestHelper
import com.deuna.sdkexample.testing.TestEvent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

object Constants {
    val env: TestEnvironment = TestEnvironment.STAGING // DEVELOPMENT, STAGING
    val country: CountryCode = CountryCode.MX
}

@RunWith(AndroidJUnit4::class)
@LargeTest
class DeunaSDKIntegrationTest {

    companion object {
        private const val TAG = "DeunaSDKIntegrationTest"
    }

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val merchantDataSource = MerchantDataSource(Constants.env)
    private var orderToken: String? = null
    private var publicApiKey: String? = null

    private lateinit var device: UiDevice
    private lateinit var webViewHelper: WebViewTestHelper

    @Before
    fun setUp() {
        Log.d(TAG, "🚀 Starting test setup...")

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        webViewHelper = WebViewTestHelper(device)

        // 1. Create order request
        val orderRequest = DeunanowOrderBuilder.createOrder(country = Constants.country)
        if (orderRequest == null) {
            throw AssertionError("Failed to create order request")
        }
        Log.d(TAG, "✅ Order request created")

        // 2. Create the merchant, store and application
        val setup: MerchantDataSource.MerchantSetupResult
        try {
            setup = merchantDataSource.setupMerchantSync(country = Constants.country)
            Log.d(TAG, "✅ Merchant setup completed - merchantId: ${setup.merchant.id}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Merchant setup failed: $e")
            throw AssertionError("Merchant setup failed: $e")
        }

        // 3. Tokenize the order
        val orderTokenResponse: TokenizeOrderResponse
        try {
            orderTokenResponse = merchantDataSource.tokenizeOrderSync(
                privateApiKey = setup.privateApiKey,
                orderData = orderRequest
            )
            Log.d(TAG, "✅ Order tokenized - token: ${orderTokenResponse.token}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Order tokenization failed: $e")
            throw AssertionError("Order tokenization failed: $e")
        }

        // 4. Create payment processor
        try {
            val processorId = merchantDataSource.createPaymentProcessorSync(
                merchantId = setup.merchant.id,
                merchantToken = setup.merchantToken,
                processorData = StripeProcessorConfig.stripeProcessorAuthorize(country = Constants.country)
            )
            Log.d(TAG, "✅ Payment processor created - processorId: $processorId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Payment processor creation failed: $e")
            throw AssertionError("Payment processor creation failed: $e")
        }

        // 5. Store the tokens for use in tests
        orderToken = orderTokenResponse.token
        publicApiKey = setup.publicApiKey

        Log.d(TAG, "✅ Test setup completed - orderToken: $orderToken")
        Log.d(TAG, "✅ Test setup completed - publicApiKey: $publicApiKey")
    }

    private fun launchActivity(): ActivityScenario<MainActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DEUNA_ENV, Constants.env.value)
            putExtra(MainActivity.EXTRA_DEUNA_API_KEY, publicApiKey)
            putExtra(MainActivity.EXTRA_ORDER_TOKEN, orderToken)
        }
        return ActivityScenario.launch(intent)
    }

    @Test
    fun testPaymentWidgetSuccess() {
        Log.d(TAG, "🧪 Starting testPaymentWidgetSuccess - orderToken: $orderToken")

        // Verify setup was successful
        assert(orderToken != null) { "Order token should not be null" }
        assert(publicApiKey != null) { "Public API key should not be null" }

        // Create waiters for events BEFORE launching
        val paymentMethodsWaiter = TestEventObserver.createWaiter(TestEvent.PAYMENT_METHODS_ENTERED)
        val paymentSuccessWaiter = TestEventObserver.createWaiter(TestEvent.PAYMENT_SUCCESS)

        // Launch the activity with the configured environment
        val scenario = launchActivity()

        // Wait for the app to be ready and tap "Show Widget" button
        Thread.sleep(2000) // Wait for UI to settle

        // Find and click the "Show Widget" button using UI Automator
        val showButton = device.findObject(UiSelector().text("Show Widget"))
        if (showButton.waitForExists(5000)) {
            Log.d(TAG, "✅ Tapping Show Widget button")
            showButton.click()
        } else {
            throw AssertionError("Show Widget button not found")
        }

        // Verify WebView appears
        if (!webViewHelper.waitForWebView(15000)) {
            throw AssertionError("WebView should open after tapping Show Widget")
        }
        Log.d(TAG, "✅ WebView appeared")

        // Wait for the paymentMethodsEntered event before filling the form
        if (!TestEventObserver.waitFor(paymentMethodsWaiter, timeoutSeconds = 30)) {
            throw AssertionError("Timeout waiting for paymentMethodsEntered event")
        }
        Log.d(TAG, "✅ Received paymentMethodsEntered event")

        // Fill form fields
        webViewHelper.fillTextFieldByLabel("4242424242424242", "Número de tarjeta")
        webViewHelper.fillTextFieldByLabel("1228", "Fecha expiración")
        webViewHelper.fillTextFieldByLabel("123", "CVV")
        webViewHelper.fillTextFieldByLabel("Test User", "Nombre como aparece en la tarjeta")
        webViewHelper.fillTextFieldByLabel("12345678", "Número de RFC")

        // Dismiss keyboard
        webViewHelper.dismissKeyboard()

        // Scroll up to see pay button
        webViewHelper.swipeUp()
        Thread.sleep(1000)

        // Tap pay button
        webViewHelper.buttonTap("Pagar")

        // Wait for PAYMENT_SUCCESS event instead of UI navigation (WebView may crash after payment)
        if (!TestEventObserver.waitFor(paymentSuccessWaiter, timeoutSeconds = 60)) {
            throw AssertionError("Timeout waiting for PAYMENT_SUCCESS event")
        }
        Log.d(TAG, "✅ Payment successful!")

        scenario.close()
    }
}
