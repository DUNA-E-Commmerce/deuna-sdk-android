package com.deuna.maven.widgets.checkout_widget

enum class CheckoutEvent(val value: String) {
  purchaseRejected("purchaseRejected"),
  paymentProcessing("paymentProcessing"),
  purchaseError("purchaseError"),
  purchase("purchase"),
  apmSuccess("apmSuccess"),
  apmSuccessful("apmSuccessful"),
  apmClickRedirect("apmClickRedirect"),
  apmFailed("apmFailed"),
  changeAddress("changeAddress"),
  changeCart("changeCart"),
  paymentMethods3dsInitiated("paymentMethods3dsInitiated"),
  paymentClick("paymentClick"),
  paymentMethodsCardNumberInitiated("paymentMethodsCardNumberInitiated"),
  paymentMethodsEntered("paymentMethodsEntered"),
  paymentMethodsSelected("paymentMethodsSelected"),
  paymentMethodsShowMore("paymentMethodsShowMore"),
  linkStarted("linkStarted"),
  paymentMethodsStarted("paymentMethodsStarted"),
  adBlock("adBlock"),
  linkClose("linkClose"),
  linkCriticalError("linkCriticalError"),
  couponStarted("couponStarted"),
  linkFailed("linkFailed"),
  paymentMethodsAddCard("paymentMethodsAddCard"),
  checkoutStarted("checkoutStarted"),
  userInfoPhoneInitiated("userInfoPhoneInitiated"),
  paymentMethodsCardExpirationDateInitiated("paymentMethodsCardExpirationDateInitiated"),
  paymentMethodsCardNameInitiated("paymentMethodsCardNameInitiated"),
  paymentMethodsCardSecurityCodeInitiated("paymentMethodsCardSecurityCodeInitiated"),
  paymentMethodsCardNumberEntered("paymentMethodsCardNumberEntered"),
  paymentMethodsCardExpirationDateEntered("paymentMethodsCardExpirationDateEntered"),
  paymentMethodsCardSecurityCodeEntered("paymentMethodsCardSecurityCodeEntered"),
  pointsToWinStarted("pointsToWinStarted"),
  paymentMethodsShowMyCards("paymentMethodsShowMyCards"),
  benefitsStarted("benefitsStarted"),
  donationsStarted("donationsStarted"),
  onBinDetected("onBinDetected"),
  onInstallmentSelected("onInstallmentSelected")
}