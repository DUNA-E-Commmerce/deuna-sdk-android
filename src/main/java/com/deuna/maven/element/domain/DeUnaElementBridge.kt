package com.deuna.maven.element.domain

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

/**
 * The DeUnaElementBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class DeUnaElementBridge(
    private val callbacks: ElementCallbacks,
    private val activity: Activity
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
        }
    }

    private fun handleEvent(eventTypeString: String) {
        try {
            val json = JSONObject(eventTypeString)
            val eventType = ElementEvent.valueOf(json.getString("type"))
            Log.d("DeUnaElementBridge", "handleEvent: $eventType")
            when (eventType) {
                ElementEvent.vaultSaveClick -> Log.d("DeUnaElementBridge", "VAULT_SAVE_CLICK")
                ElementEvent.vaultStarted -> Log.d("DeUnaElementBridge", "VAULT_STARTED")
                ElementEvent.vaultFailed -> handleError(json)
                ElementEvent.cardCreationError -> handleError(json)
                ElementEvent.vaultSavingError -> handleError(json)
                ElementEvent.vaultSavingSuccess -> handleSuccess(json)
                ElementEvent.vaultProcessing -> Log.d("DeUnaElementBridge", "VAULT_PROCESSING")
                ElementEvent.vaultClosed -> handleCloseEvent(activity)
                ElementEvent.cardSuccessfullyCreated -> handleSuccess(json)
                else -> Log.d("DeUnaElementBridge", "Unhandled event: $eventType")
            }
        } catch (e: Exception) {
            Log.d("DeUnaElementBridge", "handleEvent: $e")
        }
    }

    private fun handleCloseEvent(activity: Activity) {
        Log.d("DeUnaElementBridge", "handleCloseEvent")
        activity.finish()
    }

    private fun handleSuccess(jsonObject: JSONObject) {
        Log.d("DeUnaElementBridge", "handleSuccess: $jsonObject")
        callbacks.onSuccess?.invoke(
            ElementSuccessResponse.fromJson(jsonObject.getJSONObject("data"))
        )
    }

    private fun handleError(jsonObject: JSONObject) {
        callbacks.onError?.invoke(
            ElementErrorResponse.fromJson(jsonObject.getJSONObject("data")),
            null
        )
    }

    private fun handleChangeAddressEvent() {
    }

//    private fun handleOtherEvent(eventType: ElementEvent) {
//        if (eventType in (closeOnEvents ?: emptyArray())) {
//            callbacks.onClose?.invoke(webView)
//        }
//    }
}