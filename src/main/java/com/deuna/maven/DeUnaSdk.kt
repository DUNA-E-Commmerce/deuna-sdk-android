package com.deuna.maven

import android.content.Context
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
import com.deuna.maven.domain.Callbacks
import com.deuna.maven.domain.CheckoutEvents
import com.deuna.maven.domain.DeUnaBridge
import com.deuna.maven.domain.ElementType
import com.deuna.maven.domain.Environment
import kotlinx.coroutines.DelicateCoroutinesApi

class DeUnaSdk {
    private lateinit var apiKey: String
    private lateinit var orderToken: String
    private lateinit var environment: Environment
    private lateinit var elementType: ElementType
    private lateinit var userToken: String
    private var baseUrl: String = "https://elements.euna"
    private var actionMillisecods = 5000L
    private var closeOnEvents: Array<CheckoutEvents>? = null
    private var loggingEnabled: Boolean? = false

    companion object {
        private lateinit var instance: DeUnaSdk

        fun config(
            apiKey: String? = null,
            orderToken: String? = null,
            userToken: String? = null,
            environment: Environment,
            elementType: ElementType? = null,
            closeOnEvents: Array<CheckoutEvents>? = null,
            loggingEnabled: Boolean? = false
        ) {
            instance = DeUnaSdk().apply {
                if (apiKey != null) {
                    this.apiKey = apiKey
                }
                if (orderToken != null) {
                    this.orderToken = orderToken
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
                } else {
                    this.baseUrl = "https://pay.deuna.com"
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
            view: View
        ): Callbacks {
            instance.apply {
                val callbacks = Callbacks()
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                val webView: WebView = view.findViewById(R.id.deuna_webview)
                configureWebViewClient(webView, callbacks, closeOnEvents)
                configureWebView(webView)
                addJavascriptInterface(webView, callbacks, closeOnEvents)
                loadUrlWithNetworkCheck(webView, webView.context, "$baseUrl/$orderToken", callbacks)
                return callbacks
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun initElements(
            view: View
        ): Callbacks {
            instance.apply {
                val callbacks = Callbacks()
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                val webView: WebView = view.findViewById(R.id.deuna_webview)
                configureWebViewClient(webView, callbacks, closeOnEvents)
                val builder = Uri.parse("$baseUrl/elements/${elementType.value}").buildUpon()
                if (userToken.isNotEmpty()) {
                    builder.appendQueryParameter("userToken", userToken)
                }
                val url = builder.build().toString()
                configureWebViewClient(webView, callbacks, closeOnEvents)
                configureWebView(webView)
                addJavascriptInterface(webView, callbacks, closeOnEvents)
                loadUrlWithNetworkCheck(webView, webView.context, url, callbacks)

                return callbacks
            }
        }

        private fun configureWebView(webView: WebView) {
            webView.settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
        }


        private fun configureWebViewClient(
            webView: WebView,
            callbacks: Callbacks,
            closeOnEvents: Array<CheckoutEvents>?
        ) {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    log("Error: ${error?.description}")
                }
            }
        }

        private fun addJavascriptInterface(
            webView: WebView,
            callbacks: Callbacks,
            closeOnEvents: Array<CheckoutEvents>?
        ) {
            webView.addJavascriptInterface(
                DeUnaBridge(callbacks, webView, closeOnEvents),
                "android"
            )
        }

        private fun loadUrlWithNetworkCheck(
            view: WebView,
            context: Context,
            url: String,
            callbacks: Callbacks
        ) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if ((networkCapabilities != null) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
            ) {
                view.loadUrl(url)
            } else {
                this.log("No internet connection")
            }
        }

        private fun log(message: String) {
            if(instance.loggingEnabled!! && instance.loggingEnabled == true) {
                Log.d("[DeunaSDK]: ", message)
            }
        }
    }
}