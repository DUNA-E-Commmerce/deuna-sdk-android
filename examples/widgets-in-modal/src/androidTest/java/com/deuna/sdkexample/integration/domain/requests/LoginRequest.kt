package com.deuna.sdkexample.integration.domain.requests

import org.json.JSONObject

data class LoginRequest(
    val username: String,
    val password: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("username", username)
            put("password", password)
        }
    }
}
