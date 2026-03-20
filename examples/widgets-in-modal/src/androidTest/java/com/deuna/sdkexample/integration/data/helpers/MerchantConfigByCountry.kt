package com.deuna.sdkexample.integration.data.helpers

import com.deuna.sdkexample.integration.domain.CountryCode
import org.json.JSONObject
import kotlin.random.Random

/**
 * Country-specific merchant configuration
 */
data class MerchantConfig(
    val country: CountryCode,
    val city: String,
    val shortName: String,
    val timezone: String,
    val currency: String
)

/**
 * Provides country-specific merchant configurations
 */
object MerchantConfigByCountry {

    private val configs: Map<CountryCode, MerchantConfig> = mapOf(
        CountryCode.MX to MerchantConfig(
            country = CountryCode.MX,
            city = "CDMX",
            shortName = "AUTO",
            timezone = "America/Mexico_City",
            currency = "MXN"
        ),
        CountryCode.ES to MerchantConfig(
            country = CountryCode.ES,
            city = "Madrid",
            shortName = "AUTO",
            timezone = "Europe/Madrid",
            currency = "EUR"
        ),
        CountryCode.BR to MerchantConfig(
            country = CountryCode.BR,
            city = "Rio de Janeiro",
            shortName = "AUTO",
            timezone = "America/Sao_Paulo",
            currency = "BRL"
        ),
        CountryCode.CL to MerchantConfig(
            country = CountryCode.CL,
            city = "Santiago de Chile",
            shortName = "AUTO",
            timezone = "America/Santiago",
            currency = "CLP"
        ),
        CountryCode.CO to MerchantConfig(
            country = CountryCode.CO,
            city = "Bogota",
            shortName = "AUTO",
            timezone = "America/Bogota",
            currency = "COP"
        ),
        CountryCode.EC to MerchantConfig(
            country = CountryCode.EC,
            city = "Quito",
            shortName = "AUTO",
            timezone = "America/Guayaquil",
            currency = "USD"
        ),
        CountryCode.GT to MerchantConfig(
            country = CountryCode.GT,
            city = "Guatemala",
            shortName = "AUTO",
            timezone = "America/Guatemala",
            currency = "GTQ"
        ),
        CountryCode.PE to MerchantConfig(
            country = CountryCode.PE,
            city = "Lima",
            shortName = "AUTO",
            timezone = "America/Lima",
            currency = "PEN"
        ),
        CountryCode.HN to MerchantConfig(
            country = CountryCode.HN,
            city = "Tegucigalpa",
            shortName = "AUTO",
            timezone = "America/Tegucigalpa",
            currency = "HNL"
        ),
        CountryCode.CR to MerchantConfig(
            country = CountryCode.CR,
            city = "San Jose",
            shortName = "AUTO",
            timezone = "America/Costa_Rica",
            currency = "CRC"
        ),
        CountryCode.US to MerchantConfig(
            country = CountryCode.US,
            city = "New York",
            shortName = "AUTO",
            timezone = "America/New_York",
            currency = "USD"
        ),
        CountryCode.UY to MerchantConfig(
            country = CountryCode.UY,
            city = "Montevideo",
            shortName = "AUTO",
            timezone = "America/Montevideo",
            currency = "UYU"
        ),
        CountryCode.AR to MerchantConfig(
            country = CountryCode.AR,
            city = "Cordoba",
            shortName = "AUTO",
            timezone = "America/Argentina/Buenos_Aires",
            currency = "ARS"
        )
    )

    /**
     * Generates a random number for merchant naming (same logic as Postman)
     */
    private fun generateRandomNumber(): Int {
        return Random.nextInt(10000, 9009999)
    }

    /**
     * Creates a complete merchant request for the specified country
     * @param country Country code
     * @param customName Optional custom name (if not provided, uses AUTO_MERCHANT_{COUNTRY})
     * @param managedByDeuna Whether merchant is managed by DEUNA (default: false)
     * @return Complete MerchantRequest as JSONObject
     */
    fun createMerchantRequest(
        country: CountryCode,
        customName: String? = null,
        managedByDeuna: Boolean = false
    ): JSONObject? {
        val config = configs[country] ?: run {
            println("Unsupported country code: $country. Supported countries: ${configs.keys.joinToString(", ") { it.value }}")
            return null
        }

        val randomNumber = generateRandomNumber()
        val merchantName = customName ?: "AUTO_MERCHANT_${country.value} $randomNumber"

        return JSONObject().apply {
            put("country", config.country.value)
            put("city", config.city)
            put("name", merchantName)
            put("short_name", config.shortName)
            put("timezone", config.timezone)
            put("currency", config.currency)
            put("managed_by_deuna", managedByDeuna)
        }
    }
}
