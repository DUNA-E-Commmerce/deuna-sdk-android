package com.deuna.compose_demo.view_models

import CheckoutResponse
import ElementsResponse
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.deuna.maven.*
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.element.domain.*
import kotlinx.coroutines.*

class HomeViewModel(private val deunaSDK: DeunaSDK) : ViewModel() {
  val orderToken = mutableStateOf("")
  val userToken = mutableStateOf("")


  fun payment(
    context: Context,
    completion: (CheckoutResponse?, DeunaErrorMessage?) -> Unit,
  ) {
    deunaSDK.initCheckout(
      context = context,
      orderToken = orderToken.value.trim(),
      callbacks = CheckoutCallbacks().apply {
        onSuccess = {
          deunaSDK.closeCheckout(context = context)
          viewModelScope.launch {
            completion(it, null)
          }
        }
        onError = {
          deunaSDK.closeCheckout(context = context)
          viewModelScope.launch {
            completion(null, it)
          }
        }
        eventListener = { event, _ ->
          Log.d("DeunaSDK", "on event ${event.value}")
        }
      }
    )
  }

  fun saveCard(
    context: Context,
    completion: (ElementsResponse?, ElementsErrorMessage?) -> Unit,
  ) {
    deunaSDK.initElements(
      context = context,
      userToken = userToken.value.trim(),
      callbacks = ElementsCallbacks().apply {
        onSuccess = {
          deunaSDK.closeElements(context = context)
          viewModelScope.launch {
            completion(it, null)
          }
        }
        onError = {
          deunaSDK.closeElements(context = context)
          viewModelScope.launch {
            completion(null, it)
          }
        }
        eventListener = { event, _ ->
          Log.d("DeunaSDK", "on event ${event.value}")
        }
      }
    )
  }
}