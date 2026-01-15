package com.deuna.sdkexample.integration.data.dataSources

import com.deuna.sdkexample.integration.core.TestEnvironment
import com.deuna.sdkexample.integration.data.helpers.DeunanowOrderBuilder
import com.deuna.sdkexample.integration.data.helpers.HttpClient
import com.deuna.sdkexample.integration.data.helpers.HttpMethod
import com.deuna.sdkexample.integration.data.helpers.MerchantConfigByCountry
import com.deuna.sdkexample.integration.data.helpers.TokenizeOrderResponse
import com.deuna.sdkexample.integration.domain.CountryCode
import com.deuna.sdkexample.integration.domain.requests.BaseProcessor
import com.deuna.sdkexample.integration.domain.requests.CreateMerchantApplicationRequest
import com.deuna.sdkexample.integration.domain.requests.CreateStoreRequest
import com.deuna.sdkexample.integration.domain.requests.LoginRequest
import com.deuna.sdkexample.integration.domain.requests.PaymentProcessorResponse
import com.deuna.sdkexample.integration.domain.responses.MerchantApplicationResponse
import com.deuna.sdkexample.integration.domain.responses.MerchantLoginResponse
import com.deuna.sdkexample.integration.domain.responses.MerchantResponse
import com.deuna.sdkexample.integration.domain.responses.StoreResponse
import org.json.JSONObject

object AdminCredentials {
    const val username = "developers@getduna.com"
    const val password = "superadmin"
}

class MerchantDataSource(env: TestEnvironment) {

    private val httpClient: HttpClient = HttpClient(env.apiEndpoint)

    /**
     * Creates a new merchant synchronously.
     * @param country The country code for the merchant.
     * @param customName Optional custom name for the merchant.
     * @param managedByDeuna Whether the merchant is managed by DEUNA. Defaults to false.
     * @return The created MerchantResponse.
     * @throws Exception if merchant creation fails.
     */
    @Throws(Exception::class)
    fun createSync(
        country: CountryCode,
        customName: String? = null,
        managedByDeuna: Boolean = false
    ): MerchantResponse {
        val merchantRequest = MerchantConfigByCountry.createMerchantRequest(
            country = country,
            customName = customName,
            managedByDeuna = managedByDeuna
        ) ?: throw Exception("Unsupported country code: $country")

        val response = httpClient.requestSync(
            path = "/merchants",
            method = HttpMethod.POST,
            body = merchantRequest
        )

        return MerchantResponse.fromJson(response)
    }

    /**
     * Logs in as admin synchronously and returns the authentication token.
     * @return The MerchantLoginResponse with the token.
     * @throws Exception if login fails.
     */
    @Throws(Exception::class)
    fun loginSync(): MerchantLoginResponse {
        val loginRequest = LoginRequest(
            username = AdminCredentials.username,
            password = AdminCredentials.password
        )

        val response = httpClient.requestSync(
            path = "/merchants/login",
            method = HttpMethod.POST,
            body = loginRequest.toJson()
        )

        return MerchantLoginResponse.fromJson(response)
    }

