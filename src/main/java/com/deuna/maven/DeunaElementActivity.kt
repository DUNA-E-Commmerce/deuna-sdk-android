package com.deuna.maven

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.deuna.maven.domain.Callbacks
import com.deuna.maven.domain.DeUnaBridge

class DeunaElementActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val LOGGING_ENABLED = "logging_enabled"
    }

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna_element)

        val url = intent.getStringExtra(DeunaActivity.EXTRA_URL)
        val webView: WebView = findViewById(R.id.deuna_webview_element)
        setupWebView(webView)
        if (url != null) {
            loadUrlWithNetworkCheck(webView, this, url, Callbacks())
        }
    }

    /**
     * Setup the WebView with necessary settings and JavascriptInterface.
     */
    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }
        webView.addJavascriptInterface(DeUnaBridge(), "android") // Add JavascriptInterface
        setupWebChromeClient(webView)
    }

    /**
     * Setup the WebChromeClient to handle creation of new windows.
     */
    private fun setupWebChromeClient(webView: WebView) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
            ): Boolean {
                val newWebView = createNewWebView()
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                val layout = findViewById<ConstraintLayout>(R.id.my_constraint_layout)
                layout.addView(newWebView)

                return true
            }
        }
    }

    /**
     * Create a new WebView with a WebViewClient that handles URL loading.
     */
    private fun createNewWebView(): WebView {
        return WebView(this@DeunaElementActivity).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val newUrl = request?.url.toString()
                    Log.d("URL", newUrl)
                    view?.loadUrl(newUrl) // Load the URL in the same WebView
                    return true // Indicate that we have handled the URL loading
                }
            }
            addJavascriptInterface(DeUnaBridge(), "android") // Add JavascriptInterface
        }
    }

    /**
     * Load a URL if there is an active internet connection.
     */
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
            Log.d("Loading", url)
            view.loadUrl(url)
        } else {
            log("No internet connection")
        }
    }

    /**
     * Log a message if logging is enabled.
     */
    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(DeunaActivity.LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }
}