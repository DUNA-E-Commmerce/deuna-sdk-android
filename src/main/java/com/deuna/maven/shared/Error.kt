package com.deuna.maven.shared

enum class DeunaSDKError(val message: String) {
  NO_INTERNET_CONNECTION("No internet connection available"),
  CHECKOUT_INITIALIZATION_FAILED("Failed to initialize the checkout"),
  ORDER_NOT_FOUND("Order not found"),
  PAYMENT_ERROR("An error occurred while processing payment"),
  USER_ERROR("An error occurred related to the user authentication"),
  ORDER_ERROR("An order related error occurred"),
  UNKNOWN_ERROR("An unknown error occurred"),
  VAULT_SAVE_ERROR("Vault error")
}