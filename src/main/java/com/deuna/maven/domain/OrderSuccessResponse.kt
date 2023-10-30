package com.deuna.maven.domain

import org.json.JSONObject

data class OrderSuccessResponse(
    val order: Order,
    val metadata: Metadata
) {
    data class Metadata(
        val errorCode: String,
        val errorMessage: String
    )

    data class Order(
        val order_id: String,
        val currency: String
    )

    companion object {
        fun fromJson(json: JSONObject): OrderSuccessResponse {
            val metadata = json.getJSONObject("metadata")
            val orderData = json.getJSONObject("order")
            val meta= Metadata(
                metadata.getString("errorCode"),
                metadata.getString("errorMessage")
            )
            val order = Order(
                orderData.getString("order_id"),
                orderData.getString("currency"),
            )
            return OrderSuccessResponse(order, meta)
        }
    }
}
