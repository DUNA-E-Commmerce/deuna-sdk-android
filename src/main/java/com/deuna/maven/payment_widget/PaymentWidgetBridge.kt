package com.deuna.maven.payment_widget

import android.content.Context
import com.deuna.maven.shared.WebViewBridge

class PaymentWidgetBridge(
    private val context: Context,
    private val callbacks: PaymentWidgetCallbacks?,
) : WebViewBridge() {
    override fun handleEvent(message: String) {
        TODO("Not yet implemented")
    }
}