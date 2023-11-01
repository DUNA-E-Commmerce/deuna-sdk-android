package com.deuna.maven.domain

import android.app.Activity
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import java.util.Locale

/**
 * The DeUnaElementBridge class is used to receive messages from JavaScript code in a WebView.
 * The messages are parsed and the corresponding callbacks are called based on the event type.
 */
class DeUnaElementBridge(
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
            handleEvent(message.uppercase(Locale.getDefault()))
        } catch (e: Exception) {
            callbacks.onError?.invoke(null, message)
        }
    }

    private fun handleEvent(eventTypeString: String) {
        val eventType = CheckoutEvents.valueOf(eventTypeString)
        when (eventType) {
            CheckoutEvents.LINKCLOSE -> handleCloseEvent()
            CheckoutEvents.CHANGE_ADDRESS -> handleChangeAddressEvent()
            else -> handleOtherEvent(eventType)
        }
    }

    private fun handleCloseEvent() {
        webView.post {
            webView.visibility = View.GONE
        }
    }

    private fun handleChangeAddressEvent() {
        callbacks.onChangeAddress?.invoke(webView)
    }

    private fun handleOtherEvent(eventType: CheckoutEvents) {
        if (eventType in (closeOnEvents ?: emptyArray())) {
            callbacks.onClose?.invoke(webView)
        }
    }
}