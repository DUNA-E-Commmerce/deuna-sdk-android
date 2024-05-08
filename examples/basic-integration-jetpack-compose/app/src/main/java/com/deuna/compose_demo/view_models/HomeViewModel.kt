package com.deuna.compose_demo.view_models

import CheckoutResponse
import ElementsResponse
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.deuna.compose_demo.screens.CheckoutResult
import com.deuna.compose_demo.screens.ElementsResult
import com.deuna.compose_demo.screens.PaymentWidgetResult
import com.deuna.maven.*
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.element.domain.*
import com.deuna.maven.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.shared.*
import kotlinx.coroutines.*


/**
 * ViewModel for the Home screen, responsible for handling user interactions and data manipulation.
 * @param deunaSDK The DeunaSDK instance used for payment and card saving operations.
 */
class HomeViewModel(private val deunaSDK: DeunaSDK) : ViewModel() {

  // State variables for order token and user token
  val orderToken = mutableStateOf("")
  val userToken = mutableStateOf("")


  fun showPaymentWidget(
    context: Context,
    completion: (PaymentWidgetResult) -> Unit,
  ) {
    deunaSDK.initPaymentWidget(
      context = context,
      orderToken = orderToken.value.trim(),
      callbacks = paymentWidgetsCallbacks(context, completion)
    )
  }


  private fun checkoutCallbacks(
    context: Context,
    completion: (CheckoutResult) -> Unit,
  ): CheckoutCallbacks {
    return CheckoutCallbacks().apply {
      onSuccess = { response ->
        deunaSDK.closeCheckout(context)
        viewModelScope.launch {
          completion(CheckoutResult.Success(response))
        }
      }
      onError = { error ->
//        deunaSDK.closeCheckout(context)
//        viewModelScope.launch {
//          completion(CheckoutResult.Error(error))
//        }
      }
      onCanceled = {
        viewModelScope.launch {
          completion(CheckoutResult.Canceled)
        }
      }
      eventListener = { event, _ ->
        when (event) {
          CheckoutEvent.changeCart, CheckoutEvent.changeAddress -> {
            deunaSDK.closeCheckout(context)
            viewModelScope.launch {
              completion(CheckoutResult.Canceled)
            }
          }

          else -> Log.d("DeunaSDK", "on event ${event.value}")
        }
      }
    }
  }

  /**
   * Initiates the payment process.
   * @param context The activity context.
   * @param completion Callback to be invoked upon completion of the payment process.
   */
  fun showCheckout(
    context: Context,
    completion: (CheckoutResult) -> Unit,
  ) {
    deunaSDK.initCheckout(
      context = context,
      orderToken = orderToken.value.trim(),
      callbacks = checkoutCallbacks(context, completion)
    )
  }


  private fun paymentWidgetsCallbacks(
    context: Context,
    completion: (PaymentWidgetResult) -> Unit,
  ): PaymentWidgetCallbacks {
    return PaymentWidgetCallbacks().apply {
      onPaymentSuccess = { data ->
        deunaSDK.closeCheckout(context)
        viewModelScope.launch {
          completion(PaymentWidgetResult.Success(data))
        }
      }
      onClosed = {
//        viewModelScope.launch {
//          completion(CheckoutResult.Canceled)
//        }
      }
    }
  }


  /**
   * Initiates the process of saving the card information.
   * @param context The activity context.
   * @param completion Callback to be invoked upon completion of the card saving process.
   */
  fun saveCard(
    context: Context,
    completion: (ElementsResult) -> Unit,
  ) {
    deunaSDK.initElements(
      context = context,
      userToken = userToken.value.trim(),
      callbacks = elementsCallbacks(context, completion)
    )
  }


  private fun elementsCallbacks(
    context: Context,
    completion: (ElementsResult) -> Unit,
  ): ElementsCallbacks {
    return ElementsCallbacks().apply {
      onSuccess = { response ->
        deunaSDK.closeElements(context)
        viewModelScope.launch {
          completion(ElementsResult.Success(response))
        }
      }
      onError = { error ->
        deunaSDK.closeElements(context)
        viewModelScope.launch {
          completion(ElementsResult.Error(error))
        }
      }
      onCanceled = {
        viewModelScope.launch {
          completion(ElementsResult.Canceled)
        }
      }
      eventListener = { event, _ ->
        Log.d("DeunaSDK", "on event ${event.value}")
      }
    }
  }
}

