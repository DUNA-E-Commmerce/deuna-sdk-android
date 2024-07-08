package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.deuna.maven.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.PaymentWidgetErrorType
import com.deuna.maven.web_views.PaymentWidgetActivity
import com.deuna.maven.web_views.base.BaseWebViewActivity
import org.json.JSONObject


/**
 * Launch the payment widget View
 *
 * @param orderToken The order token that will be used to show the payment widget
 * @param context The application or activity context
 * @param callbacks An instance of PaymentWidgetCallbacks to receive event notifications.
 */
fun DeunaSDK.initPaymentWidget(
    context: Context,
    orderToken: String,
    callbacks: PaymentWidgetCallbacks,
) {

    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrorType.INVALID_ORDER_TOKEN
        )
        return
    }

    val baseUrl = this.environment.paymentWidgetBaseUrl

    PaymentWidgetActivity.setCallbacks(callbacks)

    val paymentUrl = Uri.parse("$baseUrl/now/$orderToken?mode=widget")
        .buildUpon()
        .build().toString()


    val intent = Intent(context, PaymentWidgetActivity::class.java).apply {
        putExtra(PaymentWidgetActivity.EXTRA_URL, paymentUrl)
    }
    context.startActivity(intent)
}

/**
 * Set custom styles on the payment widget.
 * This function must be only called inside the onCardBinDetected callback
 *
 * @param context The application or activity context
 * @param data The JSON data to update the payment widget UI
 */
fun DeunaSDK.setCustomCss( context: Context,  data: Map<String, Any>) {
   context.sendBroadcast(
       Intent(PaymentWidgetActivity.SEND_CUSTOM_STYLES_BROADCAST_RECEIVER_ACTION).apply {
           putExtra(
               PaymentWidgetActivity.EXTRA_CUSTOM_STYLES,
               JSONObject(data).toString()
           )
       }
   )
}


/**
 * Closes the payment widget if it's currently running.
 *
 * @param context The application or activity context
 */
fun DeunaSDK.closePaymentWidget(context: Context) {
    com.deuna.maven.closePaymentWidget()
}

/**
 * Global function used to send a broadcast event to close the payment widget view
 */
fun closePaymentWidget() {
    BaseWebViewActivity.closeWebView()
}