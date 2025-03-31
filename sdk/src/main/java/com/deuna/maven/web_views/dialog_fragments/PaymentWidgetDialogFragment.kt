package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.R
import com.deuna.maven.payment_widget.domain.PaymentWidgetBridge
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.enums.CloseAction
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
        val view = inflater.inflate(R.layout.new_tab_webview_container, container, false)
        baseWebView = view.findViewById(R.id.new_tab_webview)
        val deunaWebView = baseWebView as DeunaWebView
        deunaWebView.bridge = PaymentWidgetBridge(
            deunaWebView = deunaWebView,
            callbacks = callbacks,
            onClosedByUser = {
                callbacks.onClosed?.invoke(CloseAction.userAction)
                dismiss()
            }
        )
        baseWebView.loadUrl(url)
        return view
    }
}