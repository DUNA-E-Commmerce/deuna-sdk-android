package com.deuna.maven.domain

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONException
import org.json.JSONObject

/**
 * The DeUnaBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class DeUnaBridge(
    private val callbacks: Callbacks,
    private val webView: WebView,
    private val closeOnEvents: Array<CheckoutEvents>? = null
) {
    /**
     * The postMessage function is called when a message is received from JavaScript code in a WebView.
     * The message is parsed and the corresponding callbacks are called based on the event type.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            val json = JSONObject(message)
            when (val eventType = CheckoutEvents.valueOf(json.getString("type"))) {
                CheckoutEvents.PURCHASE_REJECTED -> {
                    callbacks.onError?.invoke(
                        OrderErrorResponse.fromJson(json.getJSONObject("data")),
                        null
                    )
                }
                CheckoutEvents.PURCHASE_SUCCESS -> {
                    callbacks.onSuccess?.invoke(
                        OrderSuccessResponse.fromJson(json.getJSONObject("data"))
                    )
                }
                CheckoutEvents.CHANGE_ADDRESS -> {
                    callbacks.onChangeAddress?.invoke(webView)
                }
                else -> {
                    if (eventType in (closeOnEvents ?: emptyArray())) {
                        callbacks.onClose?.invoke(webView)
                    }
                }
            }
        } catch (e: JSONException) {
            callbacks.onError?.invoke(null, message)
        }
    }
}