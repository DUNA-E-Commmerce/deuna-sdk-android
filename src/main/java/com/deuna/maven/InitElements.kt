package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.deuna.maven.element.DeunaElementActivity
import com.deuna.maven.element.domain.ElementsCallbacks
import com.deuna.maven.element.domain.ElementsEvent
import com.deuna.maven.utils.DeunaBroadcastReceiverAction
import java.lang.IllegalStateException


/**
 * Launch the Elements View
 *
 * @param userToken The user token
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) An array of CheckoutEvent values specifying when to close the elements activity automatically.
 *
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    userToken: String,
    callbacks: ElementsCallbacks,
    closeEvents: Set<ElementsEvent> = emptySet(),
) {
    require(userToken.isNotEmpty()) {
        "userToken must not be empty"
    }

    val apiKey = this.publicApiKey
    val baseUrl = this.environment.elementsBaseUrl

    DeunaElementActivity.setCallback(callbacks)

    val elementUrl =Uri.parse("$baseUrl/vault")
        .buildUpon()
        .appendQueryParameter("userToken", userToken)
        .appendQueryParameter("publicApiKey", apiKey)
        .appendQueryParameter("mode", "widget")
        .build().toString()


    val intent = Intent(context, DeunaElementActivity::class.java).apply {
        putExtra(DeunaElementActivity.EXTRA_URL, elementUrl)
        putStringArrayListExtra(
            DeunaElementActivity.CLOSE_ON_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}


/**
 * Closes the elements activity if it's currently running.
 *
 * @param context The application or activity context
 */
fun DeunaSDK.closeElements(context: Context) {
    com.deuna.maven.closeElements(context)
}

/**
 * Global function used to send a broadcast event to close the elements view
 */
fun closeElements(context: Context) {
    context.sendBroadcast(Intent(DeunaBroadcastReceiverAction.ELEMENTS.value))
}