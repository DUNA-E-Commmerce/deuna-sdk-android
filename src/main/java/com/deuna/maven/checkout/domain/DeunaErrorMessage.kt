package com.deuna.maven.checkout.domain

import CheckoutResponse

data class DeunaErrorMessage(var message: String,
                             var type: String,
                             var order: CheckoutResponse.Data.Order?,
                             var user: CheckoutResponse.Data.User?)