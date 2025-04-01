package com.deuna.maven.web_views.dialog_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deuna.maven.checkout.domain.CheckoutBridge
import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.web_views.dialog_fragments.base.DeunaDialogFragment

class CheckoutWidgetDialogFragment(
    val callbacks: CheckoutCallbacks,
    val closeEvents: Set<CheckoutEvent> = emptySet(),
) : DeunaDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        deunaWidget.bridge = CheckoutBridge(
            deunaWidget = deunaWidget,
            callbacks = callbacks,
            closeEvents = closeEvents,
            onCloseByUser = {
                callbacks.onClosed?.invoke(CloseAction.userAction)
                dismiss()
            },
            onCloseByEvent = {
                callbacks.onClosed?.invoke(CloseAction.systemAction)
                dismiss()
            },
        )
        return view
    }

    fun loadUrl(url: String) {
        deunaWidget.loadUrl(url)
    }
}