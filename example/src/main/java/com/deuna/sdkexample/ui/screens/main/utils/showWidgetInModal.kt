package com.deuna.sdkexample.ui.screens.main.utils

import android.content.Context
import android.util.Log
import com.deuna.sdkexample.shared.CheckoutResult
import com.deuna.sdkexample.shared.ElementsResult
import com.deuna.sdkexample.shared.PaymentWidgetResult
import com.deuna.sdkexample.ui.screens.main.WidgetToShow
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.clickToPay
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.saveCard
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.showCheckout
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.showPaymentWidget


fun showWidgetInModal(
    context: Context, viewModel: MainViewModel, widgetToShow: WidgetToShow
) {
    when (widgetToShow) {
        WidgetToShow.PAYMENT_WIDGET -> {
            viewModel.showPaymentWidget(context = context, completion = { result ->
                when (result) {
                    is PaymentWidgetResult.Canceled -> Log.d("PAYMENT", "Canceled")
                    is PaymentWidgetResult.Error -> Log.d("PAYMENT", "Error")
                    is PaymentWidgetResult.Success -> Log.d("PAYMENT", "Success")
                }
            })
        }

        WidgetToShow.CHECKOUT_WIDGET -> {
            viewModel.showCheckout(
                context = context,
                completion = { result ->
                    when (result) {
                        is CheckoutResult.Canceled -> Log.d("CHECKOUT", "Canceled")
                        is CheckoutResult.Error -> Log.d("CHECKOUT", "Error")
                        is CheckoutResult.Success -> Log.d("CHECKOUT", "Success")
                    }
                }
            )
        }

        WidgetToShow.VAULT_WIDGET -> {
            viewModel.saveCard(
                context = context,
                completion = { result ->
                    when (result) {
                        is ElementsResult.Canceled -> Log.d("VAULT", "Canceled")
                        is ElementsResult.Error -> Log.d("VAULT", "Error")
                        is ElementsResult.Success -> Log.d("VAULT", "Success")
                    }
                }
            )
        }

        WidgetToShow.CLICK_TO_PAY_WIDGET -> {
            viewModel.clickToPay(
                context = context,
                completion = { result ->
                    when (result) {
                        is ElementsResult.Canceled -> Log.d("CLICK_TO_PAY", "Canceled")
                        is ElementsResult.Error -> Log.d("CLICK_TO_PAY", "Error")
                        is ElementsResult.Success -> Log.d("CLICK_TO_PAY", "Success")
                    }
                }
            )
        }
    }
}