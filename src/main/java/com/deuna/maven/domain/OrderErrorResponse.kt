package com.deuna.maven.domain

import org.json.JSONObject

data class OrderErrorResponse(
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
        fun fromJson(json: JSONObject): OrderErrorResponse {
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
            return OrderErrorResponse(order, meta)
        }
    }
}
