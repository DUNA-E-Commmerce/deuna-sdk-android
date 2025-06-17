package com.deuna.sdkexample.web_view_manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewController(
    private val context: Context,
    val webView: WebView,
) {

    interface Listener {
        fun onWebViewLoaded()
        fun onWebViewError()
        fun onOpenExternalUrl(url: String)
    }

   var listener: Listener? = null


    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.toString()?.let { url ->
                    // Open url in Custom Chrome Tab
                    listener?.onOpenExternalUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("WebViewController", "onPageFinished")
                listener?.onWebViewLoaded()
                val js = """
                     window.open = function(url, target, features) {
                         local.openExternalUrl(url);
                     };
                """.trimIndent()
                webView.evaluateJavascript(js, null)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                listener?.onWebViewError()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                val newWebView = WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                           listener?.onOpenExternalUrl(request.url.toString())
                            return true
                        }
                    }
                }

                transport?.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.loadUrl(url)
    }
}