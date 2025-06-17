package com.deuna.sdkexample.web_view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import com.deuna.sdkexample.R
import com.deuna.sdkexample.web_view_manager.WebViewController

open class WebViewWrapper(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {


    val webView: WebView
        get() = controller.webView

    var loader: ProgressBar

    private var controller: WebViewController

    init {
        inflate(context, R.layout.webview_layout, this)
        val webView = findViewById<WebView>(R.id.web_view)
        controller = WebViewController(context, webView)
        loader = findViewById(R.id.loader)
        controller.listener = object : WebViewController.Listener {
            override fun onWebViewLoaded() {
                loader.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun onWebViewError() {
                loader.visibility = View.GONE
                // show an Error here
            }

            override fun onOpenExternalUrl(url: String) {
                openInCustomChromeTab(url)
            }
        }
    }


    fun loadUrl(url: String) {
        controller.loadUrl(url)
    }

    private fun openInCustomChromeTab(url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    fun dispose() {
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.destroy()
    }

}