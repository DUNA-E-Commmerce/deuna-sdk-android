package com.deuna.maven.element.domain

import ElementResponse
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.DeunaSDK
import org.json.JSONObject

/**
 * The DeUnaElementBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class ElementsBridge(
    private val callbacks: ElementsCallbacks,
    private val closeOnEvents: ArrayList<String>? = null
) {
    /**
     * The postMessage function is called when a message is received from JavaScript code in a WebView.
     * The message is parsed and the corresponding callbacks are called based on the event type.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            handleEvent(message)
        } catch (e: Exception) {
            Log.d("DeUnaElementBridge", "postMessage: $e")
        }
    }

    private fun handleEvent(eventTypeString: String) {
        try {
            val json = JSONObject(eventTypeString)
            val eventData = ElementResponse.fromJson(json)
            Log.d("DeUnaElementBridge", "handleEvent: $json")
            callbacks.eventListener?.invoke(eventData.type, eventData)
            when (eventData.type) {
                ElementsEvent.vaultFailed -> eventData.data.metadata?.let {
                    handleError(
                        it.errorMessage,
                        "vaultFailed",
                        eventData
                    )
                }

                ElementsEvent.cardCreationError -> eventData.data.metadata?.let {
                    handleError(
                        it.errorMessage,
                        "cardCreationError",
                        eventData
                    )
                }

                ElementsEvent.vaultSaveError -> eventData.data.metadata?.let {
                    handleError(
                        it.errorMessage,
                        "vaultSaveError",
                        eventData
                    )
                }

                ElementsEvent.vaultSaveSuccess -> handleSuccess(eventData)
                ElementsEvent.vaultClosed -> handleCloseEvent()
                ElementsEvent.cardSuccessfullyCreated -> handleSuccess(eventData)
                else -> {
                    Log.d("DeUnaElementBridge", "Unhandled event: ${eventData.type}")
                    eventData.let {
                        if (closeOnEvents?.contains(it.type.value) == true) {
                            callbacks.onClose?.invoke()
                            DeunaSDK.closeElements()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("DeUnaElementBridge", "handleEvent: $e")
        }
    }

    private fun handleCloseEvent() {
        callbacks.onClose?.invoke()
    }

    private fun handleSuccess(data: ElementResponse) {
        callbacks.onSuccess?.invoke(
            data
        )
    }

    private fun handleError(message: String, type: String, response: ElementResponse) {
        callbacks.onError?.invoke(
            ElementsErrorMessage(
                message,
                type, // Internet Connection // Checkout failed
                response.data.order,
                response.data.user
            )
        )
    }


}