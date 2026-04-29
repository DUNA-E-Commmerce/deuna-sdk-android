package com.deuna.maven.wallets

import com.deuna.maven.shared.Environment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder

internal object TokenizeWalletCard {

    private val httpClient = OkHttpClient()

    /**
     * POST {checkoutBaseUrl}/users/{userId}/cards
     * Body: { "google_pay": { "paymentData": paymentData }, "credential_source": "google_pay" }
     */
    fun tokenize(
        environment: Environment,
        publicApiKey: String,
        userId: String,
        userToken: String,
        paymentDataJson: String,
    ): JSONObject {
        val url = "${environment.checkoutBaseUrl}/users/${URLEncoder.encode(userId, "UTF-8")}/cards"

        val paymentData = JSONObject(paymentDataJson)
        val body = JSONObject().apply {
            put("google_pay", JSONObject().apply { put("paymentData", paymentData) })
            put("credential_source", "google_pay")
        }

        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $userToken")
            .header("x-api-key", publicApiKey)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty tokenization response (${response.code})")

        return JSONObject(responseBody)
    }
}
