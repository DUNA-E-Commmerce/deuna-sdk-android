package com.deuna.maven.payment_widget

import android.content.Context
import android.util.Log
import com.deuna.maven.checkout.domain.CheckoutEvent
import com.deuna.maven.closePaymentWidget
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.PaymentWidgetErrorType
import com.deuna.maven.shared.WebViewBridge
import org.json.JSONException
import org.json.JSONObject

class PaymentWidgetBridge(
    private val context: Context,
    private val callbacks: PaymentWidgetCallbacks?,
) : WebViewBridge() {
    override fun handleEvent(message: String) {
        try {
            val json = JSONObject(message)

            val type = json.getString("type")

            if (type == "onBinDetected") {
                handleCardBinDetected(json)
                return
            }

            val eventData = CheckoutResponse.fromJson(json)

            when (eventData.type) {
                CheckoutEvent.purchase, CheckoutEvent.apmSuccess -> {
                    callbacks?.onSuccess?.invoke(eventData.data)
                }

                CheckoutEvent.purchaseRejected, CheckoutEvent.purchaseError -> {
                    handleError(PaymentWidgetErrorType.PAYMENT_ERROR)
                }

                CheckoutEvent.linkFailed, CheckoutEvent.linkCriticalError -> {
                    handleError(PaymentWidgetErrorType.INITIALIZATION_FAILED)
                }

                CheckoutEvent.linkClose -> {
                    closePaymentWidget(context)
                    callbacks?.onCanceled?.invoke()
                }

                CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
                    // No action required for these events
                }

                else -> {
                    DeunaLogs.debug("PaymentWidgetBridge Unhandled event: $eventData")
                }
            }
        } catch (e: JSONException) {
            DeunaLogs.debug("PaymentWidgetBridge JSONException: $e")
        }
    }


    private fun handleCardBinDetected(json: JSONObject) {
        val metadata = json.getJSONObject("data").getJSONObject("metadata")
        callbacks?.onCardBinDetected?.invoke(
            PaymentWidgetCallbacks.CardBinMetadata.fromJson(metadata),
            { completition -> completition(null) },
        )
    }


    private fun handleError(type: PaymentWidgetErrorType) {
        callbacks?.onError?.invoke(
            type
        )
    }
}