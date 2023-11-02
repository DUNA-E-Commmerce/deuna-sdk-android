package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.deuna.maven.domain.Callbacks
import com.deuna.maven.domain.DeUnaBridge

class DeunaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val LOGGING_ENABLED = "logging_enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deuna)

        val url = intent.getStringExtra(EXTRA_URL)
        val webView: WebView = findViewById(R.id.deuna_webview)
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Habilita el soporte de múltiples ventanas
        }
        webView.apply {
            this.addJavascriptInterface(DeUnaBridge(), "android")
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@DeunaActivity).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val newUrl = request?.url.toString()
                            Log.d("URL", newUrl)
                            view?.loadUrl(newUrl)
                            // Carga la URL en el mismo WebView
                            return true // Indica que hemos manejado la carga de la URL
                        }
                    }
                }
                val bridge = (webView?.parent as ConstraintLayout).getChildAt(0) as WebView
                val originalBridge = bridge.tag as DeUnaBridge
                webView.addJavascriptInterface(originalBridge, "android")
                newWebView.addJavascriptInterface(originalBridge, "android")
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                val layout = findViewById<ConstraintLayout>(R.id.my_constraint_layout)
                layout.addView(newWebView)

                return true
            }

        }

        if (url != null) {
            loadUrlWithNetworkCheck(webView, this, url, Callbacks())
        }
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
            Log.d("Cargando", url.toString())
            view.loadUrl(url)
        } else {
            this.log("No internet connection")
        }
    }

    private fun log(message: String) {
        val loggingEnabled = intent.getBooleanExtra(LOGGING_ENABLED, false)
        if (loggingEnabled) {
            Log.d("[DeunaSDK]: ", message)
        }
    }


}