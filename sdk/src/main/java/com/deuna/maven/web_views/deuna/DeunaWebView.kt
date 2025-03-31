package com.deuna.maven.web_views.deuna

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import com.deuna.maven.findFragmentActivity
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.NetworkUtils
import com.deuna.maven.shared.WebViewBridge
import com.deuna.maven.shared.toMap
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.dialog_fragments.NewTabDialogFragment
import com.deuna.maven.web_views.file_downloaders.TakeSnapshotBridge
import com.deuna.maven.web_views.file_downloaders.downloadFile
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
class DeunaWebView(context: Context, attrs: AttributeSet? = null) : BaseWebView(context, attrs) {

    private var newTabDialogFragment: NewTabDialogFragment? = null

    /// When this var is false the close feature is disabled
    private var closeEnabled = true

    val takeSnapshotBridge = TakeSnapshotBridge("paymentWidgetTakeSnapshotBridge")
    val remoteJsFunctionsBridgeName = "onRemoteJsFunctionCalled"
    private val remoteFunctionsRequests = mutableMapOf<Int, (Json) -> Unit>()
    private var remoteFunctionsRequestId = 0
    var bridge: WebViewBridge? = null

    init {
        webView.addJavascriptInterface(RemoteFunctionBridge(), remoteJsFunctionsBridgeName)
        webView.addJavascriptInterface(takeSnapshotBridge, takeSnapshotBridge.name)
        initialize()
    }

    // Load the URL in the WebView
    @SuppressLint("SetJavaScriptEnabled")
    override fun loadUrl(url: String, javascriptToInject: String?) {
        // Add JavascriptInterface

        bridge?.let {
            DeunaLogs.info("Adding bridge ${it.name}")
            webView.addJavascriptInterface(it, it.name)
        }

        super.loadUrl(
            url, """
        console.log = function(message) {
            android.consoleLog(message);
        };
         
         window.open = function(url, target, features) {
            local.openInNewTab(url);
         };
         
         window.xprops = {
             onEventDispatch : function (event) {
                 android.postMessage(JSON.stringify(event));
             },
             onCustomCssSubscribe: function (setCustomCSS)  {
                 window.setCustomCss = setCustomCSS;
             },
             onCustomStyleSubscribe: function (setCustomStyle)  {
                 window.setCustomStyle = setCustomStyle;
             },
             onRefetchOrderSubscribe: function (refetchOrder) {
                 window.deunaRefetchOrder = refetchOrder;
             },
         };
            ${javascriptToInject ?: ""}
        """.trimIndent()
        )

        listener = object : Listener {
            override fun onWebViewLoaded() {}

            override fun onWebViewError() {}

            override fun onOpenInNewTab(url: String) {
                if (newTabDialogFragment != null) {
                    return
                }

                val fragmentActivity = context.findFragmentActivity() ?: return

                newTabDialogFragment = NewTabDialogFragment(
                    url = url,
                    onDialogDestroyed = {
                        newTabDialogFragment = null
                    }
                )
                newTabDialogFragment?.show(
                    fragmentActivity.supportFragmentManager,
                    "NewTabDialogFragment+${System.currentTimeMillis()}"
                )
            }

            override fun onDownloadFile(url: String) {
                downloadFile(url)
            }

        }
    }

    // Check internet connection and initialize other components
    private fun initialize() {
        if (!NetworkUtils(context).hasInternet) {
            onNoInternet()
            return
        }
    }


    /**
     * Configures whether the widget close action is enabled or disabled,
     * Useful for automatic redirects like 3Ds
     */
    fun updateCloseEnabled(enabled: Boolean) {
        closeEnabled = enabled
    }

    /// Closes the sub web view
    fun closeSubWebView() {
        newTabDialogFragment?.dismiss()
        newTabDialogFragment = null
    }

    override fun destroy() {
        closeSubWebView()
        super.destroy()
    }


    /**
     * Build and execute a remote JS function
     */
    fun executeRemoteFunction(
        jsBuilder: (requestId: Int) -> String,
        callback: (Json) -> Unit
    ) {
        remoteFunctionsRequestId++
        remoteFunctionsRequests[remoteFunctionsRequestId] = callback
        webView.evaluateJavascript(jsBuilder(remoteFunctionsRequestId), null)
    }


    /**
     * Js Bridge to listen the remote functions responses
     */
    inner class RemoteFunctionBridge {
        @JavascriptInterface
        fun onRemoteJsFunctionCalled(message: String) {
            try {
                val json = JSONObject(message).toMap()
                val requestId = json["requestId"] as? Int
                if (!remoteFunctionsRequests.contains(requestId)) {
                    return
                }

                remoteFunctionsRequests[requestId]?.invoke(
                    json["data"] as Json
                )
                remoteFunctionsRequests.remove(requestId)
            } catch (e: Exception) {
                Log.d("WebViewBridge", "postMessage: $e")
            }
        }
    }


    fun onNoInternet() {}

    fun onCanceledByUser() {}

}