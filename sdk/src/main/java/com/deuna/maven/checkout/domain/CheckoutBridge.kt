package com.deuna.maven.checkout.domain

import android.webkit.JavascriptInterface
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.deuna.DeunaWebView
import org.json.*

@Suppress("UNCHECKED_CAST")
class CheckoutBridge(
    val deunaWebView: DeunaWebView,
    private val callbacks: CheckoutCallbacks,
    private val closeEvents: Set<CheckoutEvent>,
    val onCloseByEvent: () -> Unit,
    onCloseByUser: () -> Unit,
    onNoInternet: () -> Unit,
    onWebViewError: () -> Unit
) : WebViewBridge(
    name = "android",
    onCloseByUser = onCloseByUser,
    onNoInternet = onNoInternet,
    onWebViewError = onWebViewError
) {

    @JavascriptInterface
    fun consoleLog(message: String) {
        DeunaLogs.info("ConsoleLogBridge: $message")
    }

    override fun handleEvent(message: String) {

        try {
            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json

            if (type == null || data == null) {
                return
            }

            val event = CheckoutEvent.valueOf(type)
            callbacks.onEventDispatch?.invoke(event, data)

            when (event) {
                CheckoutEvent.purchase -> {
                    deunaWebView.closeSubWebView()
                    callbacks.onSuccess?.invoke(data["order"] as Json)
                }

                CheckoutEvent.purchaseError -> {
                    deunaWebView.closeSubWebView()
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.PAYMENT_ERROR, data = data
                    )
                    callbacks.onError?.invoke(error)
                }

                CheckoutEvent.linkFailed, CheckoutEvent.linkCriticalError -> {
                    val error = PaymentsError.fromJson(
                        type = PaymentsError.Type.INITIALIZATION_FAILED, data = data
                    )
                    callbacks.onError?.invoke(error)
                }

                CheckoutEvent.linkClose -> {
                    onCloseByUser()
                }

                CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
                    // No action required for these events
                }

                else -> {
                    DeunaLogs.debug("CheckoutBridge Unhandled event: $event")
                }
            }

            if (closeEvents.contains(event)) {
                onCloseByEvent()
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: JSONException) {
            DeunaLogs.debug("CheckoutBridge JSONException: $e")
        }
    }
}
