package com.deuna.maven.checkout.domain

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.checkout.Callbacks
import com.deuna.maven.checkout.CheckoutEvents
import org.json.JSONException
import org.json.JSONObject

/**
 * The DeUnaBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class DeUnaBridge(
    private val activity: Activity,
    private val callbacks: Callbacks
) {
    /**
     * The postMessage function is called when a message is received from JavaScript code in a WebView.
     * The message is parsed and the corresponding callbacks are called based on the event type.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            Log.d("DeUnaBridge", message)
            val json = JSONObject(message)
            when (val eventType = CheckoutEvents.valueOf(json.getString("type"))) {
                CheckoutEvents.purchase -> {
                    callbacks.onSuccess?.invoke(
                        OrderSuccessResponse.fromJson(json.getJSONObject("data"))
                    )
                }
                CheckoutEvents.purchaseRejected -> {
                    callbacks.onError?.invoke(
                        OrderErrorResponse.fromJson(json.getJSONObject("data")),
                        null
                    )
                }
                CheckoutEvents.linkFailed -> {
                    callbacks.onError?.invoke(
                        OrderErrorResponse.fromJson(json.getJSONObject("data")),
                        null
                    )
                }
                CheckoutEvents.paymentClick -> {
                    Log.d("DeUnaBridge", "PAYMENT_CLICK")
                }
                CheckoutEvents.paymentProcessing -> {
                    Log.d("DeUnaBridge", "PAYMENT_PROCESSING")
                }
                CheckoutEvents.purchaseError -> {
                    callbacks.onError?.invoke(
                        OrderErrorResponse.fromJson(json.getJSONObject("data")),
                        null
                    )
                }
                CheckoutEvents.changeAddress -> {
                    callbacks.onClose?.invoke(activity)
                }

                else -> {
                    callbacks.onClose?.invoke(activity)
                }
            }
        } catch (e: JSONException) {
            Log.d("DeUnaBridge", "JSONException: $e")
        }
    }

//    private fun handleEvent(eventTypeString: String) {
//        val json = JSONObject(eventTypeString)
//        when (val eventType = CheckoutEvents.valueOf(json.getString("type"))) {
//            CheckoutEvents.LINKCLOSE -> handleCloseEvent()
//            CheckoutEvents.CHANGE_ADDRESS -> handleChangeAddressEvent()
//            else -> handleOtherEvent(eventType)
//        }
//    }

//    private fun handleCloseEvent() {
//        webView.post {
//            webView.visibility = View.GONE
//        }
//    }
//
//    private fun handleChangeAddressEvent() {
//        callbacks.onChangeAddress?.invoke(webView)
//    }
//
//    private fun handleOtherEvent(eventType: CheckoutEvents) {
//        if (eventType in (closeOnEvents ?: emptyArray())) {
//            callbacks.onClose?.invoke(webView)
//        }
//    }
}