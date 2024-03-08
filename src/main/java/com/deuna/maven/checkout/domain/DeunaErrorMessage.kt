package com.deuna.maven.checkout.domain

import CheckoutResponse
import com.deuna.maven.shared.*

data class DeunaErrorMessage(
  var type: DeunaSDKError,
  var order: CheckoutResponse.Data.Order?,
  var user: CheckoutResponse.Data.User?,
)