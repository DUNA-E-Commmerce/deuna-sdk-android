package com.deuna.maven.webviews

import CheckoutResponse
import android.os.*
import com.deuna.maven.*
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.client.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.CheckoutCallbacks
import org.json.*
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
                        loadUrl(parsedUrl.toString())
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
        return CheckoutBridge()
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

    inner class CheckoutBridge() : WebViewBridge() {
        override fun handleEvent(message: String) {

            try {
                val json = JSONObject(message)
                val eventData = CheckoutResponse.fromJson(json)
                callbacks?.eventListener?.invoke(eventData.type, eventData)
                when (eventData.type) {
                    CheckoutEvent.purchase, CheckoutEvent.apmSuccess -> {
                        callbacks?.onSuccess?.invoke(eventData)
                    }

                    CheckoutEvent.purchaseRejected -> {
                        handleError(
                            CheckoutErrorType.PAYMENT_ERROR,
                            eventData
                        )
                    }

                    CheckoutEvent.linkFailed, CheckoutEvent.purchaseError -> {
                        handleError(CheckoutErrorType.CHECKOUT_INITIALIZATION_FAILED, eventData)
                    }

                    CheckoutEvent.linkClose -> {
                        closeCheckout(this@CheckoutActivity)
                        callbacks?.onCanceled?.invoke()
                    }

                    CheckoutEvent.paymentMethods3dsInitiated, CheckoutEvent.apmClickRedirect -> {
                        // No action required for these events
                    }

                    else -> {
                        DeunaLogs.debug("CheckoutBridge Unhandled event: $eventData")
                    }
                }

                eventData.let {
                    if (closeEvents.contains(it.type)) {
                        closeCheckout(this@CheckoutActivity)
                    }
                }
            } catch (e: JSONException) {
                DeunaLogs.debug("CheckoutBridge JSONException: $e")
            }
        }

        private fun handleError(type: CheckoutErrorType, response: CheckoutResponse) {
            callbacks?.onError?.invoke(
                CheckoutError(
                    type,
                    response.data.order,
                    response.data.user
                )
            )
        }
    }
}

