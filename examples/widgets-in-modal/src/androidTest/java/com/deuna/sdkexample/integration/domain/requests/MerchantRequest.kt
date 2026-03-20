package com.deuna.sdkexample.integration.domain.requests

import com.deuna.sdkexample.integration.domain.CountryCode
import org.json.JSONObject

data class MerchantRequest(
    val country: String,
    val city: String,
    val name: String,
    val shortName: String,
    val timezone: String,
    val currency: String,
    val managedByDeuna: Boolean = true
) {
    constructor(
        country: CountryCode,
        city: String,
        name: String,
        shortName: String,
        timezone: String,
        currency: String,
        managedByDeuna: Boolean = true
    ) : this(
        country = country.value,
        city = city,
        name = name,
        shortName = shortName,
        timezone = timezone,
        currency = currency,
        managedByDeuna = managedByDeuna
    )

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("country", country)
            put("city", city)
            put("name", name)
            put("short_name", shortName)
            put("timezone", timezone)
            put("currency", currency)
            put("managed_by_deuna", managedByDeuna)
        }
    }
}
