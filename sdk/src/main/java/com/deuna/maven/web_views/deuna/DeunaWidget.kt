package com.deuna.maven.web_views.deuna

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.deuna.maven.widgets.checkout_widget.CheckoutBridge
import com.deuna.maven.widgets.elements_widget.ElementsBridge
import com.deuna.maven.widgets.payment_widget.PaymentWidgetBridge
import com.deuna.maven.shared.DeunaBridge
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.ElementsErrors
import com.deuna.maven.shared.NetworkUtils
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.enums.CloseAction
import com.deuna.maven.shared.extensions.findFragmentActivity
import com.deuna.maven.web_views.base.BaseWebView
import com.deuna.maven.web_views.dialog_fragments.NewTabDialogFragment
import com.deuna.maven.web_views.file_downloaders.TakeSnapshotBridge
import com.deuna.maven.web_views.file_downloaders.downloadFile
import com.deuna.maven.web_views.file_downloaders.runOnUiThread

@Suppress("UNCHECKED_CAST")
class DeunaWidget(context: Context, attrs: AttributeSet? = null) : BaseWebView(context, attrs) {

    private var newTabDialogFragment: NewTabDialogFragment? = null

    /// When this var is false the close feature is disabled
    var closeEnabled = true

    val takeSnapshotBridge = TakeSnapshotBridge("paymentWidgetTakeSnapshotBridge")
    var bridge: DeunaBridge? = null

    // Hide the pay button
    var hidePayButton = false

    // Enum used to save what action closes the widget in modal
    var closeAction = CloseAction.systemAction

    // Load the URL in the WebView
    @SuppressLint("SetJavaScriptEnabled")
    override fun loadUrl(url: String, javascriptToInject: String?) {
        webView.addJavascriptInterface(takeSnapshotBridge, takeSnapshotBridge.name)
        initialize()

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
             hidePayButton: $hidePayButton,
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
             onGetStateSubscribe: function (state){
               window.deunaWidgetState = state;
             },
             isValid: function(fn){
                window.isValid = fn;
             },
             onSubmit: function(fn){
                window.submit = fn;
             }
         };
            ${javascriptToInject ?: ""}
        """.trimIndent()
        )

        listener = object : Listener {
            override fun onWebViewLoaded() {}

            override fun onWebViewError() {
                bridge?.let {
                    runOnUiThread {
                        when (it) {
                            is PaymentWidgetBridge -> it.callbacks.onError?.invoke(
                                PaymentWidgetErrors.initializationFailed
                            )

                            is CheckoutBridge -> it.callbacks.onError?.invoke(PaymentWidgetErrors.initializationFailed)
                            is ElementsBridge -> it.callbacks.onError?.invoke(ElementsErrors.initializationFailed)
                            else -> {}
                        }
                    }
                }
            }

            override fun onOpenInNewTab(url: String) {
                if (newTabDialogFragment != null) {
                    return
                }

                val fragmentActivity = context.findFragmentActivity() ?: return

                newTabDialogFragment = NewTabDialogFragment(url = url, onDialogDestroyed = {
                    newTabDialogFragment = null
                })
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

            bridge?.let {
                runOnUiThread {
                    when (it) {
                        is PaymentWidgetBridge -> it.callbacks.onError?.invoke(PaymentWidgetErrors.noInternetConnection)
                        is CheckoutBridge -> it.callbacks.onError?.invoke(PaymentWidgetErrors.noInternetConnection)
                        is ElementsBridge -> it.callbacks.onError?.invoke(ElementsErrors.noInternetConnection)
                        else -> {}
                    }
                }
            }
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

}