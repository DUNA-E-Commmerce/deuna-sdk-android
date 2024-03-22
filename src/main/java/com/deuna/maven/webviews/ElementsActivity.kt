package com.deuna.maven.webviews

import android.os.*
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*

class ElementsActivity() : BaseWebViewActivity() {

    companion object {
        const val EXTRA_URL = "EXTRA_URL"
        private var callbacks: ElementsCallbacks? = null

        fun setCallbacks(callbacks: ElementsCallbacks) {
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
        return ElementsBridge(
            context = this,
            callbacks = callbacks,
            closeEvents = closeEvents
        )
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

}