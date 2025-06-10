package com.deuna.sdkexample.ui.screens.main.utils

import android.util.Log
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.widgets.voucher.VoucherCallbacks
import com.deuna.sdkexample.ui.screens.main.WidgetToShow

const val DEBUG_TAG = "DeunaSDK"

fun buildWidgetConfig(
    widgetToShow: WidgetToShow,
    orderToken: String,
    userToken: String,
    deunaSDK: DeunaSDK,
    onPaymentSuccess: (order: Json) -> Unit,
    onSaveCardSuccess: (cardData: Json) -> Unit
): DeunaWidgetConfiguration {
    return when (widgetToShow) {
        WidgetToShow.PAYMENT_WIDGET -> PaymentWidgetConfiguration(
            orderToken = orderToken,
            userToken = userToken,
            callbacks = PaymentWidgetCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
                onEventDispatch = { event, _ ->
                    Log.i(DEBUG_TAG, "onEventDispatch event: $event")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.NEXT_ACTION_WIDGET -> NextActionWidgetConfiguration(
            orderToken = orderToken,
            callbacks = NextActionCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.VOUCHER_WIDGET -> VoucherWidgetConfiguration(
            orderToken = orderToken,
            callbacks = VoucherCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.CHECKOUT_WIDGET -> CheckoutWidgetConfiguration(
            orderToken = orderToken,
            userToken = userToken,
            callbacks = CheckoutCallbacks().apply {
                onSuccess = onPaymentSuccess
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            },
            sdkInstance = deunaSDK
        )

        WidgetToShow.VAULT_WIDGET -> ElementsWidgetConfiguration(
            userToken = userToken,
            orderToken = orderToken,
            sdkInstance = deunaSDK,
            callbacks = ElementsCallbacks().apply {
                onSuccess = {
                    val cardData = (it["metadata"] as Json)["createdCard"] as Json
                    onSaveCardSuccess(cardData)
                }
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            }
        )

        WidgetToShow.CLICK_TO_PAY_WIDGET -> ElementsWidgetConfiguration(
            userToken = userToken,
            orderToken = orderToken,
            sdkInstance = deunaSDK,
            callbacks = ElementsCallbacks().apply {
                onSuccess = {
                    val cardData = (it["metadata"] as Json)["createdCard"] as Json
                    onSaveCardSuccess(cardData)
                }
                onError = {
                    Log.i(DEBUG_TAG, "onError code: ${it.metadata?.code}")
                    Log.i(DEBUG_TAG, "onError message: ${it.metadata?.message}")
                }
            }
        )
    }
}
