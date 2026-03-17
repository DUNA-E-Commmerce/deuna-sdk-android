package com.deuna.sdkexample.integration.domain.responses

import org.json.JSONObject

data class MerchantApplicationResponse(
    val id: String,
    val name: String,
    val publicKey: String,
    val privateKey: String,
    val signingSecret: String,
    val isSandbox: Boolean,
    val status: String,
    val channel: String,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromJson(json: JSONObject): MerchantApplicationResponse {
            return MerchantApplicationResponse(
                id = json.getString("id"),
                name = json.getString("name"),
                publicKey = json.getString("public_key"),
                privateKey = json.getString("private_key"),
                signingSecret = json.getString("signing_secret"),
                isSandbox = json.getBoolean("is_sandbox"),
                status = json.getString("status"),
                channel = json.getString("channel"),
                createdAt = json.getString("created_at"),
                updatedAt = json.getString("updated_at")
            )
        }
    }
}
