package com.deuna.maven.wallets

import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.wallets.google_pay.GooglePayCredentials
import com.deuna.maven.wallets.google_pay.GooglePayTokenizationType
import com.deuna.maven.wallets.google_pay.WalletFetchResult
import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses responses from POST /api/vault into domain objects.
 *
 * Used by both [GetWalletsAvailable] (provider list only) and
 * [WalletElements] (full fetch result with credentials + user auth).
 *
 * Expected JSON shape: see [WalletElements.fetchCredentials] KDoc.
 */
internal object VaultResponseParser {

    data class ProvidersResult(
        val providers: List<WalletProvider>,
        val googlePayCredentials: GooglePayCredentials?,
    )

    fun parseProviders(root: JSONObject): ProvidersResult {
        val paymentMethods = root.optJSONArray("paymentMethods")
            ?: return ProvidersResult(emptyList(), null)
        val merchant = root.optJSONObject("checkout")?.optJSONObject("merchant")
        val providers = mutableListOf<WalletProvider>()
        var googlePayCredentials: GooglePayCredentials? = null

        for (i in 0 until paymentMethods.length()) {
            val method = paymentMethods.optJSONObject(i) ?: continue
            val provider = WalletProvider.fromProcessorName(method.optString("processor_name"))
                ?: continue
            if (provider !in providers) {
                providers.add(provider)
                if (provider == WalletProvider.GOOGLE_PAY) {
                    googlePayCredentials = parseGooglePayCredentials(method, merchant)
                }
            }
        }
        return ProvidersResult(providers, googlePayCredentials)
    }

    fun parseFetchResult(root: JSONObject): WalletFetchResult {
        val paymentMethods = root.optJSONArray("paymentMethods")
        val merchant = root.optJSONObject("checkout")?.optJSONObject("merchant")
        val order = root.optJSONObject("checkout")?.optJSONObject("order")?.optJSONObject("order")
        val userAuthData = root.optJSONObject("userAuthResponse")?.optJSONObject("data")

        var credentials: GooglePayCredentials? = null
        if (paymentMethods != null) {
            for (i in 0 until paymentMethods.length()) {
                val method = paymentMethods.optJSONObject(i) ?: continue
                if (method.optString("processor_name") != WalletProvider.GOOGLE_PAY.processorName) continue
                credentials = parseGooglePayCredentials(method, merchant, order)
                break
            }
        }

        return WalletFetchResult(
            googlePayCredentials = credentials,
            userToken = userAuthData?.optString("user_token")?.takeIf { it.isNotEmpty() },
            userId = userAuthData?.optString("user_id")?.takeIf { it.isNotEmpty() },
        )
    }

    fun buildUserInfoBody(userInfo: UserInfo?): JSONObject? {
        if (userInfo == null || userInfo.email.isEmpty()) return null
        return JSONObject().apply {
            put("email", userInfo.email)
            if (userInfo.firstName.isNotEmpty()) put("firstName", userInfo.firstName)
            if (userInfo.lastName.isNotEmpty()) put("lastName", userInfo.lastName)
        }
    }

    private fun parseGooglePayCredentials(
        method: JSONObject,
        merchant: JSONObject?,
        order: JSONObject? = null,
    ): GooglePayCredentials {
        val creds = method.optJSONObject("credentials") ?: JSONObject()
        val extraParams = method.optJSONObject("extra_params") ?: JSONObject()
        val merchantId = creds.optString("external_merchant_id", "")
        val gatewayRaw = extraParams.optString("gateway", "")

        return GooglePayCredentials(
            merchantId = merchantId,
            merchantName = merchant?.optString("name", "") ?: "",
            gateway = gatewayRaw,
            gatewayMerchantId = merchantId,
            tokenizationType = if (gatewayRaw.equals("DIRECT", ignoreCase = true))
                GooglePayTokenizationType.DIRECT else GooglePayTokenizationType.PAYMENT_GATEWAY,
            publicKey = creds.optString("public_api_key", "").takeIf { it.isNotEmpty() },
            allowedCardNetworks = extraParams.optJSONArray("allowed_card_networks")?.toStringList()
                ?: GooglePayCredentials.DEFAULT_CARD_NETWORKS,
            allowedAuthMethods = extraParams.optJSONArray("allowed_auth_methods")?.toStringList()
                ?: GooglePayCredentials.DEFAULT_AUTH_METHODS,
            transactionInfo = parseTransactionInfo(order, merchant),
        )
    }

    private fun parseTransactionInfo(
        order: JSONObject?,
        merchant: JSONObject?,
    ): GooglePayCredentials.TransactionInfo? {
        val currency = order?.optString("currency", "") ?: return null
        val country = merchant?.optString("country", "") ?: return null
        if (currency.isEmpty() || country.isEmpty()) return null
        return GooglePayCredentials.TransactionInfo(
            totalPrice = "%.2f".format((order.optInt("total_amount", 0)) / 100.0),
            currencyCode = currency.uppercase(),
            countryCode = country.uppercase(),
        )
    }

    private fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }
}
