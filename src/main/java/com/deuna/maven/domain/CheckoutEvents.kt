package com.deuna.maven.domain

enum class CheckoutEvents(val value: String) {
    PURCHASE_REJECTED("purchaseRejected"),
    PURCHASE_SUCCESS("purchase"),
    LINK_CLOSED("linkClosed"),
    APM_SUCCESS("apmSuccess"),
    CHANGE_ADDRESS("changeAddress")
}