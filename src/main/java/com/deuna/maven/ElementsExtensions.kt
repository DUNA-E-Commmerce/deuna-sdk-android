package com.deuna.maven

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.deuna.maven.checkout.DeunaActivity
import com.deuna.maven.checkout.domain.ElementType
import com.deuna.maven.element.DeunaElementActivity
import com.deuna.maven.element.domain.ElementsCallbacks
import com.deuna.maven.element.domain.ElementsEvent
import com.deuna.maven.utils.DeunaBroadcastReceiverAction
import java.lang.IllegalStateException
import java.util.Locale


/**
 * Launch the Elements View
 *
 * @param userToken The user token
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeOnEvents (Optional) An array of CheckoutEvent values specifying when to close the elements activity automatically.
 *
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    userToken: String,
    element: ElementType,
    callbacks: ElementsCallbacks,
    showCloseButton: Boolean = false,
    closeOnEvents: Array<ElementsEvent>? = null,
) {
    val closeEvents = closeOnEvents ?: emptyArray()
    val apiKey = this.publicApiKey

    DeunaElementActivity.setCallback(callbacks)

    val elementUrl = buildElementUrl(
        baseUrl = this.environment.elementsBaseUrl,
        element = element,
        userToken = userToken,
        apiKey = apiKey,
        showCloseButton = showCloseButton
    )

    val intent = Intent(context, DeunaElementActivity::class.java).apply {
        putExtra(DeunaElementActivity.EXTRA_URL, elementUrl)
        putExtra(DeunaElementActivity.LOGGING_ENABLED, Build.TYPE == "debug")
        putStringArrayListExtra(
            DeunaElementActivity.CLOSE_ON_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}

private fun buildElementUrl(
    baseUrl: String,
    element: ElementType,
    userToken: String,
    apiKey: String,
    showCloseButton: Boolean
): String {
    val mode = if (showCloseButton) "widget" else ""
    return Uri.parse("$baseUrl/{type}")
        .buildUpon()
        .appendQueryParameter("userToken", userToken)
        .appendQueryParameter("publicApiKey", apiKey)
        .appendQueryParameter("mode", mode)
        .build().toString().replace("{type}", element.toString().lowercase(Locale.ROOT))
}


/**
 * Closes the elements activity if it's currently running.
 *
 * @param context The application or activity context
 */
fun DeunaSDK.closeElements(context: Context) {
    context.sendBroadcast(Intent(DeunaBroadcastReceiverAction.ELEMENTS.value))
}