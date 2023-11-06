package com.deuna.maven.client

import retrofit2.Callback

fun sendOrder(orderToken: String, apiKey: String, callback: Callback<Any>) {
    val call = RetrofitClient.instance.getOrder(orderToken, apiKey)

    call.enqueue(callback)
}