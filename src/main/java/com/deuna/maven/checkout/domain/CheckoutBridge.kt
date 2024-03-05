package com.deuna.maven.checkout.domain

import CheckoutResponse
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.DeunaSDK
import com.deuna.maven.checkout.CheckoutCallbacks
import com.deuna.maven.checkout.CheckoutEvent
import org.json.JSONException
import org.json.JSONObject

/**
 * The DeUnaBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class CheckoutBridge(
    private val callbacks: CheckoutCallbacks,
    private val closeOnEvents: ArrayList<String>? = null,
    private val closeCheckout: () -> Unit
) {
    /**
     * Called when the activity is starting.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        val eventData: CheckoutResponse?
        try {
            val json = JSONObject(message)
            eventData = CheckoutResponse.fromJson(json)
            callbacks.eventListener?.invoke(eventData.type, eventData)
            when (eventData.type) {
                CheckoutEvent.purchase, CheckoutEvent.apmSuccess -> {
                    handleSuccess(eventData)
                }

                CheckoutEvent.purchaseRejected -> {
                    handleError(
                        "An error ocurred while processing payment",
                        "purchaseRejected",
                        eventData
                    )
                }

                CheckoutEvent.linkFailed, CheckoutEvent.purchaseError -> {
                    handleError("Failed to initialize the checkout", "checkoutError", eventData)
                }

                CheckoutEvent.linkClose -> {
                    handleClose()
                }

                CheckoutEvent.changeAddress -> {
                    handleCloseActivity(eventData, eventData.type)
                }

                CheckoutEvent.changeCart -> {
                    handleCloseActivity(eventData, eventData.type)
                }

                else -> {
                    Log.d("DeUnaBridge", "Unhandled event: $eventData")
                    eventData.let {
                        if (closeOnEvents?.contains(it.type.value) == true) {
                            callbacks.onClose?.invoke()
                            closeCheckout()
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            Log.d("DeUnaBridge", "JSONException: $e")
        }
    }

    private fun handleCloseActivity(data: CheckoutResponse, type: CheckoutEvent) {
        callbacks.eventListener?.invoke(type, data)
    }

    private fun handleClose() {
        callbacks.onClose?.invoke()
    }

    private fun handleError(message: String, type: String, response: CheckoutResponse) {
        callbacks.onError?.invoke(
            DeunaErrorMessage(
                message,
                type, // Internet Connection // Checkout failed
                response.data.order,
                response.data.user
            )
        )
    }

    private fun handleSuccess(data: CheckoutResponse) {
        callbacks.onSuccess?.invoke(
            data
        )
    }

}