package com.deuna.maven.checkout.domain

import CheckoutResponse
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.DeunaSDK
import com.deuna.maven.checkout.CheckoutCallbacks
import com.deuna.maven.checkout.CheckoutEvent
import com.deuna.maven.shared.*
import org.json.JSONException
import org.json.JSONObject

/**
 * The CheckoutBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class CheckoutBridge(
  private val callbacks: CheckoutCallbacks,
  private val closeOnEvents: Set<CheckoutEvent>,
  private val closeCheckout: () -> Unit,
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
            DeunaSDKError.PAYMENT_ERROR,
            eventData
          )
        }

        CheckoutEvent.linkFailed, CheckoutEvent.purchaseError -> {
          handleError(DeunaSDKError.CHECKOUT_INITIALIZATION_FAILED, eventData)
        }

        CheckoutEvent.linkClose -> {
          closeCheckout()
        }

        CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
          // No action required for these events
        }

        else -> {
          Log.d("CheckoutBridge", "Unhandled event: $eventData")
          eventData.let {
            if (closeOnEvents.contains(it.type)) {
              closeCheckout()
            }
          }
        }
      }
    } catch (e: JSONException) {
      Log.d("CheckoutBridge", "JSONException: $e")
    }
  }

  private fun handleCloseActivity(data: CheckoutResponse, type: CheckoutEvent) {
    callbacks.eventListener?.invoke(type, data)
  }

  private fun handleError(type: DeunaSDKError, response: CheckoutResponse) {
    callbacks.onError?.invoke(
      DeunaErrorMessage(

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