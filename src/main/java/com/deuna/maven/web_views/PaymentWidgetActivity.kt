package com.deuna.maven.web_views

import android.os.Bundle
import com.deuna.maven.payment_widget.PaymentWidgetBridge
import com.deuna.maven.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.PaymentWidgetErrorType
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.web_views.base.BaseWebViewActivity

class PaymentWidgetActivity() : BaseWebViewActivity() {
    companion object {
        const val EXTRA_URL = "EXTRA_URL"
        private var callbacks: PaymentWidgetCallbacks? = null

        /**
         * Set the callbacks object to receive element events.
         */
        fun setCallbacks(callbacks: PaymentWidgetCallbacks) {
            this.callbacks = callbacks
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract the URL from the intent
        val url = intent.getStringExtra(ElementsActivity.EXTRA_URL)!!

        // Load the provided URL
        loadUrl(url)
    }

    override fun getBridge(): WebViewBridge {
      return  PaymentWidgetBridge(context = this, callbacks = callbacks)
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(PaymentWidgetErrorType.NO_INTERNET_CONNECTION)
    }

    override fun onCanceledByUser() {
        callbacks?.onCanceled?.invoke()
    }

    override fun onDestroy() {
        // Notify callbacks about activity closure
        callbacks?.onClosed?.invoke()
        super.onDestroy()
    }
}