package com.deuna.maven.element.domain

import com.deuna.maven.*
import com.deuna.maven.shared.*
import org.json.*

@Suppress("UNCHECKED_CAST")
class ElementsBridge(
    private val sdkInstanceId: Int,
    private val callbacks: ElementsCallbacks?,
    private val closeEvents: Set<ElementsEvent>,
) : WebViewBridge(name = "android") {
    override fun handleEvent(message: String) {
        try {


            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json

            if (type == null || data == null) {
                return
            }

            val event = ElementsEvent.valueOf(type)
            callbacks?.eventListener?.invoke(event, data)

            when (event) {

                ElementsEvent.vaultSaveSuccess, ElementsEvent.cardSuccessfullyCreated -> {
                    callbacks?.onSuccess?.invoke(data)
                }

                ElementsEvent.vaultFailed, ElementsEvent.cardCreationError, ElementsEvent.vaultSaveError -> {
                    val error = ElementsError.fromJson(
                        type = ElementsError.Type.VAULT_SAVE_ERROR,
                        data = data
                    )
                    if (error != null) {
                        callbacks?.onError?.invoke(error)
                    }
                }

                ElementsEvent.vaultClosed -> {
                    closeElements(sdkInstanceId)
                    callbacks?.onCanceled?.invoke()
                }

                else -> {
                    DeunaLogs.debug("ElementsBridge Unhandled event: $event")
                }
            }

            if (closeEvents.contains(event)) {
                closeElements(sdkInstanceId)
            }
        } catch (e: Exception) {
            DeunaLogs.debug("ElementsBridge JSONException: $e")
        }
    }
}