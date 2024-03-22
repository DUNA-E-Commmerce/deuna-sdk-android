package com.deuna.maven.webviews

import android.annotation.*
import android.content.*
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


abstract class BaseWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOSE_EVENTS = "CLOSE_EVENTS"
        const val CLOSE_BROADCAST_RECEIVER_ACTION = "com.deuna.maven.CLOSE_BROADCAST_RECEIVER"
    }

    private val closeAllReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    lateinit var loader: ProgressBar
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }


    private fun initialize() {
        if (!NetworkUtils(this).hasInternet) {
            DeunaLogs.debug("No internet connection")
            onNoInternet()
            return
        }

        // register a broadcast receiver to listen when to close the webView activity
        BroadcastReceiverUtils.register(
            context = this,
            broadcastReceiver = closeAllReceiver,
            action = CLOSE_BROADCAST_RECEIVER_ACTION
        )

        // listen when back button is pressed
        onBackPressedDispatcher.addCallback {
            DeunaLogs.debug("Canceled by user")
            onCanceledByUser()
            finish()
        }
        loader = findViewById(R.id.loader)
        webView = findViewById(R.id.deuna_webview)

    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String) {
        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
            setSupportMultipleWindows(true) // Enable support for multiple windows
        }

        webView.addJavascriptInterface(getBridge(), "android") // Add JavascriptInterface

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // When the page finishes loading, the Web View is shown and the loader is hidden
                view?.visibility = View.VISIBLE
                loader.visibility = View.GONE
            }
        }

        webView.loadUrl(url)
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


    fun cleanUrl(url: String): String {
        val protocolEndIndex = url.indexOf("//") + 2
        val protocol = url.substring(0, protocolEndIndex)
        val restOfUrl = url.substring(protocolEndIndex).replace("//", "/")
        return "$protocol$restOfUrl"
    }


    abstract fun getBridge(): WebViewBridge

    abstract fun onNoInternet()

    abstract fun onCanceledByUser()

    override fun onDestroy() {
        unregisterReceiver(closeAllReceiver)
        super.onDestroy()
    }

}


abstract class WebViewBridge {
    /**
     * The postMessage function is called when a message is received from JavaScript code in a WebView.
     * The message is parsed and the corresponding callbacks are called based on the event type.
     */
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            handleEvent(message)
        } catch (e: Exception) {
            Log.d("ElementsBridge", "postMessage: $e")
        }
    }

    abstract fun handleEvent(message: String)
}