    /**
     * Creates a new store for a merchant synchronously.
     * @param merchantId The ID of the merchant.
     * @param merchantToken The authentication token for the merchant.
     * @param storeData The store data to create.
     * @return The created StoreResponse.
     * @throws Exception if store creation fails.
     */
    @Throws(Exception::class)
    fun createStoreSync(
        merchantId: String,
        merchantToken: String,
        storeData: CreateStoreRequest
    ): StoreResponse {
        val response = httpClient.requestSync(
            path = "/merchants/$merchantId/stores",
            method = HttpMethod.POST,
            body = storeData.toJson(),
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        return StoreResponse.fromJson(response)
    }

    /**
     * Creates a new merchant application synchronously.
     * @param merchantId The ID of the merchant.
     * @param merchantToken The authentication token for the merchant.
     * @param applicationData The application data to create.
     * @return The created MerchantApplicationResponse.
     * @throws Exception if application creation fails.
     */
    @Throws(Exception::class)
    fun createMerchantApplicationSync(
        merchantId: String,
        merchantToken: String,
        applicationData: CreateMerchantApplicationRequest
    ): MerchantApplicationResponse {
        val response = httpClient.requestSync(
            path = "/merchants/$merchantId/applications",
            method = HttpMethod.POST,
            body = applicationData.toJson(),
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        return MerchantApplicationResponse.fromJson(response)
    }

    /**
     * Result containing the API keys from a full merchant setup.
     */
    data class MerchantSetupResult(
        val merchant: MerchantResponse,
        val store: StoreResponse,
        val application: MerchantApplicationResponse,
        val merchantToken: String
    ) {
        val publicApiKey: String get() = application.publicKey
        val privateApiKey: String get() = application.privateKey
    }

    /**
     * Creates a complete merchant setup: merchant, store, and application.
     * @param country The country code for the merchant.
     * @param storeName Optional store name. Defaults to "all".
     * @param applicationName Optional application name. Defaults to "Excepte".
     * @param isSandbox Whether the application is sandbox. Defaults to true.
     * @param expireAt The expiration setting. Defaults to "DEV".
     * @return MerchantSetupResult containing the merchant, store, application and API keys.
     * @throws Exception if any step fails.
     */
    @Throws(Exception::class)
    fun setupMerchantSync(
        country: CountryCode,
        storeName: String = "all",
        applicationName: String = "Excepte",
        isSandbox: Boolean = true,
        expireAt: String = "DEV"
    ): MerchantSetupResult {
        // 1. Create merchant
        val merchant = createSync(country = country, managedByDeuna = false)

        // 2. Login to get token
        val loginResponse = loginSync()

        // 3. Create store
        val storeData = CreateStoreRequest(
            name = storeName,
            address = "Auto generated address",
            latitude = 19.3600265,
            longitude = -99.1574174,
            isDefault = true
        )
        val store = createStoreSync(
            merchantId = merchant.id,
            merchantToken = loginResponse.token,
            storeData = storeData
        )

        // 4. Create application
        val applicationData = CreateMerchantApplicationRequest(
            name = applicationName,
            isSandbox = isSandbox,
            expireAt = expireAt
        )

        val application = createMerchantApplicationSync(
            merchantId = merchant.id,
            merchantToken = loginResponse.token,
            applicationData = applicationData
        )

        return MerchantSetupResult(
            merchant = merchant,
            store = store,
            application = application,
            merchantToken = loginResponse.token
        )
    }

    /**
     * Tokenizes an order and returns the order token.
     * @param privateApiKey The private API key for authentication.
     * @param orderData The order data to tokenize as JSONObject.
     * @return The TokenizeOrderResponse with the token.
     * @throws Exception if tokenization fails.
     */
    @Throws(Exception::class)
    fun tokenizeOrderSync(
        privateApiKey: String,
        orderData: JSONObject
    ): TokenizeOrderResponse {
        val response = httpClient.requestSync(
            path = "/merchants/orders",
            method = HttpMethod.POST,
            body = orderData,
            headers = mapOf("X-Api-Key" to privateApiKey)
        )

        return TokenizeOrderResponse.fromJson(response)
    }

    /**
     * Creates a payment processor for a merchant.
     * @param merchantId The ID of the merchant.
     * @param merchantToken The authentication token for the merchant.
     * @param processorData The payment processor configuration.
     * @param storeCode The store code. Defaults to "all".
     * @return The processor ID.
     * @throws Exception if creation fails.
     */
    @Throws(Exception::class)
    fun createPaymentProcessorSync(
        merchantId: String,
        merchantToken: String,
        processorData: BaseProcessor,
        storeCode: String = "all"
    ): String {
        val response = httpClient.requestSync(
            path = "/merchants/$merchantId/stores/$storeCode/processors",
            method = HttpMethod.POST,
            body = processorData.toJson(),
            headers = mapOf("Authorization" to "Bearer $merchantToken")
        )

        val processorResponse = PaymentProcessorResponse.fromJson(response)
        return processorResponse.data.id
    }
}
