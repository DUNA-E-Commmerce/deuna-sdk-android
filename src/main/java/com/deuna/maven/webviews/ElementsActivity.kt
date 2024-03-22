package com.deuna.maven.webviews

import ElementsResponse
import android.os.*
import com.deuna.maven.*
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import org.json.*

class ElementsActivity() : BaseWebViewActivity() {

    companion object {
        const val EXTRA_URL = "EXTRA_URL"
        private var callbacks: ElementsCallbacks? = null

        public fun setCallbacks(callbacks: ElementsCallbacks) {
            this.callbacks = callbacks
        }
    }

    private lateinit var closeEvents: Set<ElementsEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL)!!

        val closeEventAsStrings = intent.getStringArrayListExtra(EXTRA_CLOSE_EVENTS) ?: emptyList<String>()
        closeEvents = parseCloseEvents<ElementsEvent>(closeEventAsStrings)

        loadUrl(url)

    }


    override fun getBridge(): WebViewBridge {
        return ElementsBridge()
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(
            ElementsError(
                ElementsErrorType.NO_INTERNET_CONNECTION, null
            )
        )
    }

    override fun onCanceledByUser() {
        callbacks?.onCanceled?.invoke()
    }

    override fun onDestroy() {
        callbacks?.onClosed?.invoke()
        super.onDestroy()
    }

    inner class ElementsBridge() : WebViewBridge() {
        override fun handleEvent(message: String) {
            try {
                val json = JSONObject(message)
                val eventData = ElementsResponse.fromJson(json)

                callbacks?.eventListener?.invoke(eventData.type, eventData)
                when (eventData.type) {

                    ElementsEvent.vaultSaveSuccess, ElementsEvent.cardSuccessfullyCreated -> {
                        callbacks?.onSuccess?.invoke((eventData))
                    }

                    ElementsEvent.vaultFailed, ElementsEvent.cardCreationError, ElementsEvent.vaultSaveError -> eventData.data.metadata?.let {
                        handleError(
                            eventData
                        )
                    }

                    ElementsEvent.vaultClosed -> {
                        closeElements(this@ElementsActivity)
                        callbacks?.onCanceled?.invoke()
                    }

                    else -> {
                        DeunaLogs.debug("ElementsBridge Unhandled event: ${eventData.type}")
                    }
                }
                eventData.let {
                    if (closeEvents.contains(it.type)) {
                        closeElements(this@ElementsActivity)
                    }
                }
            } catch (e: Exception) {
                DeunaLogs.debug("ElementsBridge JSONException: $e")
            }
        }

        private fun handleError(response: ElementsResponse) {
            callbacks?.onError?.invoke(
                ElementsError(
                    ElementsErrorType.VAULT_SAVE_ERROR,
                    response.data.user
                )
            )
        }

    }
}