package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.payment_widget.domain.PaymentWidgetBridge
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.web_views.deuna.DeunaWebView


class PaymentWidgetDialogFragment(
    private val url: String,
    val callbacks: PaymentWidgetCallbacks,
) : BaseDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.deuna_webview_container, container, false)
        baseWebView = view.findViewById(R.id.deuna_webview)
        val deunaWebView = baseWebView as DeunaWebView
//        deunaWebView.bridge = PaymentWidgetBridge(
//            deunaWebView = deunaWebView,
//            callbacks = callbacks
//        )
//        baseWebView.loadUrl(url)
        return view
    }
}