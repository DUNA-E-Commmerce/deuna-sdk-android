package com.deuna.maven.web_views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.deuna.maven.payment_widget.PaymentWidgetBridge
import com.deuna.maven.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.PaymentWidgetErrorType
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.utils.BroadcastReceiverUtils
import com.deuna.maven.web_views.base.BaseWebViewActivity
import org.json.JSONObject

class PaymentWidgetActivity() : BaseWebViewActivity() {
    companion object {
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_CUSTOM_STYLES = "CUSTOM_STYLES"
        const val SEND_CUSTOM_STYLES_BROADCAST_RECEIVER_ACTION =
            "com.deuna.maven.SEND_CUSTOM_STYLES_BROADCAST_RECEIVER_ACTION"

        private var callbacks: PaymentWidgetCallbacks? = null

        /**
         * Set the callbacks object to receive element events.
         */
        fun setCallbacks(callbacks: PaymentWidgetCallbacks) {
            this.callbacks = callbacks
        }
    }

    // broadcast receiver to listen when DeunaSDK.setCustomStyles is called
    private val setCustomStylesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            sendCustomStyles(intent.getStringExtra(EXTRA_CUSTOM_STYLES)!!)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BroadcastReceiverUtils.register(
            context = this,
            broadcastReceiver = setCustomStylesReceiver,
            action = SEND_CUSTOM_STYLES_BROADCAST_RECEIVER_ACTION
        )

        // Extract the URL from the intent
        val url = intent.getStringExtra(ElementsActivity.EXTRA_URL)!!

        // Load the provided URL
        loadUrl(url)
    }

    override fun getBridge(): WebViewBridge {
        return PaymentWidgetBridge(context = this, callbacks = callbacks)
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(PaymentWidgetErrorType.NO_INTERNET_CONNECTION)
    }

    override fun onCanceledByUser() {
        callbacks?.onCanceled?.invoke()
    }

    override fun onDestroy() {
        unregisterReceiver(setCustomStylesReceiver)
        // Notify callbacks about activity closure
        callbacks?.onClosed?.invoke()
        super.onDestroy()
    }

    /// send the custom styles to the DEUNA Now link
    fun sendCustomStyles(dataAsJsonString: String) {
        val jsonString = """{ "type": "setCustomCSS","data": $dataAsJsonString}"""
        webView.evaluateJavascript(
            "javascript:postMessage(JSON.stringify($jsonString),'*')",
            null
        );
    }
}