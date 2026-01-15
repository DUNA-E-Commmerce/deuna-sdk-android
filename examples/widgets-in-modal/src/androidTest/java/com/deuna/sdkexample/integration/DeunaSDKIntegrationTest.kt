package com.deuna.sdkexample.integration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.deuna.sdkexample.integration.core.TestEnvironment
import com.deuna.sdkexample.integration.data.dataSources.MerchantDataSource
import com.deuna.sdkexample.integration.data.helpers.DeunanowOrderBuilder
import com.deuna.sdkexample.integration.domain.CountryCode
import com.deuna.sdkexample.integration.domain.requests.StripeProcessorConfig
import org.junit.Before
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

    private val merchantDataSource = MerchantDataSource(Constants.env)
    private var orderToken: String? = null
    private var publicApiKey: String? = null

    @Before
    fun setUp() {
        Log.d(TAG, "🚀 Starting test setup...")

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
        val orderTokenResponse: com.deuna.sdkexample.integration.data.helpers.TokenizeOrderResponse
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

    @Test
    fun testPaymentWidgetSuccess() {
        Log.d(TAG, "🧪 Starting testPaymentWidgetSuccess - orderToken: $orderToken")

        // Verify setup was successful
        assert(orderToken != null) { "Order token should not be null" }
        assert(publicApiKey != null) { "Public API key should not be null" }

        // TODO: Implement UI interaction with the app
        // 1. Launch activity with environment variables
        // 2. Enter order token in text field
        // 3. Tap Show button
        // 4. Interact with WebView form
        // 5. Verify payment success
    }
}
