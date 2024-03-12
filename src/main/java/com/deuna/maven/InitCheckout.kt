package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.utils.DeunaBroadcastReceiverAction


/**
 * Launch the Checkout View
 *
 * @param orderToken The order token that will be used to show the Checkout
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) A Set of CheckoutEvent values specifying when to close the checkout activity automatically.
 *
 * @throws IllegalArgumentException if the passed orderToken is not valid
 */
fun DeunaSDK.initCheckout(
  context: Context,
  orderToken: String,
  callbacks: CheckoutCallbacks,
  closeEvents: Set<CheckoutEvent> = emptySet(),
) {

  if (orderToken.isEmpty()) {
    callbacks.onError?.invoke(
      CheckoutError(CheckoutErrorType.INVALID_ORDER_TOKEN, null, null),
    )
    return
  }

  val apiKey = this.publicApiKey
  val baseUrl = this.environment.checkoutBaseUrl
  DeunaActivity.setCallback(callbacks)
  val intent = Intent(context, DeunaActivity::class.java).apply {
    putExtra(DeunaActivity.ORDER_TOKEN, orderToken)
    putExtra(DeunaActivity.API_KEY, apiKey)
    putExtra(DeunaActivity.BASE_URL, baseUrl)
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