package com.deuna.maven.checkout.domain

import org.json.JSONObject

data class OrderSuccessResponse(
    val order: Order
) {
    data class Metadata(
        val errorCode: String,
        val errorMessage: String
    )

    data class Order(
        val order_id: String,
        val currency: String,
        val itemsTotalAmoun: Int,
        val subTotal : Int,
        val totalAmount : Int,
    )

    companion object {
        fun fromJson(json: JSONObject): OrderSuccessResponse {
            val orderData = json.getJSONObject("order")
            val order = Order(
                orderData.getString("order_id"),
                orderData.getString("currency"),
                orderData.getInt("items_total_amount"),
                orderData.getInt("sub_total"),
                orderData.getInt("total_amount")
            )
            return OrderSuccessResponse(order)
        }
    }
}
