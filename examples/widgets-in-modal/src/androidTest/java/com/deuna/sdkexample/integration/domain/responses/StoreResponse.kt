package com.deuna.sdkexample.integration.domain.responses

import org.json.JSONObject

data class StoreResponse(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val updatedAt: String,
    val isDefault: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): StoreResponse {
            return StoreResponse(
                id = json.getString("id"),
                name = json.getString("name"),
                address = json.getString("address"),
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                createdAt = json.getString("created_at"),
                updatedAt = json.getString("updated_at"),
                isDefault = json.getBoolean("is_default")
            )
        }
    }
}
