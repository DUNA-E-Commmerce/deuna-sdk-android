package com.deuna.maven.web_views

import android.content.*
import android.os.*
import android.webkit.*
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.base.*
import com.deuna.maven.web_views.file_downloaders.*


class NewTabWebViewActivity : BaseWebViewActivity() {
    companion object {
        const val EXTRA_URL = "SUB_WEB_VIEW_URL"
        const val EXTRA_SDK_INSTANCE_ID = "SDK_INSTANCE_ID"
        const val SUB_WEB_VIEW_REQUEST_CODE = 20000

        var activities = mutableMapOf<Int, NewTabWebViewActivity>()

        fun closeWebView(sdkInstanceId: Int) {
            activities[sdkInstanceId]?.finish()
        }
    }

    var sdkInstanceId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkInstanceId = intent.getIntExtra(EXTRA_SDK_INSTANCE_ID, 0)
        activities[sdkInstanceId!!] = this

        webView.addJavascriptInterface(LocalBridge(), "local")
        loadUrl(
            intent.getStringExtra(EXTRA_URL)!!, javascriptToInject = """
            window.close = function() {
               local.closeWindow();
            };
            """.trimIndent()
        )
    }

    override fun onWebViewLoaded() {}

    override fun onWebViewError() {}

    override fun onOpenInNewTab(url: String) {
        webView.loadUrl(url)
    }

    override fun onDownloadFile(url: String) {
        downloadFile(url)
    }

    inner class LocalBridge {
        @JavascriptInterface
        fun closeWindow() {
            DeunaLogs.info("window.close()")
            finish()
        }
    }


    override fun onDestroy() {
        webView.destroy()
        activities.remove(sdkInstanceId)
        super.onDestroy()
    }
}

