package com.deuna.maven.element.domain

import ElementsResponse
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.DeunaSDK
import com.deuna.maven.checkout.*
import com.deuna.maven.shared.*
import org.json.JSONObject

/**
 * The ElementsBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class ElementsBridge(
  private val callbacks: ElementsCallbacks,
  private val closeOnEvents: Set<ElementsEvent>,
  private val closeElements: () -> Unit,
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
      Log.d("ElementsBridge", "postMessage: $e")
    }
  }

  private fun handleEvent(eventTypeString: String) {
    try {
      val json = JSONObject(eventTypeString)
      val eventData = ElementsResponse.fromJson(json)

      callbacks.eventListener?.invoke(eventData.type, eventData)
      when (eventData.type) {

        ElementsEvent.vaultSaveSuccess, ElementsEvent.cardSuccessfullyCreated -> {
          handleSuccess(eventData)
        }

        ElementsEvent.vaultFailed, ElementsEvent.cardCreationError, ElementsEvent.vaultSaveError -> eventData.data.metadata?.let {
          handleError(
            eventData
          )
        }

        ElementsEvent.vaultClosed -> closeElements()

        else -> {
          SDKLogger.debug("ElementsBridge Unhandled event: ${eventData.type}")
        }
      }
      eventData.let {
        if (closeOnEvents.contains(it.type)) {
          closeElements()
        }
      }
    } catch (e: Exception) {
      SDKLogger.debug("ElementsBridge JSONException: $e")
    }
  }

  private fun handleSuccess(data: ElementsResponse) {
    callbacks.onSuccess?.invoke(
      data
    )
  }

  private fun handleError(response: ElementsResponse) {
    callbacks.onError?.invoke(
      ElementsErrorMessage(
        DeunaSDKError.VAULT_SAVE_ERROR,
        response.data.order,
        response.data.user
      )
    )
  }
}