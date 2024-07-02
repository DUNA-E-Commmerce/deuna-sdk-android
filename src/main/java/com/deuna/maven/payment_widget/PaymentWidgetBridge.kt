package com.deuna.maven.payment_widget

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
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
    private val webView: WebView
) : WebViewBridge(name = "android") {


    private val refetchOrderRequests = mutableMapOf<Int, (CheckoutResponse.Data.Order?) -> Unit>()
    private var refetchOrderRequestId = 0

    @JavascriptInterface
    fun consoleLog(message: String) {
        DeunaLogs.info("ConsoleLogBridge: $message")
    }

    @JavascriptInterface
    fun onRefetchOrder(message: String) {
        handleEvent(message)
    }

    override fun handleEvent(message: String) {
        try {
            val json = JSONObject(message)

            val type = json.getString("type")

            if (type == "onBinDetected") {
                handleCardBinDetected(json)
                return
            }

            if (type == "onInstallmentSelected") {
                handleInstallmentSelected(json)
                return
            }

            if (type == "refetchOrder") {
                handleOnRefetchOrder(json)
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

                else -> {}
            }
        } catch (e: JSONException) {
            DeunaLogs.debug("PaymentWidgetBridge JSONException: $e")
        }
    }


    private fun handleCardBinDetected(json: JSONObject) {
        val data = json.getJSONObject("data")
        if (!data.has("metadata")) {
            callbacks?.onCardBinDetected?.invoke(
                null
            ) { callback -> refetchOrder(callback) }
            return
        }

        val metadata = data.getJSONObject("metadata")
        callbacks?.onCardBinDetected?.invoke(
            PaymentWidgetCallbacks.CardBinMetadata.fromJson(metadata),
        ) { callback -> refetchOrder(callback) }
    }

    private fun handleInstallmentSelected(json: JSONObject) {
        val data = json.getJSONObject("data")
        if (!data.has("metadata")) {
            callbacks?.onInstallmentSelected?.invoke(
                null
            ) { callback -> refetchOrder(callback) }
            return
        }

        val metadata = data.getJSONObject("metadata")
        callbacks?.onInstallmentSelected?.invoke(
            PaymentWidgetCallbacks.InstallmentMetadata.fromJson(metadata),
        ) { callback -> refetchOrder(callback) }
    }

    private fun handleOnRefetchOrder(json: JSONObject) {
        val requestId = json.getInt("requestId")
        if (!refetchOrderRequests.contains(requestId)) {
            return
        }

        val data = json.optJSONObject("data")
        refetchOrderRequests[requestId]?.invoke(
            if (data != null) CheckoutResponse.fromJson(json).data.order else null
        )
        refetchOrderRequests.remove(requestId)
    }


    private fun handleError(type: PaymentWidgetErrorType) {
        callbacks?.onError?.invoke(
            type
        )
    }


    private fun refetchOrder(callback: (CheckoutResponse.Data.Order?) -> Unit) {
        refetchOrderRequestId++
        refetchOrderRequests[refetchOrderRequestId] = callback

        webView.evaluateJavascript(
            """
        (function() {
            function refetchOrder( callback) {
                deunaRefetchOrder()
                    .then(data => {
                        callback({type:"refetchOrder", data: data , requestId: $refetchOrderRequestId });
                    })
                    .catch(error => {
                        callback({type:"refetchOrder", data: null , requestId: $refetchOrderRequestId });
                    });
            }

            refetchOrder(function(result) {
                android.onRefetchOrder(JSON.stringify(result));
            });
        })();
        """.trimIndent(), null
        );
    }
}