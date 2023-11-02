package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity
import com.deuna.maven.domain.Callbacks
import com.deuna.maven.domain.CheckoutEvents
import com.deuna.maven.domain.DeUnaBridge
import com.deuna.maven.domain.DeUnaElementBridge
import com.deuna.maven.domain.ElementType
import com.deuna.maven.domain.Environment
import kotlinx.coroutines.DelicateCoroutinesApi

open class DeUnaSdk {
    private lateinit var apiKey: String
    private lateinit var orderToken: String
    private lateinit var environment: Environment
    private lateinit var elementType: ElementType
    private lateinit var userToken: String
    private var baseUrl: String = ""
    private var elementUrl: String = "https://elements.deuna.io"
    private var actionMillisecods = 5000L
    private var closeOnEvents: Array<CheckoutEvents>? = null
    private var loggingEnabled: Boolean? = false
    private var context: Context? = null

    companion object {
        private lateinit var instance: DeUnaSdk

        /**
         * Configure the DeUna SDK with the given parameters.
         * @param apiKey The API key to use for the DeUna SDK.
         * @param orderToken The order token to use for the DeUna SDK.
         * @param environment The environment to use for the DeUna SDK.
         * @param elementType The element type to use for the DeUna SDK.
         * @param closeOnEvents The events to close the DeUna SDK on.
         * @param loggingEnabled Whether to enable logging for the DeUna SDK.
         * @param context The context to use for the DeUna SDK.
         * @throws IllegalStateException if the SDK has already been configured.
         */
        fun config(
            apiKey: String? = null,
            orderToken: String? = null,
            userToken: String? = null,
            environment: Environment,
            elementType: ElementType? = null,
            closeOnEvents: Array<CheckoutEvents>? = null,
            loggingEnabled: Boolean? = false,
            context: Context
        ) {
            instance = DeUnaSdk().apply {
                if (apiKey != null) {
                    this.apiKey = apiKey
                }

                if (orderToken != null) {
                    this.orderToken = orderToken
                }

                this.context = context

                if (closeOnEvents != null) {
                    this.closeOnEvents = closeOnEvents
                }

                this.environment = environment
                this.baseUrl = when (environment) {
                    Environment.DEVELOPMENT -> "https://pay.stg.deuna.com"
                    Environment.PRODUCTION -> "https://pay.deuna.com"
                }
                this.elementUrl = when (environment) {
                    Environment.DEVELOPMENT -> "https://elements.stg.deuna.io"
                    Environment.PRODUCTION -> "https://elements.deuna.io"
                }

                if(loggingEnabled != null) {
                    this.loggingEnabled = loggingEnabled
                }

                if (userToken != null || apiKey != null || elementType != null) {
                    var url = this.elementUrl
                    if (elementType != null) {
                        url += "/${elementType}"
                    }
                    val builder = Uri.parse(url).buildUpon()
                    if (userToken != null) {
                        builder.appendQueryParameter("userToken", userToken)
                    }
                    if (apiKey != null) {
                        builder.appendQueryParameter("apiKey", apiKey)
                    }
                    this.elementUrl = builder.build().toString()
                }
            }
        }

        /**
         * Initialize the DeUna SDK Checkout with the configured parameters.
         */
        fun initCheckout(
        ) {
            Intent(instance.context!!, DeunaActivity::class.java).apply {
                putExtra(DeunaActivity.EXTRA_URL, "${instance.baseUrl}/${instance.orderToken}")
                putExtra(DeunaActivity.LOGGING_ENABLED, instance.loggingEnabled)
                startActivity(instance.context!!, this, null)
            }
        }

        /**
         * Initialize the DeUna SDK Elements with the configured parameters.
         * @throws IllegalStateException if the SDK has not been configured.
         */
        fun initElements(
        ) {
            if (instance::elementType.isInitialized.not() || instance::userToken.isInitialized.not() || instance::apiKey.isInitialized.not()) {
                throw IllegalStateException("elementType, userToken and apiKey must be configured before calling initElements")
            }
            Intent(instance.context!!, DeunaElementActivity::class.java).apply {
                putExtra(DeunaElementActivity.EXTRA_URL, "${instance.elementUrl}")
                putExtra(DeunaElementActivity.LOGGING_ENABLED, instance.loggingEnabled)
                startActivity(instance.context!!, this, null)
            }
        }


    }
}