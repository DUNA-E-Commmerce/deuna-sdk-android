package com.deuna.maven.element

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.R
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.closeElements
import com.deuna.maven.element.domain.ElementsBridge
import com.deuna.maven.element.domain.ElementsCallbacks
import com.deuna.maven.element.domain.ElementsErrorMessage
import com.deuna.maven.shared.NetworkUtils
import com.deuna.maven.utils.BroadcastReceiverUtils
import com.deuna.maven.utils.DeunaBroadcastReceiverAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeunaElementActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val EXTRA_URL = "extra_url"
        const val LOGGING_ENABLED = "logging_enabled"
        const val CLOSE_ON_EVENTS = ""
        var callbacks: ElementsCallbacks? = null
        fun setCallback(callback: ElementsCallbacks?) {
            this.callbacks = callback
        }
    }

    private val closeAllReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    // Called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna_element)
        showProgressBar(true)

        val url = intent.getStringExtra(EXTRA_URL)
        val webView: WebView = findViewById(R.id.deuna_webview_element)

        scope.launch {
            setupWebView(webView, intent.getStringArrayListExtra(DeunaActivity.CLOSE_ON_EVENTS))
            if (url != null) {
                loadUrlWithNetworkCheck(webView, this@DeunaElementActivity, url)
                BroadcastReceiverUtils.register(
                    context = this@DeunaElementActivity,
                    broadcastReceiver = closeAllReceiver,
                    action = DeunaBroadcastReceiverAction.ELEMENTS
                )
            }
        }
    }

    // Called when the activity is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeAllReceiver)
    }

    // Setup the WebView with necessary settings and JavascriptInterface.
    private fun setupWebView(webView: WebView, closeOnEvents: ArrayList<String>? = null) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }
        webView.addJavascriptInterface(
            ElementsBridge(
                callbacks = callbacks!!,
                closeOnEvents = closeOnEvents,
                closeElements = {
                    closeElements(this)
                }
            ),
            "android"
        ) // Add JavascriptInterface

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                showProgressBar(false)
            }
        }

        setupWebChromeClient(webView)
    }

    // Setup the WebChromeClient to handle creation of new windows.
    private fun setupWebChromeClient(webView: WebView) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@DeunaElementActivity).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                }

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val newUrl = request?.url.toString()
                        view?.loadUrl(newUrl)
                        return true
                    }
                }

                newWebView.webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: Message
                    ): Boolean {
                        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }
                }

                webView.visibility = View.GONE

                val layout =
                    findViewById<RelativeLayout>(R.id.deuna_layout_element) // Reemplaza 'your_layout_id' con el ID de tu RelativeLayout
                layout.addView(newWebView)

                newWebView.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                newWebView.visibility = View.VISIBLE

                return true
            }
        }
    }

    // Load a URL if there is an active internet connection.
    private fun loadUrlWithNetworkCheck(view: WebView, context: Context, url: String) {
        if (NetworkUtils(context).hasInternet) {
            return view.loadUrl(url)
        }
        log("No internet connection")
        callbacks?.onError?.invoke(NetworkUtils.ELEMENTS_NO_INTERNET_ERROR)
    }

    // Log a message if logging is enabled.
    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(DeunaActivity.LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }

    // Show or Hide progress bar (loader)
    private fun showProgressBar(show: Boolean) {
        val loader: ProgressBar = findViewById(R.id.loader)
        val layout: RelativeLayout = findViewById(R.id.progressLayout)

        loader.visibility = if (show) View.VISIBLE else View.INVISIBLE
        layout.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }
}