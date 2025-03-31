package com.deuna.maven.web_views.new_tab

import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.file_downloaders.downloadFile


@Suppress("UNCHECKED_CAST")
class NewTabWebView(
    private val url: String,
    context: Context,
) : BaseWebView(context) {


    init {
        webView.addJavascriptInterface(LocalBridge(), "windowClose")
        loadUrl(
            url, javascriptToInject = """
            window.close = function() {
               windowClose.onCloseWindowCalled();
            };
            """.trimIndent()
        )
        listener = object : BaseWebView.Listener {
            override fun onWebViewLoaded() {
                webView.evaluateJavascript("""
                    (function() {
                        setTimeout(function() {
                            var button = document.getElementById("cash_efecty_button_print");
                            if (button) {
                                button.style.display = "none";
                            }
                    }, 500); // time out 500 ms
                    })();
                """, null
                )
            }

            override fun onWebViewError() {}

            override fun onOpenInNewTab(url: String) {
                loadUrl(url)
            }

            override fun onDownloadFile(url: String) {
                downloadFile(url)
            }

        }
    }


    inner class LocalBridge {
        @JavascriptInterface
        fun onCloseWindowCalled() {
            DeunaLogs.info("window.close()")
//            finish()
        }
    }
}