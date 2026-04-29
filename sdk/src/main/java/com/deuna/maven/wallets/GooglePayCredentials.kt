package com.deuna.maven.wallets

internal data class GooglePayCredentials(
    val merchantId: String = "",
    val merchantName: String = "",
    val gateway: String = "",
    val gatewayMerchantId: String = "",
    val allowedCardNetworks: List<String> = DEFAULT_CARD_NETWORKS,
    val allowedAuthMethods: List<String> = DEFAULT_AUTH_METHODS,
    val transactionInfo: TransactionInfo? = null,
) {
    data class TransactionInfo(
        val totalPrice: String,
        val currencyCode: String,
        val countryCode: String,
    )

    companion object {
        val DEFAULT_CARD_NETWORKS = listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA")
        val DEFAULT_AUTH_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    }
}

internal data class WalletFetchResult(
    val googlePayCredentials: GooglePayCredentials?,
    val userToken: String?,
    val userId: String?,
)
