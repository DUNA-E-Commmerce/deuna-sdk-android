package com.deuna.sdkexample.integration.domain.requests

import org.json.JSONObject

data class CreateMerchantApplicationRequest(
    val name: String,
    val isSandbox: Boolean,
    val expireAt: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("is_sandbox", isSandbox)
            put("expires_at", expireAt)
        }
    }
}
