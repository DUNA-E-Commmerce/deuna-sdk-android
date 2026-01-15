package com.deuna.sdkexample.integration.domain.requests

import org.json.JSONObject

data class CreateStoreRequest(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("address", address)
            put("latitude", latitude)
            put("longitude", longitude)
            put("is_default", isDefault)
        }
    }
}
