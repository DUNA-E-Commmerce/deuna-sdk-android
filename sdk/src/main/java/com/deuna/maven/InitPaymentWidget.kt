package com.deuna.maven

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity
import com.deuna.maven.payment_widget.domain.PaymentWidgetCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentWidgetErrors
import com.deuna.maven.shared.QueryParameters
import com.deuna.maven.shared.Utils
import com.deuna.maven.shared.toBase64
import com.deuna.maven.web_views.deuna.DeunaWebView
import com.deuna.maven.web_views.deuna.extensions.refetchOrder
import com.deuna.maven.web_views.dialog_fragments.PaymentWidgetDialogFragment

/**
 * Launch the payment widget View
 *
 * @param orderToken The order token that will be used to show the payment widget
 * @param context The application or activity context
 * @param callbacks An instance of PaymentWidgetCallbacks to receive event notifications.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param styleFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom style file.
 * @param paymentMethods (Optional) A list of allowed payment methods. This parameter determines what type of widget should be rendered.
 * @param checkoutModules (Optional) A list  display the payment widget with new patterns or with different functionalities
 */
fun DeunaSDK.initPaymentWidget(
    context: Context,
    orderToken: String,
    callbacks: PaymentWidgetCallbacks,
    userToken: String? = null,
    styleFile: String? = null,
    paymentMethods: List<Json> = emptyList(),
    checkoutModules: List<Json> = emptyList(),
    language: String? = null
) {

    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    val baseUrl = this.environment.paymentWidgetBaseUrl

    val queryParameters = mutableMapOf<String, String>()
    queryParameters[QueryParameters.MODE] = QueryParameters.WIDGET

    if (!language.isNullOrEmpty()) {
        queryParameters[QueryParameters.LANGUAGE] = language
    }

    if (!userToken.isNullOrEmpty()) {
        queryParameters[QueryParameters.USER_TOKEN] = userToken
    }

    if (!styleFile.isNullOrEmpty()) {
        queryParameters[QueryParameters.STYLE_FILE] = styleFile
    }

    val xpropsB64 = mutableMapOf<String, Any>()
    xpropsB64[QueryParameters.PUBLIC_API_KEY] = publicApiKey


    if (paymentMethods.isNotEmpty()) {
        xpropsB64[QueryParameters.PAYMENT_METHODS] = paymentMethods
    }

    if (checkoutModules.isNotEmpty()) {
        xpropsB64[QueryParameters.CHECKOUT_MODULES] = checkoutModules
    }

    queryParameters[QueryParameters.XPROPS_B64] = xpropsB64.toBase64()

    val paymentUrl = Utils.buildUrl(
        baseUrl = "$baseUrl/now/$orderToken",
        queryParams = queryParameters,
    )

    val fragmentActivity = context.findFragmentActivity() ?: return

    dialogFragment = PaymentWidgetDialogFragment(url = paymentUrl, callbacks = callbacks)
    dialogFragment?.show(fragmentActivity.supportFragmentManager, "PaymentWidgetDialogFragment")
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}