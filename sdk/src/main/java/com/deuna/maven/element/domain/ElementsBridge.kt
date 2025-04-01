package com.deuna.maven.element.domain

import android.webkit.JavascriptInterface
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.deuna.DeunaWebView
import org.json.*

@Suppress("UNCHECKED_CAST")
class ElementsBridge(
    val deunaWebView: DeunaWebView,
    val callbacks: ElementsCallbacks,
    private val closeEvents: Set<ElementsEvent>,
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

            val event = ElementsEvent.valueOf(type)
            callbacks.onEventDispatch?.invoke(event, data)

            when (event) {

                ElementsEvent.vaultSaveSuccess -> {
                    deunaWebView.closeSubWebView()
                    callbacks.onSuccess?.invoke(data)
                }

                ElementsEvent.vaultSaveError -> {
                    deunaWebView.closeSubWebView()
                    val error = ElementsError.fromJson(
                        type = ElementsError.Type.VAULT_SAVE_ERROR,
                        data = data
                    )
                    if (error != null) {
                        callbacks.onError?.invoke(error)
                    }
                }

                ElementsEvent.vaultClosed -> {
                    onCloseByUser()
                }

                else -> {}
            }

            if (closeEvents.contains(event)) {
                onCloseByEvent()
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: Exception) {
            DeunaLogs.debug("ElementsBridge JSONException: $e")
        }
    }
}