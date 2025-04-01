package com.deuna.maven.web_views.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.deuna.maven.R
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.file_downloaders.isFileDownloadUrl


open class BaseWebView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private var pageLoaded = false
    var loader: ProgressBar
    var webView: WebView
    var listener: Listener? = null

    init {
        inflate(context, R.layout.embedded_webview, this)
        webView = findViewById<WebView>(R.id.embedded_web_view)
        loader = findViewById<ProgressBar>(R.id.embedded_loader)
    }

    @SuppressLint("SetJavaScriptEnabled")
    open fun loadUrl(url: String, javascriptToInject: String? = null) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        webView.addJavascriptInterface(LocalBridge(), "local")

        /// Client to listen errors and content loaded
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded = true

                val js = """
                     window.open = function(url, target, features) {
                         local.openInNewTab(url);
                     };
                """.trimIndent()
                webView.evaluateJavascript(js, null)

                if (javascriptToInject != null) {
                    webView.evaluateJavascript(javascriptToInject, null)
                }

                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE
                listener?.onWebViewLoaded()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                // ignore errors when the page is already loaded
                if (pageLoaded) {
                    return
                }
                if (error != null) {
                    listener?.onWebViewError()
                }
            }
        }


        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?,
            ): Boolean {
                if (!isDialog) {
                    val newWebView = WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val transport = resultMsg?.obj as WebView.WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()

                    // Custom WebViewClient to handle external URLs and loading URLs in a new WebView
                    // for example when a link is clicked
                    val webViewClient = CustomWebViewClient(object : WebViewCallback {
                        override fun onExternalUrl(webView: WebView, url: String) {
                            if (url.isFileDownloadUrl) {
                                listener?.onDownloadFile(url)
                                return
                            }
                            listener?.onOpenInNewTab(url)
                        }

                        override fun onLoadUrl(webView: WebView, newWebView: WebView, url: String) {
                            if (url.isFileDownloadUrl) {
                                listener?.onDownloadFile(url)
                                return
                            }
                            listener?.onOpenInNewTab(url)
                        }
                    }, newWebView)
                    newWebView.webViewClient = webViewClient
                }
                return true
            }
        }

        DeunaLogs.info("Loading url: $url")
        webView.loadUrl(url)
    }


    /**
     * Intercepts window.open and launches a new activity with a new web view
     */
    inner class LocalBridge() {
        @JavascriptInterface
        fun openInNewTab(url: String) {
            if (url.isFileDownloadUrl) {
                listener?.onDownloadFile(url)
                return
            }
            listener?.onOpenInNewTab(url)
        }
    }


    interface Listener {
        fun onWebViewLoaded()
        fun onWebViewError()
        fun onOpenInNewTab(url: String)
        fun onDownloadFile(url: String)
    }


    /**
     * Removes the WebView from the view hierarchy and destroys it
     */
    open fun destroy() {
        webView.apply {
            // Remove the WebView from the view hierarchy
            (webView.parent as? ViewGroup)?.removeView(webView)

            // Stop loading and clear cache
            webView.stopLoading()
            webView.clearHistory()
            webView.clearCache(true)

            // Destroy the WebView
            webView.destroy()
        }
    }

}