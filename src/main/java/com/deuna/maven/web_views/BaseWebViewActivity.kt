package com.deuna.maven.web_views

import android.annotation.*
import android.content.*
import android.graphics.*
import android.net.*
import android.os.*
import android.util.*
import android.view.*
import android.webkit.*
import android.widget.*
import androidx.activity.*
import androidx.appcompat.app.AppCompatActivity
import com.deuna.maven.*
import com.deuna.maven.R
import com.deuna.maven.element.*
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.utils.*


/**
 * This abstract class provides a foundation for activities that display web content
 * using a WebView. It handles common tasks like checking internet connectivity,
 * registering broadcast receivers, handling back button presses, and loading URLs.
 */
abstract class BaseWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOSE_EVENTS = "CLOSE_EVENTS"
        const val CLOSE_BROADCAST_RECEIVER_ACTION = "com.deuna.maven.CLOSE_BROADCAST_RECEIVER"
    }

    private val closeAllReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish() // Close the activity when the broadcast is received
        }
    }

    lateinit var loader: ProgressBar
    private lateinit var webView: WebView

    var previousUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        initialize()
    }

    // Check internet connection and initialize other components
    private fun initialize() {
        if (!NetworkUtils(this).hasInternet) {
            DeunaLogs.debug("No internet connection")
            onNoInternet()
            return
        }

        // Register broadcast receiver to listen for close event
        BroadcastReceiverUtils.register(
            context = this,
            broadcastReceiver = closeAllReceiver,
            action = CLOSE_BROADCAST_RECEIVER_ACTION
        )

        // Handle back button press
        onBackPressedDispatcher.addCallback {
            DeunaLogs.debug("Canceled by user")
            onCanceledByUser()
            finish()
        }
        loader = findViewById(R.id.deuna_loader)
        webView = findViewById(R.id.deuna_webview)

    }

    // Load the URL in the WebView
    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        // Add JavascriptInterface
        if ("deuna.io" in url || "deuna.com" in url) {
            webView.addJavascriptInterface(getBridge(), "android")
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                // Check if this is the first load or a navigation
                if (previousUrl == null) {
                    // This is the first load
                    previousUrl = url
                    return
                }
                if (url == null) {
                    return
                }
                DeunaLogs.info("Page refreshed or Navigating to a different URL")
                // Page refreshed or Navigating to a different URL
                rebuildWebView(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE
            }
        }
        webView.loadUrl(url)
    }

    fun rebuildWebView(url: String) {
        previousUrl = null
        val layout = findViewById<RelativeLayout>(R.id.deuna_webview_container)
        layout.removeView(webView)

        webView = WebView(this@BaseWebViewActivity)
        layout.addView(webView)
        webView.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        webView.visibility = View.VISIBLE

        loadUrl(url)
    }


    inline fun <reified T : Enum<T>> parseCloseEvents(closeEventAsListString: List<String>): Set<T> {
        // Use `T` as the generic type for the enum
        return closeEventAsListString.mapNotNull { stringValue ->
            try {
                // Use `enumValueOf<T>` to get the enum value of type `T`
                enumValueOf<T>(stringValue)
            } catch (e: IllegalArgumentException) {
                null // Ignore invalid enum constant names
            }
        }.toSet()
    }


    // Remove unnecessary slashes from the URL
    fun cleanUrl(url: String): String {
        val protocolEndIndex = url.indexOf("//") + 2
        val protocol = url.substring(0, protocolEndIndex)
        val restOfUrl = url.substring(protocolEndIndex).replace("//", "/")
        return "$protocol$restOfUrl"
    }

    // Abstract methods to be implemented by subclasses
    abstract fun getBridge(): WebViewBridge

    abstract fun onNoInternet()

    abstract fun onCanceledByUser()

    // Unregister the broadcast receiver when the activity is destroyed
    override fun onDestroy() {
        unregisterReceiver(closeAllReceiver)
        super.onDestroy()
    }


}