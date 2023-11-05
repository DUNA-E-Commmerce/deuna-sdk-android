package com.deuna.maven.checkout

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.R
import com.deuna.maven.checkout.domain.DeUnaBridge
import com.deuna.maven.client.sendOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity for Deuna.
 */
class DeunaActivity : AppCompatActivity() {

    lateinit var instance: DeunaActivity

    companion object {
        const val ORDER_TOKEN = "order_token"
        const val API_KEY = "api_key"
        const val LOGGING_ENABLED = "logging_enabled"
        var callbacks: Callbacks? = null

        fun setCallback(callback: Callbacks?) {
            this.callbacks = callback
        }
    }

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna)
        instance = this
        setVisibilityProgressBar(true)
        getOrderApi(intent.getStringExtra(ORDER_TOKEN)!!, intent.getStringExtra(API_KEY)!!)
    }

    private fun launchActivity(url: String) {
        Log.d("DeunaActivityUrl", url)
        val webView: WebView = findViewById(R.id.deuna_webview)
        webView.visibility = View.VISIBLE
        setupWebView(webView)
        loadUrlWithNetworkCheck(webView, this, url)
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
        webView.addJavascriptInterface(
            DeUnaBridge(this, callbacks!!),
            "android"
        ) // Add JavascriptInterface
        setupWebChromeClient(webView)
    }

    /**
     * Setup the WebChromeClient to handle creation of new windows.
     */
    private fun setupWebChromeClient(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
        }
    }

    private fun getOrderApi(orderToken: String, apiKey: String) {

        sendOrder(orderToken, apiKey, object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>
                    setVisibilityProgressBar(false)
                    if (orderMap != null) {
                        launchActivity(orderMap.get("payment_link").toString())
                    }
                } else {
                    Toast.makeText(
                        this@DeunaActivity,
                        "Error al obtener la orden",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(
                    this@DeunaActivity,
                    "Error al obtener la orden",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    private fun setVisibilityProgressBar(isVisible: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.progress_circular)
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Create a new WebView with a WebViewClient that handles URL loading.
     */
//    private fun createNewWebView(): WebView {
//        return WebView(this@DeunaActivity).apply {
//            webViewClient = object : WebViewClient() {
//                override fun shouldOverrideUrlLoading(
//                    view: WebView?,
//                    request: WebResourceRequest?
//                ): Boolean {
//                    val newUrl = request?.url.toString()
//                    Log.d("URL", newUrl)
//                    view?.loadUrl(newUrl) // Load the URL in the same WebView
//                    return true // Indicate that we have handled the URL loading
//                }
//            }
//            addJavascriptInterface(DeUnaBridge(null , callbacks!!), "android") // Add JavascriptInterface
//        }
//    }

    /**
     * Load a URL if there is an active internet connection.
     */
    private fun loadUrlWithNetworkCheck(
        view: WebView,
        context: Context,
        url: String
    ) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if ((networkCapabilities != null) && networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        ) {
            Log.d("DeunaActivity", url)
            view.loadUrl(url)
        } else {
            log("No internet connection")
        }
    }

    fun setProgressBarVisibilityBar(visible: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.progress_circular)
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Log a message if logging is enabled.
     */
    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }
}