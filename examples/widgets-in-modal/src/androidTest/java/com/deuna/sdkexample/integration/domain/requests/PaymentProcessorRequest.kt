package com.deuna.sdkexample.integration.domain.requests

import com.deuna.sdkexample.integration.domain.CountryCode
import org.json.JSONObject

// MARK: - Base Processor Interface

interface BaseProcessor {
    val name: String
    val paymentProcessorId: Int
    val enabled: Boolean
    val currencyIso3: String
    
    fun toJson(): JSONObject
}

// MARK: - Stripe Processor

data class StripeProcessor(
    override val name: String,
    override val paymentProcessorId: Int,
    override val enabled: Boolean,
    override val currencyIso3: String,
    val countryIso: String,
    val publicApiKey: String,
    val privateApiKey: String,
    val allowInstallments: Boolean,
    val enable3dsAuthentication: Boolean,
    val automaticCapture: Boolean?,
    val multiplePartialCapture: Boolean?
) : BaseProcessor {
    
    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("payment_processor_id", paymentProcessorId)
            put("enabled", enabled)
            put("currency_iso3", currencyIso3)
            put("country_iso", countryIso)
            put("public_api_key", publicApiKey)
            put("private_api_key", privateApiKey)
            put("allow_installments", allowInstallments)
            put("enable_3ds_authentication", enable3dsAuthentication)
            automaticCapture?.let { put("automatic_capture", it) }
            multiplePartialCapture?.let { put("multiple_partial_capture", it) }
        }
    }
}

// MARK: - Stripe Processor Configurations

object StripeProcessorConfig {

    /**
     * Default Stripe processor for testing
     */
    fun stripeProcessor(
        country: CountryCode,
        automaticCapture: Boolean = true
    ): StripeProcessor {
        val currencyIso3 = getCurrencyForCountry(country)

        return StripeProcessor(
            name = "STRIPE_DIRECT",
            paymentProcessorId = 50,
            enabled = true,
            currencyIso3 = currencyIso3,
            countryIso = country.value,
            publicApiKey = "pk_test_51KanPjHzvAJD6fk5wekkuEqfrT0qyzDrNw9Rlw2Nu4PBqlS8Vt1FtTgVDmXZxZ0eM4ccxBarVwUa5v0TICjSOyei00HmiJm3my",
            privateApiKey = "sk_test_51KanPjHzvAJD6fk5iA6c7M6Hv9pHZZirj5kzEUzGW7x5Xh4bTXIbs9kxOxdawiOQxiDoySOiRDOmtyqAm3loCQmZ001dBHZhy0",
            allowInstallments = true,
            enable3dsAuthentication = false,
            automaticCapture = automaticCapture,
            multiplePartialCapture = null
        )
    }

    /**
     * Stripe processor with authorize mode (no automatic capture)
     */
    fun stripeProcessorAuthorize(country: CountryCode): StripeProcessor {
        return stripeProcessor(country = country, automaticCapture = false)
    }

    private fun getCurrencyForCountry(country: CountryCode): String {
        return when (country) {
            CountryCode.MX -> "MXN"
            CountryCode.CO -> "COP"
            CountryCode.CL -> "CLP"
            CountryCode.PE -> "PEN"
            CountryCode.EC, CountryCode.US -> "USD"
            CountryCode.BR -> "BRL"
            CountryCode.AR -> "ARS"
            CountryCode.ES -> "EUR"
            CountryCode.GT -> "GTQ"
            CountryCode.HN -> "HNL"
            CountryCode.CR -> "CRC"
            CountryCode.UY -> "UYU"
        }
    }
}

// MARK: - Payment Processor Response

data class PaymentProcessorResponse(
    val data: PaymentProcessorData
) {
    companion object {
        fun fromJson(json: JSONObject): PaymentProcessorResponse {
            val dataJson = json.getJSONObject("data")
            return PaymentProcessorResponse(
                data = PaymentProcessorData.fromJson(dataJson)
            )
        }
    }
}

data class PaymentProcessorData(
    val id: String
) {
    companion object {
        fun fromJson(json: JSONObject): PaymentProcessorData {
            return PaymentProcessorData(
                id = json.getString("id")
            )
        }
    }
}
