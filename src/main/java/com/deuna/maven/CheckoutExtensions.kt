package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.os.Build
import com.deuna.maven.checkout.CheckoutCallbacks
import com.deuna.maven.checkout.CheckoutEvent
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.utils.DeunaBroadcastReceiverAction
import java.lang.IllegalStateException


/**
 * Launch the Checkout View
 *
 * @param orderToken The order token that will be used to show the Checkout
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeOnEvents (Optional) An array of CheckoutEvent values specifying when to close the checkout activity automatically.
 *
 * @throws IllegalArgumentExceptio if the passed orderToken is not valid
 */
fun DeunaSDK.initCheckout(
    context: Context,
    orderToken: String,
    callbacks: CheckoutCallbacks,
    closeOnEvents: Array<CheckoutEvent>? = null,
) {
    require(orderToken.isNotEmpty()) {
        "orderToken must not be empty"
    }

    val closeEvents = closeOnEvents ?: emptyArray()
    val apiKey = this.privateApiKey
    val baseUrl = this.environment.checkoutBaseUrl
    DeunaActivity.setCallback(callbacks)
    val intent = Intent(context, DeunaActivity::class.java).apply {
        putExtra(DeunaActivity.ORDER_TOKEN, orderToken)
        putExtra(DeunaActivity.API_KEY, apiKey)
        putExtra(DeunaActivity.BASE_URL, baseUrl)
        putExtra(DeunaActivity.LOGGING_ENABLED, Build.TYPE == "debug")
        putStringArrayListExtra(
            DeunaActivity.CLOSE_ON_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}

/**
 * Closes the checkout activity if it's currently running.
 *
 * @param context The application or activity context
 */
fun DeunaSDK.closeCheckout(context: Context) {
    com.deuna.maven.closeCheckout(context)
}

/**
 * Global function used to send a broadcast event to close the checkout view
 */
fun closeCheckout(context: Context) {
    context.sendBroadcast(
        Intent(DeunaBroadcastReceiverAction.CHECKOUT.value)
    )
}