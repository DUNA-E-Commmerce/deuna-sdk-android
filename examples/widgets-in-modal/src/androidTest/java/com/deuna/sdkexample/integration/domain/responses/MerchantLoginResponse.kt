package com.deuna.sdkexample.integration.domain.responses

import org.json.JSONObject

data class MerchantLoginResponse(
    val code: Int,
    val expire: String,
    val token: String
) {
    companion object {
        fun fromJson(json: JSONObject): MerchantLoginResponse {
            return MerchantLoginResponse(
                code = json.getInt("code"),
                expire = json.getString("expire"),
                token = json.getString("token")
            )
        }
    }
}
