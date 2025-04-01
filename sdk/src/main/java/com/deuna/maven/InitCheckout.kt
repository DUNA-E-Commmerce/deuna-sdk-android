package com.deuna.maven

import android.content.Context
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.client.sendOrder
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.dialog_fragments.CheckoutWidgetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Launch the Checkout View
 *
 * @param orderToken The order token that will be used to show the Checkout
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) A Set of CheckoutEvent values specifying when to close the checkout activity automatically.
 * @param userToken (Optional) A user authentication token that allows skipping the OTP flow and shows the user's saved cards.
 * @param styleFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom style file
 */
fun DeunaSDK.initCheckout(
    context: Context,
    orderToken: String,
    callbacks: CheckoutCallbacks,
    closeEvents: Set<CheckoutEvent> = emptySet(),
    userToken: String? = null,
    styleFile: String? = null,
    language: String? = null
) {
    if (orderToken.isEmpty()) {
        callbacks.onError?.invoke(
            PaymentWidgetErrors.invalidOrderToken
        )
        return
    }

    val apiKey = this.publicApiKey
    val baseUrl = this.environment.checkoutBaseUrl


    val fragmentActivity = context.findFragmentActivity() ?: return

    dialogFragment = CheckoutWidgetDialogFragment(
        callbacks = callbacks,
        closeEvents = closeEvents
    )
    dialogFragment?.show(fragmentActivity.supportFragmentManager, "CheckoutWidgetDialogFragment")

    CheckoutBuilder().getOrderLink(
        baseUrl = baseUrl,
        orderToken = orderToken,
        apiKey = apiKey,
        userToken = userToken,
        styleFile = styleFile,
        language = language
    ) { error, url ->
        if (error != null) {
            callbacks.onError?.invoke(error)
        } else {
            val fragment = dialogFragment;
            if (fragment is CheckoutWidgetDialogFragment) {
                fragment.loadUrl(url!!)
            }
        }
    }
}


class CheckoutBuilder() {
    /**
     * Fetches the order details from the server using the provided credentials.
     * Parses the response to extract the payment link and load it in the WebView.
     */
    fun getOrderLink(
        baseUrl: String,
        orderToken: String,
        apiKey: String,
        userToken: String?,
        styleFile: String?,
        language: String?,
        completion: (error: PaymentsError?, url: String?) -> Unit
    ) {
        sendOrder(baseUrl, orderToken, apiKey, object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>

                    if (orderMap == null) {
                        completion(PaymentWidgetErrors.linkCouldNotBeGenerated, null)
                        return
                    }

                    val paymentLink = orderMap["payment_link"] as String?

                    if (paymentLink.isNullOrEmpty()) {
                        completion(PaymentWidgetErrors.linkCouldNotBeGenerated, null)
                        return
                    }

                    val queryParameters = mutableMapOf<String, String>()
                    queryParameters[QueryParameters.MODE] = QueryParameters.WIDGET

                    if (userToken != null) {
                        queryParameters[QueryParameters.USER_TOKEN] = userToken
                    }

                    if (styleFile != null) {
                        queryParameters[QueryParameters.STYLE_FILE] = styleFile
                    }

                    if (!language.isNullOrEmpty()) {
                        queryParameters[QueryParameters.LANGUAGE] = language
                    }

                    completion(
                        null,
                        Utils.buildUrl(baseUrl = paymentLink, queryParams = queryParameters)
                    )
                } else {
                    // Handle missing order data
                    completion(orderCouldNotBeRetrieved(), null)
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                completion(orderCouldNotBeRetrieved(), null)
            }
        })
    }

    /**
     * This method is called when the order details are not found on the server.
     * It invokes the onError callback with a CheckoutError of type ORDER_NOT_FOUND.
     */
    private fun orderCouldNotBeRetrieved(): PaymentsError {
        return PaymentsError(
            type = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED,
            metadata = PaymentsError.Metadata(
                code = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.name,
                message = PaymentsError.Type.ORDER_COULD_NOT_BE_RETRIEVED.message,
            )
        )
    }
}