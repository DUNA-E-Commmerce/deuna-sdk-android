package com.deuna.maven.webviews

import android.os.*
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.client.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.CheckoutCallbacks
import retrofit2.*
import java.net.*

class CheckoutActivity() : BaseWebViewActivity() {

    companion object {
        const val EXTRA_API_KEY = "API_KEY"
        const val EXTRA_ORDER_TOKEN = "ORDER_TOKEN"
        const val EXTRA_BASE_URL = "BASE_URL"
        private var callbacks: CheckoutCallbacks? = null

        fun setCallbacks(callbacks: CheckoutCallbacks) {
            this.callbacks = callbacks
        }
    }

    private lateinit var closeEvents: Set<CheckoutEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseUrl = intent.getStringExtra(EXTRA_BASE_URL)!!
        val orderToken = intent.getStringExtra(EXTRA_ORDER_TOKEN)!!
        val apiKey = intent.getStringExtra(EXTRA_API_KEY)!!

        val closeEventAsStrings = intent.getStringArrayListExtra(EXTRA_CLOSE_EVENTS) ?: emptyList<String>()
        closeEvents = parseCloseEvents<CheckoutEvent>(closeEventAsStrings)

        getOrderApi(baseUrl = baseUrl, orderToken = orderToken, apiKey = apiKey)
    }


    private fun getOrderApi(baseUrl: String, orderToken: String, apiKey: String) {
        sendOrder(baseUrl, orderToken, apiKey, object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>
                    if (orderMap != null) {
                        val parsedUrl = URL(orderMap.get("payment_link").toString())
                        loadUrl(
                            url = cleanUrl(parsedUrl.toString())
                        )
                    }
                } else {
                    orderNotFound()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                orderNotFound()
            }
        })
    }

    private fun orderNotFound() {
        callbacks?.onError?.invoke(
            CheckoutError(
                type = CheckoutErrorType.ORDER_NOT_FOUND,
                order = null,
                user = null
            )
        )
    }

    override fun getBridge(): WebViewBridge {
        return CheckoutBridge(
            context = this,
            callbacks = callbacks,
            closeEvents = closeEvents,
        )
    }

    override fun onNoInternet() {
        callbacks?.onError?.invoke(
            CheckoutError(
                CheckoutErrorType.NO_INTERNET_CONNECTION, null, null
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