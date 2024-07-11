package com.deuna.maven.checkout.domain

import com.deuna.maven.*
import com.deuna.maven.shared.*
import org.json.*

@Suppress("UNCHECKED_CAST")
class CheckoutBridge(
    private val sdkInstanceId: Int,
    private val callbacks: CheckoutCallbacks?,
    private val closeEvents: Set<CheckoutEvent>,
) : WebViewBridge(name = "android") {
    override fun handleEvent(message: String) {

        try {
            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json



            if (type == null || data == null) {
                return
            }

            val event = CheckoutEvent.valueOf(type)
            callbacks?.eventListener?.invoke(event, data)

            when (event) {
                CheckoutEvent.purchase, CheckoutEvent.apmSuccess -> {
                    callbacks?.onSuccess?.invoke(data)
                }

                CheckoutEvent.purchaseRejected, CheckoutEvent.purchaseError -> {
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.PAYMENT_ERROR,
                        data = data
                    )
                    if (error != null) {
                        callbacks?.onError?.invoke(error)
                    }
                }

                CheckoutEvent.linkFailed, CheckoutEvent.linkCriticalError -> {
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.INITIALIZATION_FAILED,
                        data = data
                    )
                    if (error != null) {
                        callbacks?.onError?.invoke(error)
                    }
                }

                CheckoutEvent.linkClose -> {
                    closeCheckout(sdkInstanceId)
                    callbacks?.onCanceled?.invoke()
                }

                CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
                    // No action required for these events
                }

                else -> {
                    DeunaLogs.debug("CheckoutBridge Unhandled event: $event")
                }
            }

            if (closeEvents.contains(event)) {
                closeCheckout(sdkInstanceId)
            }
        } catch (e: JSONException) {
            DeunaLogs.debug("CheckoutBridge JSONException: $e")
        }
    }
}
