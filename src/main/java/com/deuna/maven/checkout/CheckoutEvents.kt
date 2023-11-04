package com.deuna.maven.checkout

enum class CheckoutEvents(val value: String) {
    purchaseRejected("purchaseRejected"),
    paymentProcessing("paymentProcessing"),
    purchaseError("purchaseError"),
    purchase("purchase"),
    APM_SUCCESS("apmSuccess"),
    changeAddress("changeAddress"),
    paymentClick("paymentClick"),
    paymentMethodsCardNumberInitiated("paymentMethodsCardNumberInitiated"),
    paymentMethodsEntered("paymentMethodsEntered"),
    linkStarted("linkStarted"),
    paymentMethodsStarted("paymentMethodsStarted"),
    adBlock("adBlock"),
    couponStarted("couponStarted"),
    linkFailed("linkFailed"),
}