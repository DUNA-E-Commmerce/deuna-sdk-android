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

                if (context != null) {
                    this.context = context
                }

                if (closeOnEvents != null) {
                    this.closeOnEvents = closeOnEvents
                }

                if (userToken != null) {
                    this.userToken = userToken
                }
                this.environment = environment
                if (this.environment == Environment.DEVELOPMENT) {
                    this.baseUrl = "https://pay.stg.deuna.com"
                    this.elementUrl = "https://elements.stg.deuna.io"
                } else {
                    this.baseUrl = "https://pay.deuna.com"
                    this.elementUrl = "https://elements.deuna.io"
                }
                if (elementType != null) {
                    this.elementType = elementType
                }

                if(loggingEnabled != null) {
                    this.loggingEnabled = loggingEnabled
                }
            }
        }


        @OptIn(DelicateCoroutinesApi::class)
        fun initCheckout(
        ) {
            Intent(instance.context!!, DeunaActivity::class.java).apply {
                putExtra(DeunaActivity.EXTRA_URL, "${instance.baseUrl}/${instance.orderToken}")
                putExtra(DeunaActivity.LOGGING_ENABLED, instance.loggingEnabled)
                startActivity(instance.context!!, this, null)
            }
//            instance.apply {
//                val callbacks = Callbacks()
//                val cookieManager = CookieManager.getInstance()
//                cookieManager.setAcceptCookie(true)
//                val webView: WebView = view.findViewById(R.id.deuna_webview)
//                configureWebViewClient(webView, callbacks, closeOnEvents)
//                configureWebView(webView)
//                addJavascriptInterface(webView, callbacks, closeOnEvents)
//                loadUrlWithNetworkCheck(webView, webView.context, "$baseUrl/$orderToken", callbacks)
//                return callbacks
//            }
        }

//        @OptIn(DelicateCoroutinesApi::class)
//        fun initElements(
//            view: View
//        ): Callbacks {
//            instance.apply {
//                val callbacks = Callbacks()
//                val cookieManager = CookieManager.getInstance()
//                cookieManager.setAcceptCookie(true)
//                val webView: WebView = view.findViewById(R.id.deuna_webview)
//                configureWebViewClient(webView, callbacks, closeOnEvents)
//                val builder = Uri.parse("$elementUrl/${elementType.value}").buildUpon()
//                if (userToken.isNotEmpty()) {
//                    builder.appendQueryParameter("userToken", userToken)
//                }
//                if (apiKey.isNotEmpty()) {
//                    builder.appendQueryParameter("publicApiKey", apiKey)
//                }
//                val url = builder.build().toString()
//                configureWebViewClient(webView, callbacks, closeOnEvents)
//                configureWebView(webView)
//                addJavascriptElementInterface(webView, callbacks, closeOnEvents)
//                loadUrlWithNetworkCheck(webView, webView.context, url, callbacks)
//
//                return callbacks
//            }
//        }


    }
}