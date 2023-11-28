package com.deuna.maven.checkout.domain

import OrderResponse
import org.json.JSONObject

data class DeunaErrorMessage(var message: String, var type: String, var order: OrderResponse.Data.Order?, var user: OrderResponse.Data.User?) {
    companion object {
        fun parseError(message: String, type: String,  order: OrderResponse.Data.Order,  user: OrderResponse.Data.User): DeunaErrorMessage {
            return DeunaErrorMessage(message, type, order, user)
        }
    }
}